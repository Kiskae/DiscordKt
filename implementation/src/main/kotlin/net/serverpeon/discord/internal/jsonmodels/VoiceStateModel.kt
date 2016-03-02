package net.serverpeon.discord.internal.jsonmodels

import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.Guild
import net.serverpeon.discord.model.User

data class VoiceStateModel(val user_id: DiscordId<User>,
                           val suppress: Boolean,
                           val session_id: DiscordId<Session>,
                           val self_mute: Boolean,
                           val self_deaf: Boolean,
                           val mute: Boolean,
                           val deaf: Boolean,
                           val channel_id: DiscordId<Channel>?,
                           val guild_id: DiscordId<Guild>?) {
    interface Session : DiscordId.Identifiable<Session>
}