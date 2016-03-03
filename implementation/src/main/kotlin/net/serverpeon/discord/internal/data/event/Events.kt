package net.serverpeon.discord.internal.data.event

import net.serverpeon.discord.event.ban.BanCreateEvent
import net.serverpeon.discord.event.ban.BanRemoveEvent
import net.serverpeon.discord.event.channel.ChannelCreateEvent
import net.serverpeon.discord.event.channel.ChannelDeleteEvent
import net.serverpeon.discord.event.channel.ChannelUpdateEvent
import net.serverpeon.discord.event.guild.GuildCreateEvent
import net.serverpeon.discord.event.guild.GuildDeleteEvent
import net.serverpeon.discord.event.guild.GuildUpdateEvent
import net.serverpeon.discord.event.member.MemberJoinedEvent
import net.serverpeon.discord.event.member.MemberLeftEvent
import net.serverpeon.discord.event.member.MemberUpdateEvent
import net.serverpeon.discord.event.message.MessageCreateEvent
import net.serverpeon.discord.event.message.MessageDeleteEvent
import net.serverpeon.discord.event.message.MessageEditEvent
import net.serverpeon.discord.event.message.MessageEmbedEvent
import net.serverpeon.discord.event.role.RoleCreateEvent
import net.serverpeon.discord.event.role.RoleDeleteEvent
import net.serverpeon.discord.event.role.RoleUpdateEvent
import net.serverpeon.discord.model.*

data class BanAdded(override val guild: Guild, override val user: User) : BanCreateEvent
data class BanRemoved(override val guild: Guild, override val user: User) : BanRemoveEvent

data class ChannelCreatePublic(override val channel: Channel.Public) : ChannelCreateEvent.Public
data class ChannelCreatePrivate(override val channel: Channel.Private) : ChannelCreateEvent.Private
data class ChannelUpdate(override val channel: Channel.Public) : ChannelUpdateEvent
data class ChannelDeletePublic(override val deletedChannel: Channel.Public) : ChannelDeleteEvent.Public
data class ChannelDeletePrivate(override val deletedChannel: Channel.Private) : ChannelDeleteEvent.Private

data class GuildCreate(override val guild: Guild) : GuildCreateEvent
data class GuildUpdate(override val guild: Guild) : GuildUpdateEvent
data class GuildDelete(override val deletedGuild: Guild) : GuildDeleteEvent

data class MemberJoined(override val member: Guild.Member) : MemberJoinedEvent
data class MemberUpdate(override val member: Guild.Member) : MemberUpdateEvent
data class MemberLeft(override val formerMember: Guild.Member) : MemberLeftEvent

data class MessageCreated(override val message: PostedMessage) : MessageCreateEvent
data class MessageEdited(override val message: PostedMessage) : MessageEditEvent
data class MessageEmbed(override val message: PostedMessage, override val embeds: List<Any>) : MessageEmbedEvent

data class MessageDeleted(
        override val channel: Channel.Text,
        override val deletedMessageId: DiscordId<PostedMessage>
) : MessageDeleteEvent

data class RoleAdded(override val role: Role, override val guild: Guild) : RoleCreateEvent
data class RoleEdited(override val role: Role, override val guild: Guild) : RoleUpdateEvent
data class RoleDeleted(override val deletedRole: Role, override val guild: Guild) : RoleDeleteEvent