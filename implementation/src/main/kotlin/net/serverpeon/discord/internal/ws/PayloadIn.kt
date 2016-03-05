package net.serverpeon.discord.internal.ws

import com.google.gson.JsonElement

/**
 * @property t Type of the event
 * @property s Sequence number of the event for this session
 * @property op ???
 * @property d Payload, specific for each event
 */
data class PayloadIn(val t: String,
                     val s: Int?,
                     val op: Int,
                     val d: JsonElement)