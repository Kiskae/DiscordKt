package net.serverpeon.discord.internal.data

import net.serverpeon.discord.internal.rest.data.RoleModel
import net.serverpeon.discord.internal.ws.data.inbound.Event
import net.serverpeon.discord.internal.ws.data.inbound.Guilds
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.Role

class RoleNode(override val id: DiscordId<Role>) : Role, Event.Visitor {

    override fun guildRoleUpdate(e: Guilds.Roles.Update) {
        //TODO
    }

    override fun toString(): String {
        return "Role(id=$id)"
    }

    companion object {
        fun from(model: RoleModel, root: DiscordNode): RoleNode {
            return RoleNode(model.id)
        }
    }
}