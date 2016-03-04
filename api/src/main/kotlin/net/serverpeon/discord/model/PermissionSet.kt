package net.serverpeon.discord.model

import java.util.*

data class PermissionSet internal constructor(private val permissions: EnumSet<Permission>) {
    /**
     * Checks if the given permission is in this set.
     */
    operator fun contains(perm: Permission): Boolean {
        return permissions.contains(perm)
    }

    /**
     * Alias for [contains].
     */
    fun has(perm: Permission) = contains(perm)

    /**
     * Checks if all the permissions in the given collection are in this set.
     *
     * @param permissions The permissions to check for.
     */
    fun all(permissions: Collection<Permission>): Boolean {
        return this.permissions.containsAll(permissions)
    }

    /**
     * Returns `true` if this set contains no permissions.
     */
    fun empty(): Boolean {
        return this.permissions.isEmpty()
    }

    /**
     * Creates a new PermissionSet that is the same as this set but without any of the permissions contained in the
     * other set.
     *
     * @param perms The permissions to delete from the cloned set.
     */
    fun without(perms: PermissionSet): PermissionSet {
        return PermissionSet(EnumSet.copyOf(permissions).apply {
            removeAll(perms.permissions)
        })
    }

    /**
     * Creates a new PermissionSet that is the same as this set but without any of the permissions contained in the
     * collection.
     *
     * @param perms The permissions to delete from the cloned set.
     */
    fun without(perms: Collection<Permission>): PermissionSet {
        return PermissionSet(EnumSet.copyOf(permissions).apply {
            removeAll(perms)
        })
    }

    /**
     * Creates a new PermissionSet that is the same as this set but includes all of the permissions contained in the
     * other set.
     *
     * @param perms The permissions to add to the cloned set.
     */
    fun with(perms: PermissionSet): PermissionSet {
        return PermissionSet(EnumSet.copyOf(permissions).apply {
            addAll(perms.permissions)
        })
    }

    /**
     * Creates a new PermissionSet that is the same as this set but includes all of the permissions contained in the
     * other collection.
     *
     * @param perms The permissions to add to the cloned set.
     */
    fun with(perms: Collection<Permission>): PermissionSet {
        return PermissionSet(EnumSet.copyOf(permissions).apply {
            addAll(perms)
        })
    }

    companion object {
        val ZERO = PermissionSet(EnumSet.noneOf(Permission::class.java))
        val ALL = PermissionSet(EnumSet.allOf(Permission::class.java))

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
        /**
         * Required for [Channel.Public.createInvite]
         */
        CREATE_INSTANT_INVITE,
        /**
         * Required for [Guild.Member.kick]
         */
        KICK_MEMBERS,
        /**
         * Required for [Guild.Member.ban] and [Guild.unban]
         */
        BAN_MEMBERS,
        /**
         * Required for [Guild.createRole], [Role.edit] and [Role.delete]
         */
        MANAGE_ROLES,
        /**
         * Presumably required for [Channel.Public.setOverride]
         */
        MANAGE_PERMISSIONS,
        /**
         * Required for [Guild.createChannel], [Channel.Public.edit] and [Channel.Public.delete] on all channels on the
         * server.
         */
        MANAGE_CHANNELS,
        /**
         * Required for [Channel.Public.edit] and [Channel.Public.delete] on a specific channel on the server.
         *
         * @see [Channel.Public.setOverride]
         */
        MANAGE_CHANNEL,
        /**
         * Required for [Guild.edit] and [Guild.delete]
         */
        MANAGE_SERVER,

        /**
         * Required in other to receive [MessageEvent] from a given channel.
         */
        READ_MESSAGES,
        /**
         * Required for access to [Channel.Text.sendMessage]
         */
        SEND_MESSAGES,
        /**
         * Required to enable the `textToSpeech` parameter of [Channel.Text.sendMessage]
         */
        SEND_TTS_MESSAGES,
        /**
         * Required to use [PostedMessage.delete] messages that do were not posted by the user itself.
         */
        MANAGE_MESSAGES,
        /**
         * Required to enable auto-embedding of links contained in messages.
         */
        EMBED_LINKS,
        /**
         * FIXME: how does this even work?
         *
         * Required to attach files to a message.
         */
        ATTACH_FILES,
        /**
         * Required to read back the message history of a channel.
         *
         * Required for [Channel.Text.messageHistory]
         */
        READ_MESSAGE_HISTORY,
        /**
         * Required for the use of [Message.Builder.appendMentionEveryone]
         */
        MENTION_EVERYONE,

        VOICE_CONNECT,
        VOICE_SPEAK,
        VOICE_MUTE_MEMBERS,
        VOICE_DEAFEN_MEMBERS,
        VOICE_MOVE_MEMBERS,
        VOICE_USE_VAD;
    }
}