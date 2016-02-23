package net.serverpeon.discord.internal.ws.data.inbound

import net.serverpeon.discord.internal.rest.data.GuildModel
import net.serverpeon.discord.internal.rest.data.RoleModel
import net.serverpeon.discord.internal.rest.data.UserModel
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.Guild
import net.serverpeon.discord.model.Role

interface Guilds {
    val id: DiscordId<Guild>

    interface General : Guilds {
        data class Create(val guild: ReadyEventModel.ExtendedGuild) : Guilds {
            override val id: DiscordId<Guild>
                get() = guild.id
        }

        data class Update(val guild: GuildModel) : Guilds {
            override val id: DiscordId<Guild>
                get() = guild.id
        }

        data class Delete(val guild: GuildModel) : Guilds {
            override val id: DiscordId<Guild>
                get() = guild.id
        }
    }

    interface Members : Guilds {
        data class Add(val member: MemberModel) : Members {
            override val id: DiscordId<Guild>
                get() = member.guild_id!!
        }

        data class Update(val member: MemberModel) : Members {
            override val id: DiscordId<Guild>
                get() = member.guild_id!!
        }

        data class Remove(val member: MemberModel) : Members {
            override val id: DiscordId<Guild>
                get() = member.guild_id!!
        }
    }

    interface Bans : Guilds {
        data class Add(val user: UserModel, val guild_id: DiscordId<Guild>) : Bans {
            override val id: DiscordId<Guild>
                get() = guild_id
        }

        data class Remove(val user: UserModel, val guild_id: DiscordId<Guild>) : Bans {
            override val id: DiscordId<Guild>
                get() = guild_id
        }
    }

    interface Roles : Guilds {
        data class Create(val role: RoleModel, val guild_id: DiscordId<Guild>) : Roles {
            override val id: DiscordId<Guild>
                get() = guild_id
        }

        data class Update(val role: RoleModel, val guild_id: DiscordId<Guild>) : Roles {
            override val id: DiscordId<Guild>
                get() = guild_id
        }

        data class Delete(val role_id: DiscordId<Role>, val guild_id: DiscordId<Guild>) : Roles {
            override val id: DiscordId<Guild>
                get() = guild_id
        }
    }

    data class EmojiUpdate(val guild_id: DiscordId<Guild>, val emojis: List<GuildModel.DataEmoji>) : Guilds {
        override val id: DiscordId<Guild>
            get() = guild_id
    }

    // Sent when integrated services are updated?
    data class IntegrationsUpdate(val guild_id: DiscordId<Guild>) : Guilds {
        override val id: DiscordId<Guild>
            get() = guild_id
    }
}