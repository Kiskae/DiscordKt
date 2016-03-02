package net.serverpeon.discord.interaction

import java.util.concurrent.CompletableFuture

/**
 * Marks an object that can be permanently removed from the model.
 */
interface Deletable {
    /**
     * Requests that Discord delete this resource.
     *
     * @throws [PermissionException]
     */
    @Throws(PermissionException::class)
    fun delete(): CompletableFuture<Void>
}