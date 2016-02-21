package net.serverpeon.discord.internal.ws.data.inbound

import net.serverpeon.discord.internal.rest.data.GuildModel
import net.serverpeon.discord.internal.rest.data.RoleModel
import net.serverpeon.discord.internal.rest.data.UserModel
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.Guild
import net.serverpeon.discord.model.Role

interface Guilds {
    data class Create(val guild: GuildModel) : Guilds
    data class Update(val guild: GuildModel) : Guilds
    data class Delete(val guild: GuildModel) : Guilds

    interface Members {
        data class Add(val member: MemberModel) : Members
        data class Update(val member: MemberModel) : Members
        data class Remove(val member: MemberModel) : Members
    }

    interface Bans {
        data class Add(val user: UserModel, val guild_id: DiscordId<Guild>) : Bans
        data class Remove(val user: UserModel, val guild_id: DiscordId<Guild>) : Bans
    }

    interface Roles {
        data class Create(val role: RoleModel, val guild_id: DiscordId<Guild>) : Roles
        data class Update(val role: RoleModel, val guild_id: DiscordId<Guild>) : Roles
        data class Delete(val role_id: DiscordId<Role>, val guild_id: DiscordId<Guild>) : Roles
    }

    data class EmojiUpdate(val guild_id: DiscordId<Guild>, val emojis: List<GuildModel.DataEmoji>)

    // Sent when integrated services are updated?
    data class IntegrationsUpdate(val guild_id: DiscordId<Guild>)
}