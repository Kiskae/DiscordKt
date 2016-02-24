package net.serverpeon.discord.model

import rx.Observable

/**
 * A channel is a line of communication in Discord.
 * Channels can either exist within a guild ([Public]) or happen between two people ([Private]).
 */
interface Channel : DiscordId.Identifiable<Channel> {
    /**
     * Indicates whether this is a private or a public (guild) channel.
     *
     * If `true` then this can be cast to [Channel.Private], otherwise [Channel.Public]
     */
    val isPrivate: Boolean

    /**
     * Indicates the communication type of this channel.
     *
     * [Type.TEXT] -> Object is [Public] and [Text]
     * [Type.VOICE] -> Object is [Public] and [Voice]
     * [Type.PRIVATE] -> Object is [Private] and [Text]
     */
    val type: Type

    /**
     * A channel that belongs to a [Guild], members can join according to the permissions defined by the guild
     * and the permission overrides for the individual channel.
     *
     * At any time an arbitrary number of people can receive the messages sent to this channel.
     */
    interface Public : Channel, Text, Voice {
        /**
         * The guild that this channel belongs to.
         */
        val guild: Guild
        /**
         * The topic set for this channel.
         *
         * **Note:** [Type.VOICE] channels do not have a topic and will always return `""`
         */
        val topic: String
        /**
         * The name of this channel, this shows up in channel list on the official discord interface.
         */
        val name: String
        /**
         *
         */
        val memberOverrides: Observable<ResolvedPermission<Guild.Member>>
        /**
         *
         */
        val roleOverrides: Observable<ResolvedPermission<Role>>

        /**
         *
         */
        fun permissionsFor(role: Role): PermissionSet

        /**
         *
         */
        fun permissionsFor(member: Guild.Member): PermissionSet
    }

    /**
     * @property holder
     * @property perms
     */
    data class ResolvedPermission<G : DiscordId.Identifiable<*>>(val holder: G, val perms: PermissionSet)

    /**
     * A direct messaging channel to [recipient], messages can be read by you and them.
     */
    interface Private : Channel, Text {
        /**
         * The recipient on the other end of this channel.
         */
        val recipient: User
    }

    interface Text {

        /**
         * TODO: should text/voice be separated?
         *
         * @param msgSpec TODO
         */
        //fun sendMessage(msgSpec: Nothing): Observable<Message>

        //TODO: message history
    }

    interface Voice {
        /**
         *
         */
        val members: Observable<Guild.Member>
    }

    enum class Type {
        /**
         * Standard text chat, receives [Message] as units of communication.
         */
        TEXT,
        /**
         * A voice channel, communication happens outside of the monitoring capabilities of this client.
         */
        VOICE,
        /**
         * Direct message chat, for sending [Message] back and forth with a single person.
         */
        PRIVATE
    }
}