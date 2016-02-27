package net.serverpeon.discord.internal.data

import net.serverpeon.discord.interaction.Editable
import net.serverpeon.discord.internal.rest.data.RoleModel
import net.serverpeon.discord.internal.rest.data.WrappedId
import net.serverpeon.discord.internal.rest.retro.Guilds.EditRoleRequest
import net.serverpeon.discord.internal.toFuture
import net.serverpeon.discord.internal.ws.data.inbound.Event
import net.serverpeon.discord.internal.ws.data.inbound.Guilds
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.PermissionSet
import net.serverpeon.discord.model.Role
import java.awt.Color
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger

class RoleNode(private val root: DiscordNode,
               private val guild: GuildNode,
               override val id: DiscordId<Role>,
               override var name: String,
               override val imported: Boolean,
               override var grouped: Boolean,
               override var permissions: PermissionSet,
               override var color: Color,
               var position: Int) : Role, Event.Visitor {
    private val changeId = AtomicInteger(0)

    override fun guildRoleUpdate(e: Guilds.Roles.Update) {
        changeId.incrementAndGet()
        e.role.let {
            color = it.color
            permissions = it.permissions
            grouped = it.hoist
            name = it.name
            position = it.position
        }
    }

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

    private inner class Transaction(override var color: Color,
                                    override var grouped: Boolean,
                                    override var name: String,
                                    override var permissions: PermissionSet) : Role.Edit {
        private var aborted: TransactionTristate = TransactionTristate.AWAIT
        private val changeIdAtInit = changeId.get()

        override fun commit(): CompletableFuture<Role> {
            if (changeId.compareAndSet(changeIdAtInit, changeIdAtInit + 1)) {
                throw Editable.ResourceChangedException(this@RoleNode)
            } else if (aborted == TransactionTristate.ABORTED) {
                throw Editable.AbortedTransactionException()
            } else if (aborted == TransactionTristate.COMPLETED) {
                throw IllegalStateException("Don't call complete() twice")
            } else {
                aborted = TransactionTristate.COMPLETED
                return root.api.Guilds.editRole(WrappedId(guild.id), WrappedId(id), EditRoleRequest(
                        color = color,
                        hoist = grouped,
                        name = name,
                        permissions = permissions
                )).toFuture().thenApply {
                    RoleNode.from(it, guild, root)
                }
            }
        }

        override fun abort() {
            if (aborted == TransactionTristate.AWAIT) {
                aborted = TransactionTristate.ABORTED
            } else if (aborted == TransactionTristate.COMPLETED) {
                throw IllegalArgumentException("äbort() after complete()")
            }
        }
    }

    companion object {
        fun from(model: RoleModel, guild: GuildNode, root: DiscordNode): RoleNode {
            return RoleNode(root,
                    guild,
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