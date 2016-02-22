package net.serverpeon.discord.internal.data

import net.serverpeon.discord.internal.rest.data.ChannelModel
import net.serverpeon.discord.internal.ws.data.inbound.PrivateChannelModel
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.DiscordId

class ChannelNode(val root: DiscordNode,
                  override val id: DiscordId<Channel>) : Channel, EventProcessor {

    override fun acceptEvent(event: Any) {
        throw UnsupportedOperationException()
    }

    companion object {
        fun from(channel: ChannelModel, root: DiscordNode): ChannelNode {
            throw UnsupportedOperationException()
        }

        fun from(privateChannel: PrivateChannelModel, root: DiscordNode): ChannelNode {
            return ChannelNode(root, privateChannel.id)
        }
    }
}