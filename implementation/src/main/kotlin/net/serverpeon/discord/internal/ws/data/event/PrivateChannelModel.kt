package net.serverpeon.discord.internal.ws.data.event

import net.serverpeon.discord.internal.rest.data.UserModel
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.Message

data class PrivateChannelModel(val recipient: UserModel,
                               val last_message_id: DiscordId<Message>,
                               val is_private: Boolean,
                               val id: DiscordId<Channel>)