package net.serverpeon.discord.internal

import net.serverpeon.discord.RateLimitException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import rx.Completable
import rx.Observable
import rx.Single
import rx.Subscriber
import rx.subscriptions.Subscriptions
import java.io.IOException
import java.time.Duration
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import javax.websocket.Session

private object ConcurrentExt {
    val logger = createLogger()
}

fun Call<Void>.rx(): Completable {
    return this.internalToRx().toCompletable()
}

fun <T> Call<T>.rx(): Single<T> {
    return this.rxObservable().toSingle()
}

fun <T> Call<T>.rxObservable(): Observable<T> {
    return this.internalToRx().map { it.body() }
}

open class ResponseException(
        val call: Call<*>,
        val response: Response<*>
) : RuntimeException("Call [${call.request().url()}] failed: [${response.message()}]")

class ImplRateLimitException(val call: Call<*>,
                             val response: Response<*>,
                             retryAfter: Duration)
: RateLimitException(
        "Call [${call.request().url()}] failed: [${response.message()}]",
        retryAfter
)


private fun <T> Call<T>.internalToRx(): Observable<Response<T>> {
    return Observable.create { sub ->
        this.clone().apply {
            sub.add(Subscriptions.create { cancel() })
            enqueue(RxCallback(sub))
        }
    }
}

private class RxCallback<T>(val sub: Subscriber<in Response<T>>) : Callback<T> {
    override fun onFailure(call: Call<T>, th: Throwable) {
        if (sub.isUnsubscribed) {
            // NOOP
        } else if (call.isCanceled) {
            // Since it was cancelled we just say its complete, but we don't have any data to pass on
            sub.onCompleted()
        } else {
            sub.onError(th)
        }
    }

    override fun onResponse(call: Call<T>, response: Response<T>) {
        if (sub.isUnsubscribed) {
            // NOOP
        } else if (response.isSuccess) {
            sub.onNext(response)
            sub.onCompleted()
        } else if (response.code() == 429) {
            sub.onError(ImplRateLimitException(call, response, response.headers().get("Retry-After")?.let {
                Duration.ofMillis(it.toLong())
            } ?: Duration.ZERO))
        } else {
            sub.onError(ResponseException(call, response))
        }
    }
}

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

fun <T> Observable<T>.toFuture(): CompletableFuture<T> {
    val future = CompletableFuture<T>()
    val subscription = this.last().subscribe({
        future.complete(it)
    }, {
        future.completeExceptionally(it)
    })
    future.exceptionally ({ th ->
        if (th is CancellationException) {
            subscription.unsubscribe()
        }
        @Suppress("CAST_NEVER_SUCCEEDS")
        (null as Nothing)
    })
    return future
}

fun Session.send(text: String): CompletableFuture<Void> {
    val future = CompletableFuture<Void>()

    if (isOpen) {
        ConcurrentExt.logger.kTrace { "[SEND] $text" }
        asyncRemote.sendText(text, { result ->
            if (result.isOK) {
                future.complete(null)
            } else {
                future.completeExceptionally(result.exception)
            }
        })
    } else {
        future.completeExceptionally(IOException("Trying to send to a closed socket."))
    }

    return future
}

fun <T> Call<T>.toFuture(): CompletableFuture<T> {
    val future = CompletableFuture<T>()
    this.clone().apply {
        future.handle { value, th ->
            // Add cancel handling logic
            if (value == null && th is CancellationException) {
                cancel()
            }
            null
        }
        enqueue(FutureCallback(future))
    }
    return future
}

private class FutureCallback<T>(val future: CompletableFuture<T>) : Callback<T> {
    override fun onFailure(call: Call<T>, th: Throwable) {
        if (call.isCanceled) {
            future.completeExceptionally(
                    if (th is CancellationException)
                        th
                    else
                        CancellationException()
            )
        } else {
            future.completeExceptionally(th)
        }
    }

    override fun onResponse(call: Call<T>, response: Response<T>) {
        if (response.isSuccess) {
            future.complete(response.body())
        } else if (response.code() == 429) {
            future.completeExceptionally(ImplRateLimitException(call, response, response.headers().get("Retry-After")?.let {
                Duration.ofMillis(it.toLong())
            } ?: Duration.ZERO))
        } else {
            future.completeExceptionally(ResponseException(call, response))
        }
    }
}