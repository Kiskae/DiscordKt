package net.serverpeon.discord.model

import rx.Observable

interface Emoji : DiscordId.Identifiable<Emoji> {
    val restrictedTo: Observable<Role>
    val name: String
    val imported: Boolean

    //TODO: require_colons -> escaping?
}