package net.serverpeon.discord.internal.ws.data.inbound

import net.serverpeon.discord.internal.rest.data.SelfModel
import net.serverpeon.discord.model.*

interface Misc {
    data class UserUpdate(val self: SelfModel) : Misc
    data class Ready(val data: ReadyEventModel) : Misc
    data class TypingStart(val user_id: DiscordId<User>,
                           val timestamp: Long,
                           val channel_id: DiscordId<Channel>)

    data class PresenceUpdate(val user: UserRef,
                              val status: String,
                              val roles: List<DiscordId<Role>>,
                              val guild_id: DiscordId<Guild>,
                              val game: Playing?) {
        data class UserRef(val id: DiscordId<User>)
        data class Playing(val name: String)
    }

    data class VoiceStateUpdate(val update: VoiceStateModel)
}