package net.serverpeon.discord.internal.data

import net.serverpeon.discord.internal.rest.data.SelfModel

class WhoamiNode : UserNode(), EventProcessor {
    override fun acceptEvent(event: Any) {
        throw UnsupportedOperationException()
    }

    companion object {
        fun from(self: SelfModel, root: DiscordNode): WhoamiNode {
            return WhoamiNode()
        }
    }
}