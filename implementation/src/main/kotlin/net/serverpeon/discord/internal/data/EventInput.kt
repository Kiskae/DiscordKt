package net.serverpeon.discord.internal.data

import net.serverpeon.discord.internal.ws.data.inbound.Channels
import net.serverpeon.discord.internal.ws.data.inbound.Event
import net.serverpeon.discord.internal.ws.data.inbound.Guilds
import net.serverpeon.discord.internal.ws.data.inbound.Misc
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.Guild

interface EventInput<T : EventInput<T>> {
    fun handler(): Handler<T>

    fun handle(event: Event) {
        @Suppress("UNCHECKED_CAST")
        event.accept(this as T, handler())
    }

    fun wireToGuild(id: DiscordId<Guild>, event: Event) = noop()
    fun wireToChannel(id: DiscordId<Channel>, event: Event) = noop()

    interface Handler<T : EventInput<T>> {
        fun channelCreate(target: T, e: Channels.Create.Public) = target.wireToGuild(e.channel.guild_id!!, e)
        fun channelCreate(target: T, e: Channels.Create.Private) = noop()

        fun channelUpdate(target: T, e: Channels.Update) = target.wireToChannel(e.channel.id, e)
        fun channelDelete(target: T, e: Channels.Delete.Public) = target.wireToGuild(e.channel.guild_id!!, e)
        fun channelDelete(target: T, e: Channels.Delete.Private) = noop()

        fun guildCreate(target: T, e: Guilds.General.Create) = noop()

        fun guildUpdate(target: T, e: Guilds.General.Update) = target.wireToGuild(e.guild.id, e)
        fun guildDelete(target: T, e: Guilds.General.Delete) = target.wireToGuild(e.guild.id, e)
        fun guildMemberAdd(target: T, e: Guilds.Members.Add) = target.wireToGuild(e.member.guild_id!!, e)
        fun guildMemberUpdate(target: T, e: Guilds.Members.Update) = target.wireToGuild(e.member.guild_id!!, e)
        fun guildMemberRemove(target: T, e: Guilds.Members.Remove) = target.wireToGuild(e.member.guild_id!!, e)
        fun guildBanAdd(target: T, e: Guilds.Bans.Add) = target.wireToGuild(e.guild_id, e)
        fun guildBanRemove(target: T, e: Guilds.Bans.Remove) = target.wireToGuild(e.guild_id, e)
        fun guildRoleCreate(target: T, e: Guilds.Roles.Create) = target.wireToGuild(e.guild_id, e)
        fun guildRoleUpdate(target: T, e: Guilds.Roles.Update) = target.wireToGuild(e.guild_id, e)
        fun guildRoleDelete(target: T, e: Guilds.Roles.Delete) = target.wireToGuild(e.guild_id, e)
        fun guildEmojiUpdate(target: T, e: Guilds.EmojiUpdate) = target.wireToGuild(e.guild_id, e)
        fun guildIntegrationsUpdate(target: T, e: Guilds.IntegrationsUpdate) = target.wireToGuild(e.guild_id, e)

        fun userUpdate(target: T, e: Misc.UserUpdate) = noop()

        fun ready(target: T, e: Misc.Ready) = noop()
        fun resumed(target: T, e: Misc.Resumed) = noop()
        fun guildMemberChunks(target: T, e: Misc.MembersChunk) = target.wireToGuild(e.guild_id, e)

        fun typingStart(target: T, e: Misc.TypingStart) = target.wireToChannel(e.channel_id, e)
        fun presenceUpdate(target: T, e: Misc.PresenceUpdate) = target.wireToGuild(e.guild_id, e)
        fun voiceStateUpdate(target: T, e: Misc.VoiceStateUpdate) {
            if (e.update.guild_id != null) {
                target.wireToGuild(e.update.guild_id, e)
            } else {
                target.wireToChannel(e.update.channel_id!!, e)
            }
        }
    }

    companion object {
        private fun noop() {

        }
    }
}