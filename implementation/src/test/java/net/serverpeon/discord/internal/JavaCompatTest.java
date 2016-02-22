package net.serverpeon.discord.internal;

import net.serverpeon.discord.DiscordClient;
import org.junit.Test;

public class JavaCompatTest {
    @Test
    public void testBuilderAccess() {
        DiscordClient.Companion.newBuilder().token("hello").build();
    }
}
