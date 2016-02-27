package net.serverpeon.discord.model

import net.serverpeon.discord.interaction.Deletable
import net.serverpeon.discord.interaction.Editable
import java.awt.Color

/**
 *
 */
interface Role : DiscordId.Identifiable<Role>, Editable<Role, Role.Edit>, Deletable {
    /**
     *
     */
    val name: String

    /**
     *
     */
    val imported: Boolean

    /**
     *
     */
    val grouped: Boolean

    /**
     *
     */
    val permissions: PermissionSet

    /**
     *
     */
    val color: Color

    interface Edit : Editable.Transaction<Edit, Role> {
        var color: Color

        var grouped: Boolean

        var name: String

        var permissions: PermissionSet
    }
}