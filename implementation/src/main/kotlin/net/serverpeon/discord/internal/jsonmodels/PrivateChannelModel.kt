package net.serverpeon.discord.internal.jsonmodels

import net.serverpeon.discord.internal.jsonmodels.UserModel
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.PostedMessage

data class PrivateChannelModel(val recipient: UserModel,
                               val last_message_id: DiscordId<PostedMessage>,
                               val is_private: Boolean,
                               val id: DiscordId<Channel>)