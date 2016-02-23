package net.serverpeon.discord.internal.ws.data.inbound

import net.serverpeon.discord.internal.rest.data.UserModel
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.Guild
import net.serverpeon.discord.model.Role
import java.time.ZonedDateTime

/**
 * @property guild_id Only present in GuildMember* events
 */
data class MemberModel(val user: UserModel,
                       val roles: List<DiscordId<Role>>?,
                       val mute: Boolean,
                       val joined_at: ZonedDateTime,
                       val deaf: Boolean,
                       val guild_id: DiscordId<Guild>?)