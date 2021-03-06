package net.serverpeon.discord.internal.ws.client

import com.google.common.collect.ImmutableMap
import com.google.gson.Gson
import com.google.gson.JsonElement
import net.serverpeon.discord.internal.loggerFor
import net.serverpeon.discord.internal.ws.PayloadIn
import java.io.Reader
import java.util.concurrent.atomic.AtomicInteger

internal class MessageTranslator private constructor(private val dsl: DSL,
                                                     private val handlers: ImmutableMap<String, DSL.(JsonElement) -> Any>) {
    private val logger = loggerFor<MessageTranslator>()
    private val sequence = AtomicInteger(-1)

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

        logger.trace {
            "[${event.t},${event.op},${event.s}] ${dsl.gson.toJson(event.d)}"
        }

        if (event.s != null) {
            // Update latest sequence number
            sequence.set(event.s)
        }

        return when (event.op) {
            0 -> {
                // Default event handler
                val handler = handlers[event.t]
                if (handler != null) {
                    dsl.handler(event.d)
                } else {
                    logger.warn { "Unhandled event: [${event.t}] ${dsl.gson.toJson(event.d)}" }
                    null
                }
            }
            7 -> {
                // This should be correctly handled upstream
                dsl.gson.fromJson(event.d, ReconnectCommand::class.java).apply {
                    // Insert the correct sequence number
                    this.sequence = this@MessageTranslator.sequence.andDecrement
                }
            }
            else -> {
                logger.warn { "Unknown op: $event" }
            }
        }
    }
}