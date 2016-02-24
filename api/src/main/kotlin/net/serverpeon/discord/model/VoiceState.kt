package net.serverpeon.discord.model

interface VoiceState {
    val deaf: Boolean
    val mute: Boolean
    val forcedDeaf: Boolean
    val forcedMute: Boolean
}