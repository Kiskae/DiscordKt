package net.serverpeon.discord.internal.ws.data.inbound

import com.google.gson.JsonElement
import net.serverpeon.discord.internal.rest.data.ChannelModel
import net.serverpeon.discord.internal.rest.data.GuildModel
import net.serverpeon.discord.internal.rest.data.RoleModel
import net.serverpeon.discord.internal.rest.data.SelfModel
import net.serverpeon.discord.model.*
import java.time.Duration
import java.time.ZonedDateTime

data class ReadyEventModel(val v: Int,
                           val user_settings: UserSettings,
                           val user_guild_settings: List<UserGuildSettings>,
                           val user: SelfModel,
                           val tutorial: JsonElement?,
                           val session_id: String,
                           val read_state: List<ReadState>,
                           val private_channels: List<PrivateChannelModel>,
                           val heartbeat_interval: Long,
                           val guilds: List<ExtendedGuild>) {

    data class UserSettings(val theme: String,
                            val show_current_game: Boolean,
                            val render_embeds: Boolean,
                            val message_display_compact: Boolean,
                            val locale: String,
                            val inline_embed_media: Boolean,
                            val inline_attachment_media: Boolean,
                            val enable_tts_command: Boolean,
                            val convert_emoticons: Boolean)

    data class UserGuildSettings(val suppress_everyone: Boolean,
                                 val muted: Boolean,
                                 val mobile_push: Boolean,
                                 val message_notifications: Int,
                                 val guild_id: DiscordId<Guild>,
                                 val channel_overrides: List<ChannelOverrides>) {
        data class ChannelOverrides(val muted: Boolean,
                                    val message_notifications: Int,
                                    val channel_id: DiscordId<Channel>)
    }

    data class ReadState(val mention_count: Int,
                         val last_message_id: DiscordId<Message>,
                         val id: DiscordId<Channel>)

    data class ExtendedGuild(val voice_states: List<VoiceStateModel>,
                             val verification_level: Int,
                             val features: List<String>,
                             val afk_timeout: Duration,
                             val joined_at: ZonedDateTime,
                             val afk_channel_id: DiscordId<Channel>?,
                             val id: DiscordId<Guild>,
                             val icon: String?,
                             val name: String,
                             val roles: List<RoleModel>,
                             val region: String,
                             val splash: String?,
                             val emojis: List<GuildModel.DataEmoji>,
                             val owner_id: DiscordId<User>,
                             val presences: List<Presence>,
                             val members: List<MemberModel>,
                             val member_count: Int,
                             val large: Boolean,
                             val channels: List<ChannelModel>) {

        data class Presence(val user: Ref,
                            val status: Misc.PresenceUpdate.Status,
                            val game: Game) {
            data class Ref(val id: DiscordId<User>)
            data class Game(val name: String)
        }

    }

}