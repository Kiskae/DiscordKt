package net.serverpeon.discord.internal

import com.google.common.eventbus.EventBus
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.internal.bind.TypeAdapters
import net.serverpeon.discord.DiscordClient
import net.serverpeon.discord.DiscordClient.Builder.UserMetadata
import net.serverpeon.discord.internal.rest.adapters.*
import net.serverpeon.discord.internal.rest.retro.ApiWrapper
import net.serverpeon.discord.internal.rest.retro.Auth
import net.serverpeon.discord.internal.rest.rx
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.PermissionSet
import okhttp3.OkHttpClient
import rx.Single
import java.awt.Color
import java.time.Duration
import java.time.ZonedDateTime

class ClientBuilder : DiscordClient.Builder {
    companion object {
        private val DISCORDKT_VERSION = ClientBuilder::class.java.`package`.implementationVersion ?: "DEBUG"
    }

    private var tokenProvider: ((ApiWrapper) -> Single<String>)? = null
    private var eventBus: EventBus? = null
    private var okHttpClient: OkHttpClient? = null
    private var metadata: UserMetadata = UserMetadata("DiscordKt $DISCORDKT_VERSION")
    private var gson: Gson = setupGson().create()

    override fun login(email: String, password: String): DiscordClient.Builder {
        this.tokenProvider = { api ->
            api.Auth.login(Auth.LoginRequest(email, password)).rx().map {
                it.token ?: throw IllegalArgumentException("Discord failed to return a valid token!")
            }
        }
        return this
    }

    override fun token(token: String): DiscordClient.Builder {
        this.tokenProvider = {
            Single.just(token)
        }
        return this
    }

    override fun eventBus(eventBus: EventBus): DiscordClient.Builder {
        this.eventBus = eventBus
        return this
    }

    override fun metadata(metadata: UserMetadata): DiscordClient.Builder {
        this.metadata = metadata
        return this
    }

    override fun build(): DiscordClient {
        return ClientSession(createApiWrapper(), this.gson, this.eventBus ?: EventBus(), this.metadata)
    }

    // Implementation extension
    fun okHttp(client: OkHttpClient): ClientBuilder {
        this.okHttpClient = client
        return this
    }

    // Implementation extension
    fun extendGson(adapter: (GsonBuilder) -> GsonBuilder): ClientBuilder {
        this.gson = adapter(setupGson()).create()
        return this
    }

    private fun createApiWrapper(): Single<ApiWrapper> {
        val tokenProvider = this.tokenProvider ?: throw IllegalStateException("Please call login() or token() to configure the authentication method before build()")

        return ApiWrapper(okHttpClient ?: setupOkHttp(metadata), gson).let {
            tokenProvider(it).map { token ->
                it.token = token // Set token in ApiWrapper
                it
            }
        }
    }

    private fun setupGson(): GsonBuilder {
        return GsonBuilder().apply {
            registerTypeAdapter(Color::class.java, ColorAdapter.nullSafe())
            registerTypeAdapterFactory(TypeAdapters.newFactory(DiscordId::class.java, DiscordIdAdapter.nullSafe()))
            registerTypeAdapter(Duration::class.java, DurationAdapter.nullSafe())
            registerTypeAdapter(ZonedDateTime::class.java, ZonedDateTimeAdapter.nullSafe())
            registerTypeAdapter(PermissionSet::class.java, PermissionSetAdapter.nullSafe())
        }
    }

    private fun setupOkHttp(metadata: UserMetadata): OkHttpClient {
        return OkHttpClient.Builder().addInterceptor { chain ->
            chain.proceed(chain.request().let {
                it.newBuilder().addHeader("User-Agent", metadata.userAgent).build()
            })
        }.build()
    }
}