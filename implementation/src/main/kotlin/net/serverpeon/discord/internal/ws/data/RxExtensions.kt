package net.serverpeon.discord.internal.ws.data

import rx.Observable
import java.util.concurrent.CompletableFuture

fun <T> CompletableFuture<T>.toObservable(): Observable<T> {
    return Observable.create { sub ->
        this.whenComplete { result, throwable ->
            if (throwable != null) {
                sub.onError(throwable)
            } else {
                sub.onNext(result)
                sub.onCompleted()
            }
        }
    }
}
