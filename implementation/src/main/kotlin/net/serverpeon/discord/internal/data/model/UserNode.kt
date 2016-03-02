package net.serverpeon.discord.internal.data.model

import net.serverpeon.discord.internal.data.EventInput
import net.serverpeon.discord.internal.rest.WrappedId
import net.serverpeon.discord.internal.rest.retro.Users
import net.serverpeon.discord.internal.rxObservable
import net.serverpeon.discord.internal.toFuture
import net.serverpeon.discord.internal.ws.data.inbound.Misc
import net.serverpeon.discord.message.Message
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.PostedMessage
import net.serverpeon.discord.model.User
import rx.Observable
import java.util.concurrent.CompletableFuture

interface UserNode : User, EventInput<UserNode> {
    override var username: String
    override var discriminator: String
    override var avatar: DiscordId<User.Avatar>?

    override fun handler(): EventInput.Handler<UserNode> {
        return UserEventHandler
    }

    // Handles UserNode.Profile as well as SelfNode
    private object UserEventHandler : EventInput.Handler<UserNode> {
        override fun presenceUpdate(target: UserNode, e: Misc.PresenceUpdate) {
            e.user.let {
                it.username?.let { target.username = it }
                it.discriminator?.let { target.discriminator = it }
                it.avatar?.let { target.avatar = it }
            }
        }

        override fun userUpdate(target: UserNode, e: Misc.UserUpdate) {
            if (target is SelfNode) {
                e.self.let {
                    target.username = it.username
                    target.discriminator = it.discriminator
                    target.avatar = it.avatar
                    target.email = it.email
                }
            }
        }
    }


    class Profile(val root: DiscordNode,
                  override val id: DiscordId<User>,
                  override var username: String,
                  override var discriminator: String,
                  override var avatar: DiscordId<User.Avatar>?) : UserNode {
        override fun sendMessage(message: Message): CompletableFuture<PostedMessage> {
            // Find the associated private channel or create a new one, then send a message to that channel.
            return privateChannel()
                    .toFuture()
                    .thenCompose {
                        it.sendMessage(message)
                    }
        }

        override fun toString(): String {
            return "Profile(id=$id, username='$username', discriminator='$discriminator', avatar=$avatar)"
        }


        private fun privateChannel(): Observable<Channel.Private> {
            val createNew = root.api.Users.createPrivateChannel(
                    WrappedId(root.self.id),
                    Users.PrivateChannelCreate(id)
            ).rxObservable().map {
                Builder.channel(it, root)
            }

            val getCurrent = root.privateChannels.filter {
                it.recipient.id == id
            }

            return getCurrent.switchIfEmpty(createNew)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is UserNode) return false

            if (id != other.id) return false

            return true
        }

        override fun hashCode(): Int {
            return id.hashCode()
        }
    }
}