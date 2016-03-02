package net.serverpeon.discord.internal.data.model

import net.serverpeon.discord.interaction.Editable
import net.serverpeon.discord.internal.data.EventInput
import net.serverpeon.discord.internal.rest.WrappedId
import net.serverpeon.discord.internal.rest.retro.Guilds.EditRoleRequest
import net.serverpeon.discord.internal.toFuture
import net.serverpeon.discord.internal.ws.data.inbound.Guilds
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.PermissionSet
import net.serverpeon.discord.model.Role
import java.awt.Color
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class RoleNode(private val root: DiscordNode,
               private val guild: GuildNode,
               override val id: DiscordId<Role>,
               override var name: String,
               override val imported: Boolean,
               override var grouped: Boolean,
               override var permissions: PermissionSet,
               override var color: Color,
               var position: Int) : Role, EventInput<RoleNode> {
    private val changeId = AtomicInteger(0)

    override fun edit(): Role.Edit {
        guild.selfAsMember.checkPermission(guild, PermissionSet.Permission.MANAGE_ROLES)
        return Transaction(color, grouped, name, permissions)
    }

    override fun delete(): CompletableFuture<Void> {
        guild.selfAsMember.checkPermission(guild, PermissionSet.Permission.MANAGE_ROLES)
        return root.api.Guilds.deleteRole(WrappedId(guild.id), WrappedId(id)).toFuture()
    }

    override fun toString(): String {
        return "Role(id=$id, name='$name', imported=$imported, grouped=$grouped, permissions=$permissions, color=$color)"
    }

    override fun handler(): EventInput.Handler<RoleNode> {
        return RoleEventHandler
    }

    private object RoleEventHandler : EventInput.Handler<RoleNode> {
        override fun guildRoleUpdate(target: RoleNode, e: Guilds.Roles.Update) {
            target.changeId.incrementAndGet()
            e.role.let {
                target.color = it.color
                target.permissions = it.permissions
                target.grouped = it.hoist
                target.name = it.name
                target.position = it.position
            }
        }
    }

    private inner class Transaction(override var color: Color,
                                    override var grouped: Boolean,
                                    override var name: String,
                                    override var permissions: PermissionSet) : Role.Edit {
        private var completed = AtomicBoolean(false)
        private val changeIdAtInit = changeId.get()

        override fun commit(): CompletableFuture<Role> {
            if (changeId.compareAndSet(changeIdAtInit, changeIdAtInit + 1)) {
                throw Editable.ResourceChangedException(this@RoleNode)
            } else if (completed.getAndSet(true)) {
                throw IllegalStateException("Don't call complete() twice")
            } else {
                return root.api.Guilds.editRole(WrappedId(guild.id), WrappedId(id), EditRoleRequest(
                        color = color,
                        hoist = grouped,
                        name = name,
                        permissions = permissions
                )).toFuture().thenApply {
                    Builder.role(it, guild)
                }
            }
        }
    }
}