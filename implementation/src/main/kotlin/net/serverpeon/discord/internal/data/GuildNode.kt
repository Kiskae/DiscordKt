package net.serverpeon.discord.internal.data

import com.google.common.collect.ImmutableList
import net.serverpeon.discord.internal.rest.data.GuildModel
import net.serverpeon.discord.internal.ws.data.inbound.*
import net.serverpeon.discord.model.*
import rx.Observable

class GuildNode(val root: DiscordNode, override val id: DiscordId<Guild>) : Guild, Event.Visitor {
    internal var channelMap = createEmptyMap<Channel, ChannelNode>()
    internal var roleMap = createEmptyMap<Role, RoleNode>()
    internal var memberMap = createEmptyMap<User, MemberNode>()
    internal var emojiMap = createEmptyMap<Emoji, EmojiNode>()

    override fun getChannelById(id: DiscordId<Channel>): Observable<Channel> {
        return observableLookup(id) { channelMap[it] }
    }

    override val channels: Observable<Channel>
        get() = observableList { channelMap.values }

    override fun channelCreate(e: Channels.Create.Public) {
        check(e.channel.id !in channelMap) { "Double channel creation: $e" }

        val channel = ChannelNode.from(e.channel, root)
        channelMap = channelMap.immutableAdd(channel.id, channel)
    }

    override fun guildUpdate(e: Guilds.General.Update) {
        //TODO: what needs to be updated
    }

    override fun guildMemberAdd(e: Guilds.Members.Add) {
        check(e.member.user.id !in memberMap) { "Redundant member adds: $e" }

        val newMember = MemberNode.from(e.member, this, root)
        memberMap = memberMap.immutableAdd(newMember.id, newMember)
    }

    override fun guildMemberUpdate(e: Guilds.Members.Update) = wireToUser(e.member.user.id, e)

    override fun guildMemberRemove(e: Guilds.Members.Remove) {
        check(e.member.user.id in memberMap) { "Trying to remove non-existent member: $e" }
        memberMap = memberMap.immutableRemove(e.member.user.id)
    }

    override fun guildBanAdd(e: Guilds.Bans.Add) {
        // Ignored for now
    }

    override fun guildBanRemove(e: Guilds.Bans.Remove) {
        // Ignored for now
    }

    override fun guildRoleCreate(e: Guilds.Roles.Create) {
        check(e.role.id !in roleMap) { "Duplicate role create $e" }

        val role = RoleNode.from(e.role, root)
        roleMap = roleMap.immutableAdd(role.id, role)
    }

    override fun guildRoleUpdate(e: Guilds.Roles.Update) {
        roleMap[e.role.id]!!.visit(e)
    }

    override fun guildRoleDelete(e: Guilds.Roles.Delete) {
        check(e.role_id in roleMap) { "Attempt to remove non-existent role: $e" }
        memberMap.values.forEach { it.visit(e) }
        roleMap = roleMap.immutableRemove(e.role_id)
    }

    override fun guildEmojiUpdate(e: Guilds.EmojiUpdate) {
        emojiMap = e.emojis.map { parseEmoji(it, this) }.toImmutableIdMap()
    }

    override fun guildIntegrationsUpdate(e: Guilds.IntegrationsUpdate) {
        // Only a notification, ignored for now
    }

    override fun presenceUpdate(e: Misc.PresenceUpdate) = wireToUser(e.user.id, e)

    override fun voiceStateUpdate(e: Misc.VoiceStateUpdate) {
        //FIXME: How the hell are we going to do this?
        super.voiceStateUpdate(e)
    }

    override fun wireToUser(id: DiscordId<User>, e: Event) {
        memberMap[id]?.visit(e)
    }

    override fun channelDelete(e: Channels.Delete.Public) {
        check(e.channel.id in channelMap) { "Removing non-existent channel: $e" }
        channelMap.immutableRemove(e.channel.id)
    }

    override fun toString(): String {
        return "Guild(id=$id, channels=${channelMap.values}, roles=${roleMap.values}, membersNo=${memberMap.size})"
    }

    class EmojiNode(
            val roles: List<Role>,
            override val name: String,
            override val imported: Boolean,
            override val id: DiscordId<Emoji>) : Emoji {
        override val restrictedTo: Observable<Role>
            get() = Observable.defer { Observable.from(roles) }

        override fun toString(): String {
            return "Emoji(id=$id, name='$name')"
        }
    }

    companion object {
        private fun parseEmoji(model: GuildModel.DataEmoji, guildNode: GuildNode): EmojiNode {
            return EmojiNode(
                    ImmutableList.copyOf(model.roles.map { guildNode.roleMap[it]!! }),
                    model.name,
                    model.managed,
                    model.id
            )
        }

        fun from(data: ReadyEventModel.ExtendedGuild, root: DiscordNode): GuildNode {
            val guildNode = GuildNode(root, data.id)

            guildNode.channelMap = data.channels.map { ChannelNode.from(it, root) }.toImmutableIdMap()
            guildNode.roleMap = data.roles.map { RoleNode.from(it, root) }.toImmutableIdMap()
            guildNode.memberMap = data.members.map { MemberNode.from(it, guildNode, root) }.toImmutableIdMap()
            guildNode.emojiMap = data.emojis.map { parseEmoji(it, guildNode) }.toImmutableIdMap()

            return guildNode
        }
    }
}