package net.serverpeon.discord.model

import java.awt.Color

interface Role : DiscordId.Identifiable<Role> {
    val name: String

    val imported: Boolean

    val grouped: Boolean

    val permissions: PermissionSet

    val color: Color
}