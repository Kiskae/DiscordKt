package net.serverpeon.discord.model

import rx.Observable
import java.util.concurrent.CompletableFuture

/**
 * Root of the Discord data model, represents all the channels and guilds associated with the user that the client
 * is logged in as.
 */
interface ClientModel {
    /**
     * Retrieve the list of guilds that the client's user is a part of.
     */
    fun guilds(): Observable<Guild>

    /**
     * Looks up a guild by its unique ID, always returns either one or zero results.
     */
    fun getGuildById(id: DiscordId<Guild>): Observable<Guild>

    /**
     * Creates a new guild named [name].
     *
     * @param name Name of the new server, must be between 2-100 characters long.
     * @param region Region in which the server should be located. Regions can be retrieved through
     *               [getAvailableServerRegions].
     */
    fun createGuild(name: String, region: Region): CompletableFuture<Guild>

    /**
     * Attempt to look up a user, if the user has not been encountered in a guild/DM then this method will fail
     * to return a result.
     */
    fun getUserById(id: DiscordId<User>): Observable<User>

    /**
     * Retrieves a list of all direct messaging chats that the client's user is participating in.
     */
    fun privateChannels(): Observable<Channel.Private>

    /**
     * Looks up a channel by its id. This can be both private channels as well as public channels belonging to a guild.
     */
    fun getChannelById(id: DiscordId<Channel>): Observable<Channel>

    /**
     * Look up a private channel with the given id.
     */
    fun getPrivateChannelById(id: DiscordId<Channel>): Observable<Channel.Private>

    /**
     * Look up a private channel by the id of the recipient.
     */
    fun getPrivateChannelByUser(userId: DiscordId<User>): Observable<Channel.Private>

    /**
     * Access a list of all available server regions.
     * This is used to change the region of a server in [Guild.edit].
     */
    fun getAvailableServerRegions(): Observable<Region>

    /**
     * Attempt to retrieve a channel invite from the given [code_or_url].
     *
     * Accepted forms are:
     * * "discord.gg" invite URL
     * * Invite code associated with the invite
     * * xkcd-based human readable invite code.
     *
     * @param code_or_url The string from which to extract the invite.
     */
    fun getInvite(code_or_url: String): Observable<Invite>
}