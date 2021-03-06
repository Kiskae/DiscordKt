package net.serverpeon.discord.internal.ws.data.inbound

import com.google.gson.annotations.SerializedName
import net.serverpeon.discord.internal.data.EventInput
import net.serverpeon.discord.internal.jsonmodels.*
import net.serverpeon.discord.model.*

interface Misc : Event {
    data class UserUpdate(val self: SelfModel) : Misc {
        override fun <T : EventInput<T>> accept(visitor: T, handler: EventInput.Handler<T>)
                = handler.userUpdate(visitor, this)
    }

    data class Ready(val data: ReadyEventModel) : Misc {
        override fun <T : EventInput<T>> accept(visitor: T, handler: EventInput.Handler<T>)
                = handler.ready(visitor, this)
    }

    data class MembersChunk(val members: List<MemberModel>, val guild_id: DiscordId<Guild>) : Misc {
        override fun <T : EventInput<T>> accept(visitor: T, handler: EventInput.Handler<T>)
                = handler.guildMemberChunks(visitor, this)
    }

    data class Resumed(val heartbeat_interval: Long) : Misc {
        override fun <T : EventInput<T>> accept(visitor: T, handler: EventInput.Handler<T>)
                = handler.resumed(visitor, this)
    }

    data class TypingStart(val user_id: DiscordId<User>,
                           val timestamp: Long,
                           val channel_id: DiscordId<Channel>) : Misc {
        override fun <T : EventInput<T>> accept(visitor: T, handler: EventInput.Handler<T>)
                = handler.typingStart(visitor, this)
    }

    /**
     * If the presence update is for a relationship instead of a guild member, 'roles' and 'guild_id' will be null
     */
    data class PresenceUpdate(val user: UserRef,
                              val status: Status,
                              val roles: List<DiscordId<Role>>?,
                              val guild_id: DiscordId<Guild>?,
                              val game: Playing?) : Misc {

        override fun <T : EventInput<T>> accept(visitor: T, handler: EventInput.Handler<T>)
                = handler.presenceUpdate(visitor, this)

        /**
         * Includes username, discriminator, bot and avatar if one of those changes (or it seems at random other places)
         * Should be used to update the user model
         */
        data class UserRef(val id: DiscordId<User>,
                           val username: String?,
                           val discriminator: String?,
                           val avatar: DiscordId<User.Avatar>?,
                           val bot: Boolean?) {
            fun toUserModel(): UserModel {
                require(username != null)
                require(discriminator != null)
                return UserModel(username!!, id, discriminator!!, avatar, bot)
            }
        }

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
        override fun <T : EventInput<T>> accept(visitor: T, handler: EventInput.Handler<T>)
                = handler.voiceStateUpdate(visitor, this)
    }
}