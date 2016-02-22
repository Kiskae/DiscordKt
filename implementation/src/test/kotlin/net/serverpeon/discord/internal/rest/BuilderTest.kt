package net.serverpeon.discord.internal.rest

import com.google.common.eventbus.EventBus
import net.serverpeon.discord.DiscordClient
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.fail

class BuilderTest {
    @Test
    fun testBuilderNoAuth() {
        try {
            DiscordClient.newBuilder().build()
            fail()
        } catch (ex: IllegalStateException) {
            assertEquals("Please call login() or token() to configure the authentication method before build()", ex.message)
            assertNull(ex.cause)
        }
    }

    @Test
    fun testProvidedEventbus() {
        val eBus = EventBus()
        val client = DiscordClient.newBuilder().token("hello").eventBus(eBus).build()
        assertEquals(eBus, client.eventBus())
        client.close()
    }
}