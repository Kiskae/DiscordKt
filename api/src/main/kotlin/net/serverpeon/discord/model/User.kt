package net.serverpeon.discord.model

import net.serverpeon.discord.message.Message
import java.util.concurrent.CompletableFuture

/**
 * Represents a user on the Discord servers
 */
interface User : DiscordId.Identifiable<User> {
    /**
     * The current username of this user, this name is NOT unique.
     */
    val username: String

    /**
     * The discriminator for this user, used in combination with the username to determine
     * which user is being addressed, since people can share usernames.
     */
    val discriminator: String

    /**
     * TODO: maybe replace with ability to load the avatar?
     *
     * Unique id that can be used to retrieve the current avatar of this user
     */
    val avatar: DiscordId<Avatar>?

    /**
     * Sends a private message to this user.
     *
     * If there is not yet a private channel between the client and this user then that channel will be created.
     */
    fun sendMessage(message: Message): CompletableFuture<PostedMessage>

    interface Avatar : DiscordId.Identifiable<Avatar>
}