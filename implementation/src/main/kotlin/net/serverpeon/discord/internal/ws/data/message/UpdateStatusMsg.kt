package net.serverpeon.discord.internal.ws.data.message

import net.serverpeon.discord.internal.ws.PayloadOut

data class UpdateStatusMsg(val game: Game, val idle_since: Any?) {
    data class Game(val name: String)

    fun toPayload(): PayloadOut<UpdateStatusMsg> = PayloadOut(3, this)
}