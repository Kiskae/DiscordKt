package net.serverpeon.discord.internal.data.model

import net.serverpeon.discord.interaction.Editable
import net.serverpeon.discord.interaction.PermissionException
import net.serverpeon.discord.internal.data.EventInput
import net.serverpeon.discord.internal.jsonmodels.ChannelModel
import net.serverpeon.discord.internal.jsonmodels.MessageModel
import net.serverpeon.discord.internal.rest.WrappedId
import net.serverpeon.discord.internal.rest.retro.Channels.*
import net.serverpeon.discord.internal.rx
import net.serverpeon.discord.internal.rxObservable
import net.serverpeon.discord.internal.toFuture
import net.serverpeon.discord.internal.ws.data.inbound.Channels
import net.serverpeon.discord.internal.ws.data.inbound.Misc
import net.serverpeon.discord.message.Message
import net.serverpeon.discord.model.*
import rx.Completable
import rx.Observable
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Pattern

abstract class ChannelNode<T : ChannelNode<T>> private constructor(val root: DiscordNode,
                                                                   override val id: DiscordId<Channel>
) : Channel, Channel.Text, EventInput<T> {
    override fun delete(): CompletableFuture<Void> {
        return root.api.Channels.deleteChannel(WrappedId(id)).toFuture()
    }

    override fun messageHistory(limit: Int): Observable<PostedMessage> {
        checkPermission(PermissionSet.Permission.READ_MESSAGE_HISTORY)
        return Observable.concat<List<MessageModel>>(Observable.create { sub ->
            //TODO: some sort of rate limiting mechanism
            var currentLimit = limit
            var lastMessage: MessageModel? = null
            sub.setProducer {
                if (currentLimit > 0) {
                    //Produce the next call.
                    val requestLimit = Math.min(currentLimit, 100)
                    currentLimit -= requestLimit
                    val obs = root.api.Channels.getMessages(WrappedId(id),
                            limit = requestLimit,
                            before = lastMessage?.let {
                                WrappedId(it.id)
                            }
                    ).rxObservable().publish().apply {
                        // Set lastMessage to last message of list
                        sub.add(subscribe { lastMessage = it.last() })
                    }.refCount()

                    sub.onNext(obs)
                } else {
                    sub.onCompleted()
                }
            }
        }).flatMapIterable {
            it
        }.map { Builder.message(it, root) }
    }

    override fun sendMessage(message: Message, textToSpeech: Boolean?): CompletableFuture<PostedMessage> {
        checkPermission(PermissionSet.Permission.SEND_MESSAGES)
        if (textToSpeech == true) {
            checkPermission(PermissionSet.Permission.SEND_TTS_MESSAGES)
        }
        val mentions = message.mentions.map { it.id }.toList().toBlocking().first()
        return root.api.Channels.sendMessage(WrappedId(id), SendMessageRequest(
                message.encodedContent,
                mentions = if (mentions.isNotEmpty()) mentions else null,
                tts = textToSpeech
        )).toFuture().thenApply { Builder.message(it, root) }
    }

    abstract fun checkPermission(perm: PermissionSet.Permission)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as ChannelNode<*>

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }


    /**
     * Public channel objects start here
     */
    class Public internal constructor(root: DiscordNode,
                                      id: DiscordId<Channel>,
                                      override val guild: GuildNode,
                                      override var topic: String,
                                      override val type: Channel.Type,
                                      override var name: String,
                                      internal var overrides: LinkedHashMap<DiscordId<*>, OverrideData>,
                                      internal var position: Int
    ) : ChannelNode<Public>(root, id), Channel.Public {
        private val changeId = AtomicInteger(0)

        override fun handler(): EventInput.Handler<Public> {
            return PublicChannelUpdater
        }

        override fun checkPermission(perm: PermissionSet.Permission) {
            guild.selfAsMember.checkPermission(this, perm)
        }

        override val voiceStates: Observable<VoiceState> = Observable.empty() //TODO: implement voice state

        override val memberOverrides: Observable<Channel.ResolvedPermission<Guild.Member>>
            get() = filterAndMapOverrides(shouldBeMember = true) { id ->
                val user = guild.memberMap[id]!!
                val perms = permissionsFor(user)
                Channel.ResolvedPermission(user, perms)
            }

        override val roleOverrides: Observable<Channel.ResolvedPermission<Role>>
            get() = filterAndMapOverrides(shouldBeMember = false) { id ->
                val role = guild.roleMap[id]!!
                val perms = permissionsFor(role)
                Channel.ResolvedPermission(role, perms)
            }

        override fun permissionsFor(role: Role): PermissionSet {
            val overrides = overrides[role.id]
            return overrides?.let {
                role.permissions.without(it.deny).with(it.allow)
            } ?: role.permissions
        }

        override fun permissionsFor(member: Guild.Member): PermissionSet {
            return resolveMemberPerms(member, true)
        }

        override val isPrivate: Boolean
            get() = false

        private fun resolveMemberPerms(member: Guild.Member, applyOwnOverride: Boolean): PermissionSet {
            return if (member.id == guild.ownerId) {
                PermissionSet.ALL
            } else {
                member.roles.map {
                    permissionsFor(it)
                }.reduce(PermissionSet.ZERO, { p1, p2 ->
                    p1.with(p2) // Fold all roles together
                }).let {
                    if (applyOwnOverride) {
                        it.map { rolePerms ->
                            val overrides = overrides[member.id]
                            overrides?.let {
                                rolePerms.without(it.deny).with(it.allow)
                            } ?: rolePerms
                        }
                    } else {
                        it
                    }
                }.toBlocking().first() //Everything SHOULD be available, so this won't cause problems (famous last words)
            }
        }

        override fun setOverride(allow: PermissionSet, deny: PermissionSet, member: Guild.Member): CompletableFuture<PermissionSet> {
            return if (allow.empty() && deny.empty()) {
                val current = overrides[member.id]
                if (current != null) {
                    //Go through deletion
                    root.api.Channels.deletePermissionsUser(WrappedId(id), WrappedId(member.id)).toFuture()
                            .thenApply {
                                resolveMemberPerms(member, false)
                            }
                } else {
                    //No override, return current
                    CompletableFuture.completedFuture(permissionsFor(member))
                }
            } else {
                root.api.Channels.changePermissionsUser(WrappedId(id), WrappedId(member.id), EditPermissionRequest(
                        allow = allow,
                        deny = deny,
                        id = member.id,
                        type = "member"
                )).toFuture().thenApply {
                    resolveMemberPerms(member, false).without(deny).with(allow)
                }
            }
        }

        override fun setOverride(allow: PermissionSet, deny: PermissionSet, role: Role): CompletableFuture<PermissionSet> {
            return if (allow.empty() && deny.empty()) {
                val current = overrides[role.id]
                if (current != null) {
                    root.api.Channels.deletePermissionsRole(WrappedId(id), WrappedId(role.id)).toFuture()
                            .thenApply { role.permissions }
                } else {
                    CompletableFuture.completedFuture(role.permissions)
                }
            } else {
                root.api.Channels.changePermissionsRole(WrappedId(id), WrappedId(role.id), EditPermissionRequest(
                        allow = allow,
                        deny = deny,
                        id = role.id,
                        type = "role"
                )).toFuture().thenApply { role.permissions.without(deny).with(allow) }
            }
        }

        override fun indicateTyping(): Completable {
            return root.api.Channels.postActivity(WrappedId(id)).rx()
        }

        override fun edit(): Channel.Public.Edit {
            guild.selfAsMember.checkPermission(this, PermissionSet.Permission.MANAGE_CHANNEL)

            return Transaction(topic, name)
        }

        private fun <T : DiscordId.Identifiable<T>, G : T> filterAndMapOverrides(
                shouldBeMember: Boolean,
                objectLookup: (DiscordId<T>) -> Channel.ResolvedPermission<G>
        ): Observable<Channel.ResolvedPermission<G>> {
            return Observable.defer {
                Observable.from(overrides.entries)
            }.filter {
                it.value.isMember == shouldBeMember
            }.map {
                @Suppress("UNCHECKED_CAST")
                objectLookup(it.key as DiscordId<T>)
            }
        }

        inner class Transaction(override var topic: String, override var name: String) : Channel.Public.Edit {
            private var completed = AtomicBoolean(false)
            private val changeIdAtInit = changeId.get()

            override fun commit(): CompletableFuture<Channel.Public> {
                if (changeId.compareAndSet(changeIdAtInit, changeIdAtInit + 1)) {
                    throw Editable.ResourceChangedException(this@Public)
                } else if (completed.getAndSet(true)) {
                    throw IllegalStateException("Don't call complete() twice")
                } else {
                    return root.api.Channels.editChannel(WrappedId(id), EditChannelRequest(
                            name = name,
                            position = position,
                            topic = topic
                    )).toFuture().thenApply {
                        Builder.channel(it, guild)
                    }
                }
            }
        }
    }

    data class OverrideData(val allow: PermissionSet, val deny: PermissionSet, val isMember: Boolean)

    private object PublicChannelUpdater : EventInput.Handler<Public> {
        override fun voiceStateUpdate(target: Public, e: Misc.VoiceStateUpdate) {
            // Pass through to member
            target.guild.memberMap[e.update.user_id]?.handle(e)
        }

        override fun typingStart(target: Public, e: Misc.TypingStart) {
            // Ignored, not something we need to represent
        }

        override fun channelUpdate(target: Public, e: Channels.Update) {
            e.channel.let {
                target.name = it.name
                target.topic = it.topic ?: ""
                target.overrides = translateOverrides(it.permission_overwrites)
                target.position = it.position
            }
        }
    }

    /**
     * Private channel objects start here
     */
    class Private internal constructor(root: DiscordNode,
                                       id: DiscordId<Channel>,
                                       override val recipient: UserNode
    ) : ChannelNode<Private>(root, id), Channel.Private {
        override fun handler(): EventInput.Handler<Private> {
            return PrivateChannelUpdater
        }

        override fun checkPermission(perm: PermissionSet.Permission) {
            if (perm == PermissionSet.Permission.MANAGE_MESSAGES) {
                throw PermissionException(PermissionSet.Permission.MANAGE_MESSAGES)
            }
        }

        override fun indicateTyping(): Completable {
            return root.api.Channels.postActivity(WrappedId(id)).rx()
        }

        override fun sendMessage(message: Message, textToSpeech: Boolean?): CompletableFuture<PostedMessage> {
            val mentions = message.mentions.toList().toBlocking().first()
            return root.api.Channels.sendMessage(WrappedId(id), SendMessageRequest(
                    content = message.encodedContent,
                    mentions = if (mentions.isEmpty()) null else mentions.map { it.id },
                    tts = textToSpeech
            )).toFuture().thenApply {
                error("Message not yet translatable")
            }
        }

        override val type: Channel.Type
            get() = Channel.Type.PRIVATE
        override val isPrivate: Boolean
            get() = true
    }

    private object PrivateChannelUpdater : EventInput.Handler<Private> {
        override fun typingStart(target: Private, e: Misc.TypingStart) {
            // Ignored
        }
    }

    companion object {
        private val CHANNEL_NAME_REQUIREMENTS = Pattern.compile("^[A-Za-z0-9\\-]{2,100}$")

        internal fun translateOverrides(
                permission_overwrites: List<ChannelModel.PermissionOverwrites>
        ): LinkedHashMap<DiscordId<*>, OverrideData> {
            val pairs = permission_overwrites.map {
                DiscordId<Role>(it.id) to OverrideData(it.allow, it.deny, it.type == "member")
            }.toTypedArray()
            return linkedMapOf(*pairs)
        }

        fun sanitizeChannelName(name: String): String {
            val nameWithDashes = name.replace(' ', '-')
            check(CHANNEL_NAME_REQUIREMENTS.asPredicate().test(nameWithDashes)) {
                "Channel name contains invalid characters: $name"
            }
            return nameWithDashes.toLowerCase()
        }
    }
}