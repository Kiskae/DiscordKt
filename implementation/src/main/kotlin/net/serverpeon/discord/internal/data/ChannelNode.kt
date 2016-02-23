package net.serverpeon.discord.internal.data

import net.serverpeon.discord.internal.rest.data.ChannelModel
import net.serverpeon.discord.internal.ws.data.inbound.Channels
import net.serverpeon.discord.internal.ws.data.inbound.Event
import net.serverpeon.discord.internal.ws.data.inbound.Misc
import net.serverpeon.discord.internal.ws.data.inbound.PrivateChannelModel
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.DiscordId

class ChannelNode(val root: DiscordNode,
                  override val id: DiscordId<Channel>,
                  val isPrivate: Boolean) : Channel, Event.Visitor {

    override fun channelUpdate(e: Channels.Update) {
        //TODO: update metadata, probably overrides
    }

    override fun voiceStateUpdate(e: Misc.VoiceStateUpdate) {
        // Ignored for now, how do we represent this
    }

    override fun typingStart(e: Misc.TypingStart) {
        // Ignored, not something we need to represent
    }

    override fun toString(): String {
        return "Channel(id=$id)"
    }

    companion object {
        fun from(channel: ChannelModel, root: DiscordNode): ChannelNode {
            return ChannelNode(root, channel.id, channel.is_private)
        }

        fun from(privateChannel: PrivateChannelModel, root: DiscordNode): ChannelNode {
            return ChannelNode(root, privateChannel.id, privateChannel.is_private)
        }
    }
}