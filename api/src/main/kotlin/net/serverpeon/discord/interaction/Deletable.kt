package net.serverpeon.discord.interaction

import java.util.concurrent.CompletableFuture

interface Deletable {
    /**
     * @throws [PermissionException]
     */
    @Throws(PermissionException::class)
    fun delete(): CompletableFuture<Void>
}