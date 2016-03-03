package net.serverpeon.discord.internal.data.model

import net.serverpeon.discord.message.Message
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.PostedMessage
import net.serverpeon.discord.model.User
import java.util.concurrent.CompletableFuture

class SelfNode(val root: DiscordNode,
               override val id: DiscordId<User>,
               override var username: String,
               override var discriminator: String,
               override var avatar: DiscordId<User.Avatar>?,
               var email: String) : UserNode {
    override fun toString(): String {
        return "Self(id=$id, username='$username', discriminator='$discriminator', avatar=$avatar)"
    }

    override fun sendMessage(message: Message): CompletableFuture<PostedMessage> {
        throw IllegalStateException("Why are you trying to send a message to yourself!")
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