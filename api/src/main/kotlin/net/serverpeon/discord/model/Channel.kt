package net.serverpeon.discord.model

import net.serverpeon.discord.interaction.Deletable
import net.serverpeon.discord.interaction.Editable
import net.serverpeon.discord.interaction.PermissionException
import net.serverpeon.discord.message.Message
import rx.Completable
import rx.Observable
import java.time.Duration
import java.util.concurrent.CompletableFuture

/**
 * A channel is a line of communication in Discord.
 * Channels can either exist within a guild ([Public]) or happen between two people ([Private]).
 */
interface Channel : DiscordId.Identifiable<Channel>, Deletable {
    /**
     * Indicates whether this is a private or a public (guild) channel.
     *
     * If `true` then this can be cast to [Channel.Private], otherwise [Channel.Public]
     */
    val isPrivate: Boolean

    /**
     * Indicates the communication type of this channel.
     *
     * [Type.TEXT] -> Channel is [Public] and [Text]
     * [Type.VOICE] -> Channel is [Public] and [Voice]
     * [Type.PRIVATE] -> Channel is [Private] and [Text]
     */
    val type: Type

    /**
     * A channel that belongs to a [Guild], members can join according to the permissions defined by the guild
     * and the permission overrides for the individual channel.
     *
     * At any time an arbitrary number of people can receive the messages sent to this channel.
     */
    interface Public : Channel, Text, Voice, Editable<Public, Public.Edit> {
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
         * A list of members that have customized permissions in this channel.
         *
         * @see [setOverride]
         */
        val memberOverrides: Observable<ResolvedPermission<Guild.Member>>
        /**
         * A list of roles that have customized permissions in this channel.
         *
         * @see [setOverride]
         */
        val roleOverrides: Observable<ResolvedPermission<Role>>

        /**
         * Queries the final permissions for the given role after applying channel-specific overrides.
         *
         * Accessing permissions through this method instead of [roleOverrides] is more efficient for
         * single role queries.
         *
         * @param role Role to query for permissions with overrides
         */
        fun permissionsFor(role: Role): PermissionSet

        /**
         * Queries the final permissions for the given member after applying channel-specific overrides for both the
         * roles that this member has and member-specific ones.
         *
         * Accessing permissions through this method instead of [memberOverrides] is more efficient for
         * single member queries.
         *
         * @param member Member to query for permissions with overrides
         */
        fun permissionsFor(member: Guild.Member): PermissionSet

        /**
         * Create a new invite which another use can use to join this channel/server.
         *
         * @param expiredAfter Sets the invite to expire after this amount of time has passed. Set to [Duration.ZERO]
         *                     to make the invite never expire.
         * @param maxUses Limit the number of times the invite can be used. Set to 0 for unlimited.
         * @param temporaryMembership Whether this invite grants temporary membership, which means the user will be
         *                            kicked from the server when they go offline.
         * @param humanReadableId Whether to generate a human-readable invite using the method described in
         *                        http://xkcd.com/936/ instead of a random alphanumeric code.
         * @throws PermissionException if the client does now have [PermissionSet.Permission.CREATE_INSTANT_INVITE]
         */
        @Throws(PermissionException::class)
        fun createInvite(expiredAfter: Duration = Duration.ZERO,
                         maxUses: Int = 0,
                         temporaryMembership: Boolean = false,
                         humanReadableId: Boolean = false): CompletableFuture<Invite.Details>

        /**
         * Retrieves all active invites previously created for this channel.
         *
         * @throws PermissionException If the client does not have [PermissionSet.Permission.MANAGE_CHANNEL] for this
         *                             channel.
         */
        @Throws(PermissionException::class)
        fun getActiveInvites(): Observable<Invite.Details>

        /**
         * Customizes the permissions for the given member.
         * Any permissions that are in neither [allow] nor [deny] will use the state given by the member's roles.
         *
         * Setting both [allow] and [deny] to [PermissionSet.ZERO] will clear a previously established override.
         *
         * @param allow Permissions to explicitly allow.
         * @param deny Permissions to explicitly deny.
         * @param member Target to customize the permissions for.
         * @return Future that will complete when the change has been applied, will return the permissions effective
         *         after applying the change.
         * @throws PermissionException if the permission [PermissionSet.Permission.MANAGE_PERMISSIONS] is missing.
         */
        @Throws(PermissionException::class)
        fun setOverride(allow: PermissionSet, deny: PermissionSet, member: Guild.Member): CompletableFuture<PermissionSet>

        /**
         * Customizes the permissions for the given role.
         * Any permissions that are in neither [allow] nor [deny] will use the state assigned to the base role.
         *
         * Setting both [allow] and [deny] to [PermissionSet.ZERO] will clear a previously established override.
         *
         * @param allow Permissions to explicitly allow.
         * @param deny Permissions to explicitly deny.
         * @param role Target to customize the permissions for.
         * @return Future that will complete when the change has been applied, will return the permissions effective
         *         after applying the change.
         * @throws PermissionException if the permission [PermissionSet.Permission.MANAGE_PERMISSIONS] is missing.
         */
        @Throws(PermissionException::class)
        fun setOverride(allow: PermissionSet, deny: PermissionSet, role: Role): CompletableFuture<PermissionSet>

        interface Edit : Editable.Transaction<Edit, Public> {
            /**
             * Attempt to change the topic of the channel. This will only have an effect if the channel is a [Text]
             * channel.
             */
            var topic: String
            /**
             * Change the public name of the channel.
             * It must be between 2-100 characters long and can only contain alphanumeric characters and dashes.
             */
            var name: String
        }
    }

    /**
     * Represents the final permissions given to the [holder] after applying all role permissions and relevant overrides.
     *
     * @property holder Whoever holds these permissions.
     * @property perms The permissions at the time that the source method was called.
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

    interface Text : Channel {

        /**
         * Overload for [sendMessage] with textToSpeech set to null.
         */
        @Throws(PermissionException::class)
        fun sendMessage(message: Message) = sendMessage(message, null)

        /**
         * Sends the given message to the channel
         *
         * @param message The message to send
         * @param textToSpeech Whether to mark the message for text-to-speech.
         * @return A future that returns the message with additional information about the post.
         * @throws PermissionException if the permission [PermissionSet.Permission.SEND_MESSAGES] is missing.
         */
        @Throws(PermissionException::class)
        fun sendMessage(message: Message, textToSpeech: Boolean?): CompletableFuture<PostedMessage>

        /**
         * Retrieves the last [limit] messages sent to this channel.
         *
         * @param limit The number of messages to retrieve.
         * @param before Start the message history at this message.
         * @return A lazy observable that retrieves the messages from the backend, if only a subset of the
         *         returned value is used then it will attempt to limit additional requests.
         * @throws PermissionException if the permission [PermissionSet.Permission.READ_MESSAGE_HISTORY] is missing.
         */
        @Throws(PermissionException::class)
        fun messageHistory(limit: Int, before: DiscordId<PostedMessage>? = null): Observable<PostedMessage>

        /**
         * Subscribing to the returned completable will send a 'typing' state to discord.
         *
         * It will maintain this state for **5** seconds so it will need to be resubscribed to repeatedly in order to
         * maintain typing state for longer periods.
         */
        fun indicateTyping(): Completable
    }

    interface Voice : Channel {
        /**
         * Retrieves the current voice states for all people in this voice channel.
         *
         * Calling this on a non-voice channel will result in an empty observable.
         */
        val voiceStates: Observable<VoiceState>

        //TODO: potentially allow the opening of a voice channel....
    }

    enum class Type {
        /**
         * Standard text chat, receives [PostedMessage] as units of communication.
         */
        TEXT,
        /**
         * A voice channel, communication happens outside of the monitoring capabilities of this client.
         */
        VOICE,
        /**
         * Direct message chat, for sending [PostedMessage] back and forth with a single person.
         */
        PRIVATE
    }
}