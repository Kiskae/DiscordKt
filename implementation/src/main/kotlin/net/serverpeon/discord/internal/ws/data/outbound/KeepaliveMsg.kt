package net.serverpeon.discord.internal.ws.data.outbound

import net.serverpeon.discord.internal.ws.PayloadOut

object KeepaliveMsg {
    fun toPayload(): PayloadOut<String> = PayloadOut(1, System.currentTimeMillis().toString())
}