package net.serverpeon.discord.model

import java.time.Instant
import java.time.ZonedDateTime
import java.util.concurrent.CompletableFuture

interface Invite : DiscordId.Identifiable<Invite> {
    val guild: GuildSpec

    data class GuildSpec(val id: DiscordId<Guild>, val name: String)

    val channel: ChannelSpec

    data class ChannelSpec(val id: DiscordId<Channel>, val name: String, val type: Channel.Type)

    val xkcd: DiscordId<Invite>?

    fun accept(): CompletableFuture<Void>

    interface Details : Invite {
        val inviter: User

        val revoked: Boolean

        val temporary: Boolean

        val createdAt: ZonedDateTime

        val uses: Int

        val maxUses: Int

        val expiresAt: Instant?
    }
}