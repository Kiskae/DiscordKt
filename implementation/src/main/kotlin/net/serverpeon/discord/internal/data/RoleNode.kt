package net.serverpeon.discord.internal.data

import net.serverpeon.discord.internal.rest.data.RoleModel
import net.serverpeon.discord.internal.ws.data.inbound.Event
import net.serverpeon.discord.internal.ws.data.inbound.Guilds
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.PermissionSet
import net.serverpeon.discord.model.Role
import java.awt.Color

class RoleNode(private val root: DiscordNode,
               override val id: DiscordId<Role>,
               override var name: String,
               override val imported: Boolean,
               override var grouped: Boolean,
               override var permissions: PermissionSet,
               override var color: Color,
               var position: Int) : Role, Event.Visitor {

    override fun guildRoleUpdate(e: Guilds.Roles.Update) {
        e.role.let {
            color = it.color
            permissions = it.permissions
            grouped = it.hoist
            name = it.name
            position = it.position
        }
    }

    override fun toString(): String {
        return "Role(id=$id, name='$name', imported=$imported, grouped=$grouped, permissions=$permissions, color=$color)"
    }

    companion object {
        fun from(model: RoleModel, root: DiscordNode): RoleNode {
            println("${model.name} - ${model.position}")
            return RoleNode(root,
                    model.id,
                    model.name,
                    model.managed,
                    model.hoist,
                    model.permissions,
                    model.color,
                    model.position);
        }
    }
}