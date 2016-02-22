package net.serverpeon.discord.internal.data

import net.serverpeon.discord.internal.rest.data.RoleModel
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.Role

class RoleNode(override val id: DiscordId<Role>) : Role {
    companion object {
        fun from(model: RoleModel, root: DiscordNode): RoleNode {
            return RoleNode(model.id)
        }
    }
}