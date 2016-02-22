package net.serverpeon.discord.model

import java.util.*

data class PermissionSet internal constructor(private val permissions: EnumSet<Permission>) {
    fun toSet(): Set<Permission> {
        return Collections.unmodifiableSet(permissions)
    }

    operator fun contains(perm: Permission): Boolean {
        return permissions.contains(perm)
    }

    fun has(perm: Permission) = contains(perm)

    fun all(permissions: Collection<Permission>): Boolean {
        return this.permissions.containsAll(permissions)
    }

    fun without(perms: Collection<Permission>): PermissionSet {
        return PermissionSet(EnumSet.copyOf(permissions).apply {
            removeAll(perms)
        })
    }

    fun with(perms: Collection<Permission>): PermissionSet {
        return PermissionSet(EnumSet.copyOf(permissions).apply {
            addAll(perms)
        })
    }

    companion object {
        val ZERO = PermissionSet(EnumSet.noneOf(Permission::class.java))

        @JvmStatic
        fun create(permissions: Collection<Permission>): PermissionSet {
            return if (permissions.isEmpty()) {
                ZERO
            } else {
                PermissionSet(EnumSet.copyOf(permissions))
            }
        }
    }

    enum class Permission {
        CREATE_INSTANT_INVITE,
        KICK_MEMBERS,
        BAN_MEMBERS,
        MANAGE_ROLES,
        MANAGE_PERMISSIONS,
        MANAGE_CHANNELS,
        MANAGE_CHANNEL,
        MANAGE_SERVER,

        READ_MESSAGES,
        SEND_MESSAGES,
        SEND_TTS_MESSAGES,
        MANAGE_MESSAGES,
        EMBED_LINKS,
        ATTACH_FILES,
        READ_MESSAGE_HISTORY,
        MENTION_EVERYONE,

        VOICE_CONNECT,
        VOICE_SPEAK,
        VOICE_MUTE_MEMBERS,
        VOICE_DEAFEN_MEMBERS,
        VOICE_MOVE_MEMBERS,
        VOICE_USE_VAD;
    }
}