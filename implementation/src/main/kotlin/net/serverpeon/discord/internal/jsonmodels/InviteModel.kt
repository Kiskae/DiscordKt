package net.serverpeon.discord.internal.jsonmodels

import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.Guild
import net.serverpeon.discord.model.Invite
import java.time.ZonedDateTime

object InviteModel {
    data class Basic(val code: DiscordId<Invite>,
                     val guild: GuildRef,
                     val xkcdpass: String?,
                     val channel: ChannelRef)

    data class Rich(val code: DiscordId<Invite>,
                    val guild: GuildRef,
                    val xkcdpass: String?,
                    val channel: ChannelRef,
                    val max_age: Int,
                    val revoked: Boolean,
                    val created_at: ZonedDateTime,
                    val temporary: Boolean,
                    val uses: Int,
                    val max_uses: Int,
                    val inviter: UserModel)

    data class GuildRef(val id: DiscordId<Guild>, val name: String)
    data class ChannelRef(val id: DiscordId<Channel>, val name: String, val type: ChannelModel.Type)
}