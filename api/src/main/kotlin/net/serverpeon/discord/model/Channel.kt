package net.serverpeon.discord.model

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
     * Indicates the communication type of this channel
     */
    val type: Type

    /**
     * A channel that belongs to a [Guild], members can join according to the permissions defined by the guild
     * and the permission overrides for the individual channel.
     *
     * At any time an arbitrary number of people can receive the messages sent to this channel.
     */
    interface Public : Channel {
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
        //TODO: permission override
    }

    /**
     * A direct messaging channel to [recipient], messages can be read by you and them.
     */
    interface Private : Channel {
        /**
         * The recipient on the other end of this channel.
         */
        val recipient: User
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