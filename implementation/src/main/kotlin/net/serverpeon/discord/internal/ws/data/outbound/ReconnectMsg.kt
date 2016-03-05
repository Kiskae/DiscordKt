package net.serverpeon.discord.internal.ws.data.outbound

import net.serverpeon.discord.internal.ws.PayloadOut

data class ReconnectMsg(val session_id: String, val seq: Int) {
    fun toPayload(): PayloadOut<ReconnectMsg> = PayloadOut(6, this)
}