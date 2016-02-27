package net.serverpeon.discord.model

import net.serverpeon.discord.interaction.Deletable
import net.serverpeon.discord.interaction.Editable
import java.awt.Color

/**
 * A role represents a set of permissions and other properties that can be assigned as a package to members of a [Guild].
 */
interface Role : DiscordId.Identifiable<Role>, Editable<Role, Role.Edit>, Deletable {
    /**
     * The name of this role.
     */
    val name: String

    /**
     * Whether this role was created automatically by Discord.
     * In most scenarios the members that are a part of this group will also be assigned automatically.
     */
    val imported: Boolean

    /**
     * Whether to show this group separately in the Discord UI.
     */
    val grouped: Boolean

    /**
     * The permissions given by this role.
     */
    val permissions: PermissionSet

    /**
     * The name color of all users for which this is the top role.
     */
    val color: Color

    interface Edit : Editable.Transaction<Edit, Role> {
        var color: Color

        var grouped: Boolean

        var name: String

        var permissions: PermissionSet
    }
}