package net.serverpeon.discord.internal.ws.client

import com.google.gson.Gson
import net.serverpeon.discord.internal.ws.data.inbound.Channels
import net.serverpeon.discord.internal.ws.data.inbound.Guilds
import net.serverpeon.discord.internal.ws.data.inbound.Messages
import net.serverpeon.discord.internal.ws.data.inbound.Misc

internal object DiscordHandlers {
    fun create(gson: Gson): MessageTranslator {
        return MessageTranslator.Builder(gson).apply {
            registerType("CHANNEL_CREATE") { data ->
                if (data.asJsonObject.has("recipient")) {
                    Channels.Create.Private(data.parse())
                } else {
                    Channels.Create.Public(data.parse())
                }
            }
            registerType("CHANNEL_UPDATE") { Channels.Update(it.parse()) }
            registerType("CHANNEL_DELETE") { data ->
                if (data.asJsonObject.has("recipient")) {
                    Channels.Delete.Private(data.parse())
                } else {
                    Channels.Delete.Public(data.parse())
                }
            }
            registerType("MESSAGE_CREATE") { Messages.Create(it.parse()) }
            registerType("MESSAGE_UPDATE") { Messages.Update(it.parse()) }
            registerType("MESSAGE_DELETE") { it.parse<Messages.Delete>() }
            registerType("MESSAGE_ACK") { it.parse<Messages.Acknowledge>() }
            registerType("GUILD_CREATE") { Guilds.Create(it.parse()) }
            registerType("GUILD_UPDATE") { Guilds.Update(it.parse()) }
            registerType("GUILD_DELETE") { Guilds.Delete(it.parse()) }
            registerType("GUILD_MEMBER_ADD") { Guilds.Members.Add(it.parse()) }
            registerType("GUILD_MEMBER_UPDATE") { Guilds.Members.Update(it.parse()) }
            registerType("GUILD_MEMBER_REMOVE") { Guilds.Members.Remove(it.parse()) }
            registerType("GUILD_BAN_ADD") { it.parse<Guilds.Bans.Add>() }
            registerType("GUILD_BAN_REMOVE") { it.parse<Guilds.Bans.Remove>() }
            registerType("GUILD_ROLE_CREATE") { it.parse<Guilds.Roles.Create>() }
            registerType("GUILD_ROLE_UPDATE") { it.parse<Guilds.Roles.Update>() }
            registerType("GUILD_ROLE_DELETE") { it.parse<Guilds.Roles.Delete>() }
            registerType("USER_UPDATE") { Misc.UserUpdate(it.parse()) }
            registerType("READY") { Misc.Ready(it.parse()) }
            registerType("TYPING_START") { it.parse<Misc.TypingStart>() }
            registerType("PRESENCE_UPDATE") { it.parse<Misc.PresenceUpdate>() }
            registerType("GUILD_EMOJIS_UPDATE") { it.parse<Guilds.EmojiUpdate>() }
            registerType("GUILD_INTEGRATIONS_UPDATE") { it.parse<Guilds.IntegrationsUpdate>() }
            registerType("VOICE_STATE_UPDATE") { Misc.VoiceStateUpdate(it.parse()) }
        }.build()
    }
}