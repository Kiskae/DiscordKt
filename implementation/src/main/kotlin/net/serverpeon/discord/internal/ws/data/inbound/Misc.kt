package net.serverpeon.discord.internal.ws.data.inbound

import com.google.gson.annotations.SerializedName
import net.serverpeon.discord.internal.rest.data.SelfModel
import net.serverpeon.discord.model.*

interface Misc : Event {
    data class UserUpdate(val self: SelfModel) : Misc {
        override fun accept(visitor: Event.Visitor) = visitor.userUpdate(this)
    }

    data class Ready(val data: ReadyEventModel) : Misc {
        override fun accept(visitor: Event.Visitor) = visitor.ready(this)
    }

    data class TypingStart(val user_id: DiscordId<User>,
                           val timestamp: Long,
                           val channel_id: DiscordId<Channel>) : Misc {
        override fun accept(visitor: Event.Visitor) = visitor.typingStart(this)
    }

    data class PresenceUpdate(val user: UserRef,
                              val status: Status,
                              val roles: List<DiscordId<Role>>,
                              val guild_id: DiscordId<Guild>,
                              val game: Playing?) : Misc {

        override fun accept(visitor: Event.Visitor) = visitor.presenceUpdate(this)

        /**
         * Includes username, discriminator and avatar if one of those changes (or it seems at random other places)
         * Should be used to update the user model
         */
        data class UserRef(val id: DiscordId<User>,
                           val username: String?,
                           val discriminator: String?,
                           val avatar: DiscordId<User.Avatar>?)

        data class Playing(val name: String)

        enum class Status {
            @SerializedName("online")
            ONLINE,
            @SerializedName("offline")
            OFFLINE,
            @SerializedName("idle")
            IDLE
        }
    }

    data class VoiceStateUpdate(val update: VoiceStateModel) : Misc {
        override fun accept(visitor: Event.Visitor) = visitor.voiceStateUpdate(this)
    }
}