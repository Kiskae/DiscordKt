package net.serverpeon.discord.internal.ws.data.inbound

import net.serverpeon.discord.internal.rest.data.GuildModel
import net.serverpeon.discord.internal.rest.data.RoleModel
import net.serverpeon.discord.internal.rest.data.UserModel
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.Guild
import net.serverpeon.discord.model.Role

interface Guilds : Event {
    interface General : Guilds {
        data class Create(val guild: ReadyEventModel.ExtendedGuild) : Guilds {
            override fun accept(visitor: Event.Visitor) = visitor.guildCreate(this)
        }

        data class Update(val guild: GuildModel) : Guilds {
            override fun accept(visitor: Event.Visitor) = visitor.guildUpdate(this)
        }

        data class Delete(val guild: GuildModel) : Guilds {
            override fun accept(visitor: Event.Visitor) = visitor.guildDelete(this)
        }
    }

    interface Members : Guilds {
        data class Add(val member: MemberModel) : Members {
            override fun accept(visitor: Event.Visitor) = visitor.guildMemberAdd(this)
        }

        data class Update(val member: MemberModel) : Members {
            override fun accept(visitor: Event.Visitor) = visitor.guildMemberUpdate(this)
        }

        data class Remove(val member: MemberModel) : Members {
            override fun accept(visitor: Event.Visitor) = visitor.guildMemberRemove(this)
        }
    }

    interface Bans : Guilds {
        data class Add(val user: UserModel, val guild_id: DiscordId<Guild>) : Bans {
            override fun accept(visitor: Event.Visitor) = visitor.guildBanAdd(this)
        }

        data class Remove(val user: UserModel, val guild_id: DiscordId<Guild>) : Bans {
            override fun accept(visitor: Event.Visitor) = visitor.guildBanRemove(this)
        }
    }

    interface Roles : Guilds {
        data class Create(val role: RoleModel, val guild_id: DiscordId<Guild>) : Roles {
            override fun accept(visitor: Event.Visitor) = visitor.guildRoleCreate(this)
        }

        data class Update(val role: RoleModel, val guild_id: DiscordId<Guild>) : Roles {
            override fun accept(visitor: Event.Visitor) = visitor.guildRoleUpdate(this)
        }

        data class Delete(val role_id: DiscordId<Role>, val guild_id: DiscordId<Guild>) : Roles {
            override fun accept(visitor: Event.Visitor) = visitor.guildRoleDelete(this)
        }
    }

    data class EmojiUpdate(val guild_id: DiscordId<Guild>, val emojis: List<GuildModel.DataEmoji>) : Guilds {
        override fun accept(visitor: Event.Visitor) = visitor.guildEmojiUpdate(this)
    }

    // Sent when integrated services are updated?
    data class IntegrationsUpdate(val guild_id: DiscordId<Guild>) : Guilds {
        override fun accept(visitor: Event.Visitor) = visitor.guildIntegrationsUpdate(this)
    }
}