package net.serverpeon.discord.internal.rest.retro

import com.google.gson.Gson
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.net.URI

class ApiWrapper(private val client: OkHttpClient,
                 private val gson: Gson,
                 var token: String? = null) {
    private val authenticatedClient: OkHttpClient by lazy {
        client.newBuilder().addInterceptor(TokenInjector()).build()
    }

    private val authenticatedRetrofit: Retrofit by lazy {
        Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(HttpUrl.parse("https://discordapp.com/api/"))
                .client(authenticatedClient)
                .build()
    }

    private val statusRetrofit: Retrofit by lazy {
        Retrofit.Builder()
                .baseUrl(HttpUrl.parse("https://status.discordapp.com/api/v2/"))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client) // Unauthenticated, not the API
                .build()
    }

    val Auth: Auth by create { authenticatedRetrofit }
    val Guilds: Guilds by create { authenticatedRetrofit }
    val Me: Users.Me by create { authenticatedRetrofit }
    val Users: Users by create { authenticatedRetrofit }
    val Channels: Channels by create { authenticatedRetrofit }
    val Status: Status by create { statusRetrofit }
    val Gateway: WsGateway by create { authenticatedRetrofit }
    val Voice: Voice by create { authenticatedRetrofit }

    interface WsGateway {
        @GET("gateway")
        fun wsEndpoint(): Call<EndpointSpec>

        data class EndpointSpec(val url: URI)
    }

    private inline fun <reified T : Any> create(crossinline retrofit: () -> Retrofit): Lazy<T> {
        return lazy(LazyThreadSafetyMode.PUBLICATION) {
            retrofit().create(T::class.java)
        }
    }

    private inner class TokenInjector : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val currentToken = token
            return chain.proceed(chain.request().let { req ->
                //Only send over HTTPS
                if (currentToken != null && req.isHttps) {
                    req.newBuilder().addHeader("Authorization", currentToken).build()
                } else {
                    req
                }
            })
        }
    }
}