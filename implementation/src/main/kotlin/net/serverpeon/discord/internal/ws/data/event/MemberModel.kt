package net.serverpeon.discord.internal.ws.data.event

import net.serverpeon.discord.internal.rest.data.UserModel
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.Role
import java.time.ZonedDateTime

data class MemberModel(val user: UserModel,
                       val roles: List<DiscordId<Role>>?,
                       val mute: Boolean,
                       val joined_at: ZonedDateTime,
                       val deaf: Boolean)