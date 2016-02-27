package net.serverpeon.discord.model

/**
 * Represents the abilities of the [user] in terms of audio input (mute) and output(deaf).
 */
interface VoiceState {
    /**
     * Whether this user has deafened the sound on their end.
     */
    val deaf: Boolean

    /**
     * Whether this user has muted their microphone on their end.
     */
    val mute: Boolean

    /**
     * Whether someone with permission [PermissionSet.Permission.VOICE_DEAFEN_MEMBERS] has deafened the sound of this
     * user.
     */
    val forcedDeaf: Boolean

    /**
     * Whether someone with permission [PermissionSet.Permission.VOICE_MUTE_MEMBERS] has muted the sound of this
     * user.
     */
    val forcedMute: Boolean

    /**
     * The user associated with this voice state.
     */
    val user: User

    //TODO: currentChannel?

    //TODO: moveTo?
}