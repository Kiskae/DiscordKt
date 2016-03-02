package net.serverpeon.discord.internal.jsonmodels

import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.PermissionSet
import net.serverpeon.discord.model.Role
import java.awt.Color

data class RoleModel(val color: Color,
                     val hoist: Boolean,
                     val id: DiscordId<Role>,
                     val managed: Boolean,
                     val name: String,
                     val permissions: PermissionSet,
                     val position: Int)