package net.serverpeon.discord.model

import rx.Observable

/**
 * A custom emoji that can be used by anyone with the roles listed in [restrictedTo].
 *
 * Within chat messages it is formatted as "<:[name]:[id]>" and dynamically replaced by the client.
 */
interface Emoji : DiscordId.Identifiable<Emoji> {
    /**
     * In order to use this emoji a user needs to be a member of the associated guild and have one of the roles
     * from this list of roles.
     */
    val restrictedTo: Observable<Role>
    /**
     * Public name and keyword of this emoji
     */
    val name: String
    /**
     * Whether this emoji was automatically imported from an external source by discord.
     *
     * Examples include [Twitch](http://twitch.tv) subscriber channel emotes.
     */
    val imported: Boolean
    /**
     * *Unknown*; possibly forced escaping due to conflicting keywords.
     */
    val mustBeEscaped: Boolean
}