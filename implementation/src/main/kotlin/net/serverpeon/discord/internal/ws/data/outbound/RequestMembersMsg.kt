package net.serverpeon.discord.internal.ws.data.outbound

import net.serverpeon.discord.internal.ws.PayloadOut
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.Guild

data class RequestMembersMsg(val guild_id: List<DiscordId<Guild>>, val query: String = "", val limit: Int = 0) {
    fun toPayload(): PayloadOut<RequestMembersMsg> = PayloadOut(8, this)
}