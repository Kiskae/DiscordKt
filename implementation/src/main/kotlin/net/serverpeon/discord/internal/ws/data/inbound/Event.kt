package net.serverpeon.discord.internal.ws.data.inbound

import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.Guild
import net.serverpeon.discord.model.User

interface Event {
    interface Visitor {
        fun visit(e: Event) {
            e.accept(this)
        }

        fun unhandled(e: Event) {

        }

        fun wireToGuild(id: DiscordId<Guild>, e: Event) = unhandled(e)
        fun wireToChannel(id: DiscordId<Channel>, e: Event) = unhandled(e)
        fun wireToUser(id: DiscordId<User>, e: Event) = unhandled(e)

        fun channelCreate(e: Channels.Create.Public) = wireToGuild(e.channel.guild_id!!, e)
        fun channelCreate(e: Channels.Create.Private) = unhandled(e)
        fun channelUpdate(e: Channels.Update) = wireToChannel(e.channel.id, e)
        fun channelDelete(e: Channels.Delete.Public) = wireToGuild(e.channel.guild_id!!, e)
        fun channelDelete(e: Channels.Delete.Private) = unhandled(e)

        fun guildCreate(e: Guilds.General.Create) = unhandled(e)
        fun guildUpdate(e: Guilds.General.Update) = wireToGuild(e.guild.id, e)
        fun guildDelete(e: Guilds.General.Delete) = wireToGuild(e.guild.id, e)
        fun guildMemberAdd(e: Guilds.Members.Add) = wireToGuild(e.member.guild_id!!, e)
        fun guildMemberUpdate(e: Guilds.Members.Update) = wireToGuild(e.member.guild_id!!, e)
        fun guildMemberRemove(e: Guilds.Members.Remove) = wireToGuild(e.member.guild_id!!, e)
        fun guildBanAdd(e: Guilds.Bans.Add) = wireToGuild(e.guild_id, e)
        fun guildBanRemove(e: Guilds.Bans.Remove) = wireToGuild(e.guild_id, e)
        fun guildRoleCreate(e: Guilds.Roles.Create) = wireToGuild(e.guild_id, e)
        fun guildRoleUpdate(e: Guilds.Roles.Update) = wireToGuild(e.guild_id, e)
        fun guildRoleDelete(e: Guilds.Roles.Delete) = wireToGuild(e.guild_id, e)
        fun guildEmojiUpdate(e: Guilds.EmojiUpdate) = wireToGuild(e.guild_id, e)
        fun guildIntegrationsUpdate(e: Guilds.IntegrationsUpdate) = wireToGuild(e.guild_id, e)

        //TODO: are messages relevant?

        fun userUpdate(e: Misc.UserUpdate) = unhandled(e)
        fun ready(e: Misc.Ready) = unhandled(e)
        fun typingStart(e: Misc.TypingStart) = wireToChannel(e.channel_id, e)
        fun presenceUpdate(e: Misc.PresenceUpdate) = wireToGuild(e.guild_id, e)
        fun voiceStateUpdate(e: Misc.VoiceStateUpdate) {
            if (e.update.guild_id != null) {
                wireToGuild(e.update.guild_id, e)
            } else {
                wireToChannel(e.update.channel_id!!, e)
            }
        }
    }

    interface RefHolder<T> {
        var value: T?
    }

    fun accept(visitor: Visitor)
}