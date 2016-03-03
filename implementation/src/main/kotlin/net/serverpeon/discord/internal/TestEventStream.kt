package net.serverpeon.discord.internal

import net.serverpeon.discord.DiscordClient

fun main(args: Array<String>) {
    val token = System.getenv("DISCORD_TOKEN")
    val email = System.getenv("DISCORD_EMAIL")
    val password = System.getenv("DISCORD_PASSWORD")

    val client = DiscordClient.newBuilder().apply {
        if (token != null) {
            token(token)
        } else {
            login(email, password)
        }
    }.retries(0).build()

    // Kick off model update with a dummy model access
    client.guilds().subscribe { guild ->
        guild.roles.subscribe { role ->
            println("${guild.name} / ${role.name} -> ${role.color}")
        }
    }

    client.closeFuture().await()
}