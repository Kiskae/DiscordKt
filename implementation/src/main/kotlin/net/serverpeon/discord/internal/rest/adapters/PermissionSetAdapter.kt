package net.serverpeon.discord.internal.rest.adapters

import com.google.common.collect.ImmutableMultimap
import com.google.common.collect.Multimap
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import net.serverpeon.discord.model.PermissionSet

object PermissionSetAdapter : TypeAdapter<PermissionSet>() {
    private val idMapping: Multimap<Int, PermissionSet.Permission> =
            ImmutableMultimap.builder<Int, PermissionSet.Permission>().apply {
                infix fun Int.to(perm: PermissionSet.Permission) {
                    put(1.shl(this), perm)
                }

                0 to PermissionSet.Permission.CREATE_INSTANT_INVITE
                1 to PermissionSet.Permission.KICK_MEMBERS
                2 to PermissionSet.Permission.BAN_MEMBERS
                3 to PermissionSet.Permission.MANAGE_ROLES
                3 to PermissionSet.Permission.MANAGE_PERMISSIONS
                4 to PermissionSet.Permission.MANAGE_CHANNELS
                4 to PermissionSet.Permission.MANAGE_CHANNEL
                5 to PermissionSet.Permission.MANAGE_SERVER

                10 to PermissionSet.Permission.READ_MESSAGES
                11 to PermissionSet.Permission.SEND_MESSAGES
                12 to PermissionSet.Permission.SEND_TTS_MESSAGES
                13 to PermissionSet.Permission.MANAGE_MESSAGES
                14 to PermissionSet.Permission.EMBED_LINKS
                15 to PermissionSet.Permission.ATTACH_FILES
                16 to PermissionSet.Permission.READ_MESSAGE_HISTORY
                17 to PermissionSet.Permission.MENTION_EVERYONE

                20 to PermissionSet.Permission.VOICE_CONNECT
                21 to PermissionSet.Permission.VOICE_SPEAK
                22 to PermissionSet.Permission.VOICE_MUTE_MEMBERS
                23 to PermissionSet.Permission.VOICE_DEAFEN_MEMBERS
                24 to PermissionSet.Permission.VOICE_MOVE_MEMBERS
                25 to PermissionSet.Permission.VOICE_USE_VAD
            }.build()

    private fun PermissionSet.encode(): Int {
        return idMapping.entries().filter {
            // Retain permissions contained in this PermissionSet
            it.value in this
        }.map {
            // Map to int
            it.key
        }.fold(0, Int::or)
    }

    private fun Int.decode(): PermissionSet {
        return PermissionSet.create(idMapping.entries().filter {
            // Find permissions which have their mask enabled
            this.and(it.key) > 0
        }.map {
            it.value
        })
    }

    override fun write(writer: JsonWriter, value: PermissionSet) {
        writer.value(value.encode())
    }

    override fun read(reader: JsonReader): PermissionSet? {
        return reader.nextInt().decode()
    }
}