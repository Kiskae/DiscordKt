package net.serverpeon.discord.internal.jsonmodels

import net.serverpeon.discord.model.*
import java.time.Duration
import java.time.ZonedDateTime

data class GuildModel(val features: List<String>,
                      val afk_timeout: Duration,
                      val joined_at: ZonedDateTime,
                      val afk_channel_id: DiscordId<Channel>?,
                      val id: DiscordId<Guild>,
                      val icon: String?,
                      val name: String,
                      val roles: List<RoleModel>,
                      val region: String,
                      val splash: String?,
                      val emojis: List<DataEmoji>,
                      val owner_id: DiscordId<User>) {

    data class DataEmoji(val roles: List<DiscordId<Role>>,
                         val require_colons: Boolean,
                         val name: String,
                         val managed: Boolean,
                         val id: DiscordId<Emoji>)
}