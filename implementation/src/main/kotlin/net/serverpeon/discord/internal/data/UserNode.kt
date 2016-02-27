package net.serverpeon.discord.internal.data

import net.serverpeon.discord.internal.rest.data.UserModel
import net.serverpeon.discord.internal.rest.data.WrappedId
import net.serverpeon.discord.internal.rest.retro.Users
import net.serverpeon.discord.internal.rxObservable
import net.serverpeon.discord.internal.toFuture
import net.serverpeon.discord.internal.ws.data.inbound.Event
import net.serverpeon.discord.internal.ws.data.inbound.Misc
import net.serverpeon.discord.message.Message
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.PostedMessage
import net.serverpeon.discord.model.User
import java.util.concurrent.CompletableFuture

interface UserNode : User, Event.Visitor {
    override var username: String
    override var discriminator: String
    override var avatar: DiscordId<User.Avatar>?

    companion object {
        fun from(data: UserModel, node: DiscordNode): UserNode {
            return Profile(
                    node,
                    data.id,
                    data.username,
                    data.discriminator,
                    data.avatar
            )
        }
    }

    class Profile(val root: DiscordNode,
                  override val id: DiscordId<User>,
                  override var username: String,
                  override var discriminator: String,
                  override var avatar: DiscordId<User.Avatar>?) : UserNode {
        override fun sendMessage(message: Message): CompletableFuture<PostedMessage> {
            // Find the associated private channel or create a new one, then send a message to that channel.
            return root.privateChannels.filter { it.recipient.id == id }.switchIfEmpty(root.api.Users.createPrivateChannel(
                    WrappedId(root.self.id),
                    Users.PrivateChannelCreate(id)
            ).rxObservable().map { ChannelNode.from(it, root) })
                    .toFuture()
                    .thenCompose {
                        it.sendMessage(message)
                    }
        }

        override fun presenceUpdate(e: Misc.PresenceUpdate) {
            e.user.let {
                it.username?.let { this.username = it }
                it.discriminator?.let { this.discriminator = it }
                it.avatar?.let { this.avatar = it }
            }
        }

        override fun toString(): String {
            return "Profile(id=$id, username='$username', discriminator='$discriminator', avatar=$avatar)"
        }
    }
}