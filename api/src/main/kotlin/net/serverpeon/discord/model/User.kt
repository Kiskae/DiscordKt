package net.serverpeon.discord.model

/**
 * Represents a user on the Discord servers
 */
interface User : DiscordId.Identifiable<User> {
    /**
     * @property username The current username of this user, this name is NOT unique.
     */
    val username: String

    /**
     * @property discriminator The discriminator for this user, used in combination with the username to determine
     *                         which user is being addressed, since people can share usernames.
     */
    val discriminator: String

    /**
     * TODO: maybe replace with ability to load the avatar?
     *
     * @property avatar Unique id that can be used to retrieve the current avatar of this user
     */
    val avatar: DiscordId<Avatar>?

    interface Avatar : DiscordId.Identifiable<Avatar>
}