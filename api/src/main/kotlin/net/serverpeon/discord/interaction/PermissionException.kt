package net.serverpeon.discord.interaction

import net.serverpeon.discord.model.PermissionSet

class PermissionException(val perm: PermissionSet.Permission)
: RuntimeException("Cannot perform action because of missing permission: ${perm.name}")