package net.serverpeon.discord.interaction

import java.util.concurrent.CompletableFuture

/**
 * @property T The resource that is being edited
 * @property G The transaction that represents all the desired edits.
 */
interface Editable<T : Editable<T, G>, G : Editable.Transaction<G, T>> {

    /**
     * Start a new transaction to change the given resource.
     */
    @Throws(PermissionException::class)
    fun edit(): G

    interface Transaction<G : Transaction<G, T>, T : Editable<T, G>> {
        /**
         * Attempt to apply these changes to the live model.
         * Will return the changed object if it succeeds.
         *
         * If the underlying resource has changed between [edit] and [commit] the observable will fail with an
         * [ResourceChangedException].
         */
        fun commit(): CompletableFuture<T>
    }

    class ResourceChangedException(val resource: Editable<*, *>) : RuntimeException(
            "The resource $resource has changed between the start of the transaction and the attempt to commit."
    )
}