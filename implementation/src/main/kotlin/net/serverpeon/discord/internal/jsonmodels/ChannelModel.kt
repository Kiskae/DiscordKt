package net.serverpeon.discord.internal.jsonmodels

import com.google.gson.annotations.SerializedName
import net.serverpeon.discord.model.*

data class ChannelModel(val guild_id: DiscordId<Guild>?,
                        val name: String,
                        val permission_overwrites: List<PermissionOverwrites>,
                        val topic: String?,
                        val position: Int,
                        val last_message_id: DiscordId<PostedMessage>?,
                        val type: Type,
                        val id: DiscordId<Channel>,
                        val is_private: Boolean) {
    enum class Type {
        @SerializedName("text")
        TEXT,
        @SerializedName("voice")
        VOICE
    }

    data class PermissionOverwrites(val type: String,
                                    val id: String,
                                    val deny: PermissionSet,
                                    val allow: PermissionSet)
}