package net.serverpeon.discord.internal.ws.data.inbound

import net.serverpeon.discord.internal.data.EventInput
import net.serverpeon.discord.internal.data.model.GuildNode
import net.serverpeon.discord.internal.data.model.MemberNode
import net.serverpeon.discord.internal.data.model.RoleNode
import net.serverpeon.discord.internal.jsonmodels.MemberModel
import net.serverpeon.discord.internal.jsonmodels.ReadyEventModel
import net.serverpeon.discord.internal.jsonmodels.GuildModel
import net.serverpeon.discord.internal.jsonmodels.RoleModel
import net.serverpeon.discord.internal.jsonmodels.UserModel
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.Guild
import net.serverpeon.discord.model.Role

interface Guilds : Event {
    interface General : Guilds {
        data class Create(val guild: ReadyEventModel.ExtendedGuild) : Guilds {
            override fun <T : EventInput<T>> accept(visitor: T, handler: EventInput.Handler<T>)
                    = handler.guildCreate(visitor, this)
        }

        data class Update(val guild: GuildModel) : Guilds {
            override fun <T : EventInput<T>> accept(visitor: T, handler: EventInput.Handler<T>)
                    = handler.guildUpdate(visitor, this)
        }

        data class Delete(val guild: GuildModel) : Guilds, Event.RefHolder<GuildNode> {
            override var value: GuildNode? = null

            override fun <T : EventInput<T>> accept(visitor: T, handler: EventInput.Handler<T>)
                    = handler.guildDelete(visitor, this)
        }
    }

    interface Members : Guilds {
        data class Add(val member: MemberModel) : Members {
            override fun <T : EventInput<T>> accept(visitor: T, handler: EventInput.Handler<T>)
                    = handler.guildMemberAdd(visitor, this)
        }

        data class Update(val member: MemberModel) : Members {
            override fun <T : EventInput<T>> accept(visitor: T, handler: EventInput.Handler<T>)
                    = handler.guildMemberUpdate(visitor, this)
        }

        data class Remove(val member: MemberModel) : Members, Event.RefHolder<MemberNode> {
            override var value: MemberNode? = null

            override fun <T : EventInput<T>> accept(visitor: T, handler: EventInput.Handler<T>)
                    = handler.guildMemberRemove(visitor, this)
        }
    }

    interface Bans : Guilds {
        data class Add(val user: UserModel, val guild_id: DiscordId<Guild>) : Bans {
            override fun <T : EventInput<T>> accept(visitor: T, handler: EventInput.Handler<T>)
                    = handler.guildBanAdd(visitor, this)
        }

        data class Remove(val user: UserModel, val guild_id: DiscordId<Guild>) : Bans {
            override fun <T : EventInput<T>> accept(visitor: T, handler: EventInput.Handler<T>)
                    = handler.guildBanRemove(visitor, this)
        }
    }

    interface Roles : Guilds {
        data class Create(val role: RoleModel, val guild_id: DiscordId<Guild>) : Roles {
            override fun <T : EventInput<T>> accept(visitor: T, handler: EventInput.Handler<T>)
                    = handler.guildRoleCreate(visitor, this)
        }

        data class Update(val role: RoleModel, val guild_id: DiscordId<Guild>) : Roles {
            override fun <T : EventInput<T>> accept(visitor: T, handler: EventInput.Handler<T>)
                    = handler.guildRoleUpdate(visitor, this)
        }

        data class Delete(val role_id: DiscordId<Role>, val guild_id: DiscordId<Guild>)
        : Roles, Event.RefHolder<RoleNode> {
            override var value: RoleNode? = null

            override fun <T : EventInput<T>> accept(visitor: T, handler: EventInput.Handler<T>)
                    = handler.guildRoleDelete(visitor, this)
        }
    }

    data class EmojiUpdate(val guild_id: DiscordId<Guild>, val emojis: List<GuildModel.DataEmoji>) : Guilds {
        override fun <T : EventInput<T>> accept(visitor: T, handler: EventInput.Handler<T>)
                = handler.guildEmojiUpdate(visitor, this)
    }

    // Sent when integrated services are updated?
    data class IntegrationsUpdate(val guild_id: DiscordId<Guild>) : Guilds {
        override fun <T : EventInput<T>> accept(visitor: T, handler: EventInput.Handler<T>)
                = handler.guildIntegrationsUpdate(visitor, this)
    }
}