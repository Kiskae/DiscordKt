package net.serverpeon.discord.internal.data

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import net.serverpeon.discord.internal.rest.data.UserModel
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.User

class UserCache {
    private val cache: Cache<DiscordId<User>, UserNode> = CacheBuilder.newBuilder()
            .weakValues()
            .build()

    fun retrieve(id: DiscordId<User>): UserNode? {
        return cache.getIfPresent(id)
    }

    fun retrieve(id: DiscordId<User>, model: UserModel): UserNode {
        return cache[id, { UserNode.from(model) }]
    }

    fun retrieve(id: DiscordId<User>, node: WhoamiNode): UserNode {
        return cache[id, { node }].apply {
            // Ensure we have our whoami model over a plain user model
            if (this !is WhoamiNode) {
                cache.put(id, node)
            }
        }
    }
}