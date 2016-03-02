package net.serverpeon.discord.internal.jsonmodels

import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.User

data class SelfModel(val verified: Boolean,
                     val username: String,
                     val id: DiscordId<User>,
                     val email: String,
                     val discriminator: String,
                     val avatar: DiscordId<User.Avatar>?)