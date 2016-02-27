package net.serverpeon.discord.internal.data

import net.serverpeon.discord.internal.rest.data.SelfModel
import net.serverpeon.discord.internal.ws.data.inbound.Misc
import net.serverpeon.discord.message.Message
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.PostedMessage
import net.serverpeon.discord.model.User
import java.util.concurrent.CompletableFuture

class WhoamiNode(val root: DiscordNode,
                 override val id: DiscordId<User>,
                 override var username: String,
                 override var discriminator: String,
                 override var avatar: DiscordId<User.Avatar>?,
                 var email: String) : UserNode {
    override fun userUpdate(e: Misc.UserUpdate) {
        e.self.let {
            this.username = it.username
            this.discriminator = it.discriminator
            this.avatar = it.avatar
            this.email = it.email
        }
    }

    override fun toString(): String {
        return "Self(id=$id, username='$username', discriminator='$discriminator', avatar=$avatar, email='$email')"
    }

    override fun sendMessage(message: Message): CompletableFuture<PostedMessage> {
        throw IllegalStateException("Why are you trying to send a message to yourself!")
    }

    companion object {
        fun from(self: SelfModel, root: DiscordNode): WhoamiNode {
            // Note: we ignore the 'verified' node since we're logged in, so we're always verified
            return WhoamiNode(
                    root,
                    self.id,
                    self.username,
                    self.discriminator,
                    self.avatar,
                    self.email
            )
        }
    }
}