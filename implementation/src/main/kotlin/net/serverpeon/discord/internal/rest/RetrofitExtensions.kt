package net.serverpeon.discord.internal.rest

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import rx.Completable
import rx.Observable
import rx.Single
import rx.Subscriber
import rx.subscriptions.Subscriptions

fun Call<Void>.rx(): Completable {
    return this.internalToRx().toCompletable()
}

fun <T> Call<T>.rx(): Single<T> {
    return this.rxObservable().toSingle()
}

fun <T> Call<T>.rxObservable(): Observable<T> {
    return this.internalToRx().map { it.body() }
}

class ResponseException(
        val call: Call<*>,
        val response: Response<*>
) : RuntimeException("Call [$call] failed: [$response]")

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
        } else {
            sub.onError(ResponseException(call, response))
        }
    }

}