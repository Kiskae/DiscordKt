package net.serverpeon.discord.internal.rest.data

import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.User

data class UserModel(val username: String,
                     val id: DiscordId<User>,
                     val discriminator: String,
                     val avatar: String?)