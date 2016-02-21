package net.serverpeon.discord.internal.ws.client

import com.google.common.collect.ImmutableMap
import com.google.gson.Gson
import com.google.gson.JsonElement
import net.serverpeon.discord.internal.createLogger
import net.serverpeon.discord.internal.kTrace
import net.serverpeon.discord.internal.kWarn
import net.serverpeon.discord.internal.ws.PayloadIn
import java.io.Reader

internal class MessageTranslator private constructor(private val dsl: DSL,
                                                     private val handlers: ImmutableMap<String, DSL.(JsonElement) -> Any>) {
    private val logger = createLogger()

    class Builder(private val gson: Gson) {
        private val builder = ImmutableMap.builder<String, DSL.(JsonElement) -> Any>()

        fun <A : Any> registerType(id: String, processor: DSL.(JsonElement) -> A): Builder {
            builder.put(id.trim(), processor)
            return this
        }

        fun build(): MessageTranslator {
            return MessageTranslator(DSL(gson), builder.build())
        }
    }

    class DSL(val gson: Gson) {
        inline fun <reified A : Any> JsonElement.parse(): A {
            return gson.fromJson(this, A::class.java)
        }
    }

    fun translate(input: Reader): Any? {
        val event = dsl.gson.fromJson(input, PayloadIn::class.java)

        logger.kTrace {
            "[${event.t}] ${dsl.gson.toJson(event.d)}"
        }

        val handler = handlers[event.t]
        return if (handler != null) {
            dsl.handler(event.d)
        } else {
            logger.kWarn { "Unhandled event: ${event.t} [${dsl.gson.toJson(event.d)}" }
            null
        }
    }
}