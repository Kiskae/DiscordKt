package net.serverpeon.discord.internal.data.model

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import net.serverpeon.discord.internal.jsonmodels.UserModel
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.User

class UserCache(private val root: DiscordNode) {
    private val cache: Cache<DiscordId<User>, UserNode> = CacheBuilder.newBuilder()
            .weakValues()
            .build()

    fun retrieve(id: DiscordId<User>): UserNode? {
        return cache.getIfPresent(id)
    }

    fun retrieve(model: UserModel): UserNode {
        return retrieve(model.id, model)
    }

    fun retrieve(id: DiscordId<User>, model: UserModel): UserNode {
        return cache[id, { Builder.user(model, root) }]
    }

    fun retrieve(id: DiscordId<User>, node: SelfNode): UserNode {
        return cache[id, { node }].let {
            // Ensure we have our whoami model over a plain user model
            if (it !is SelfNode) {
                cache.put(id, node)
                node
            } else {
                it
            }
        }
    }
}