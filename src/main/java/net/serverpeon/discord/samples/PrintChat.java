package net.serverpeon.discord.samples;

import com.google.common.eventbus.Subscribe;
import net.serverpeon.discord.DiscordClient;
import net.serverpeon.discord.event.message.MessageCreateEvent;
import net.serverpeon.discord.model.Channel;

public class PrintChat {
    public void main(String[] args) {
        // Actually call login() or token() in your code
        DiscordClient client = DiscordClient.newBuilder().build();

        // Register an event listener to the client's EventBus
        client.eventBus().register(new Object() {
            @Subscribe
            public void onMessage(MessageCreateEvent event) {
                System.out.printf("[%s] %s: %s%n",
                        mapChannelName(event.getChannel()),
                        event.getMessage().getAuthor().getUsername(),
                        event.getMessage().getContent());
            }
        });

        // Tell the client to connect to discord, accessing any of ClientModel's methods
        // will also do this
        client.startEmittingEvents();

        // Block on the closeFuture
        client.closeFuture().await();
    }

    private String mapChannelName(Channel.Text channel) {
        if (channel instanceof Channel.Public) {
            Channel.Public pubChannel = (Channel.Public) channel;
            return String.format("%s#%s", pubChannel.getGuild().getName(), pubChannel.getName());
        } else {
            Channel.Private privChannel = (Channel.Private) channel;
            return privChannel.getRecipient().getUsername();
        }
    }
}
