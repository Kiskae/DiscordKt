# DiscordKt [![Build Status](https://travis-ci.org/Kiskae/DiscordKt.svg?branch=master)](https://travis-ci.org/Kiskae/DiscordKt) [ ![Download](https://api.bintray.com/packages/kiskae/maven/discordkt/images/download.svg)](https://bintray.com/kiskae/maven/discordkt/_latestVersion)
An opinionated Discord client written in Kotlin.

## Usage

### Dependencies

The project is split into two subprojects. 
*api* contains the interfaces that the user should use to access the client. 
*impl* contains the implementation of the client.

By using the following set-up the client will only be able to access *api* classes and only make the implementation available during runtime.

**Gradle**
```groovy
repositories {
    maven {
        url "http://dl.bintray.com/kiskae/maven"
    }
}

dependencies {
    compile 'net.serverpeon:discordkt-api:<version>'
    runtime 'net.serverpeon:discordkt-impl:<version>'
}
```

**Maven**
```xml
<repositories>
  <repository>
    <id>bintray-kiskae-maven</id>
    <name>bintray</name>
    <url>http://dl.bintray.com/kiskae/maven</url>
  </repository>
</repositories>
<dependencies>
  <dependency>
    <groupId>net.serverpeon</groupId>
    <artifactId>discordkt-api</artifactId>
    <version>1.0-alpha1</version>
    <type>pom</type>
  </dependency>
  <dependency>
    <groupId>net.serverpeon</groupId>
    <artifactId>discordkt-impl</artifactId>
    <version>1.0-alpha1</version>
    <type>pom</type>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```

**For users of SLF4J**: The implementation contains a number of logging statements that will be very spammy if the logging level is set to `TRACE`.
Adding the following logger configuration to the `log4j2.xml` configuration will present this spam:
```xml
<Logger name="net.serverpeon.discord.internal" level="info" additivity="false">
    <Appenders...>
</Logger>
```

### Creating the Client

The client can be obtained by using the fluent builder created using `DiscordClient.newBuilder()`.
After the client is built it is not connected to Discord until data is accessed or the `DiscordClient#startEmittingEvents()` method is called.

```java
DiscordClient client = DiscordClient.newBuilder()
        .login("hello@discord.gg", "somepassword")
        .build()
```

Using the Netty-inspired `DiscordClient.closeFuture()` 'closing' future the user can block until the client is closed.
This is usually applied in the following way:

```java
public void main(String[] args) {
  DiscordClient client = ...
  
  // Add event listeners
  client.eventBus.register(...);
  
  // Connect to discord
  client.startEmittingEvents();
  
  // Block until completion or exception.
  client.closeFuture().await();
}
```

### Observing events

Events can be observed by listening on the client's [EventBus](http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/eventbus/EventBus.html)

A full listing of events can be found here: [net.serverpeon.discord.event](https://github.com/Kiskae/DiscordKt/tree/master/api/src/main/kotlin/net/serverpeon/discord/event)

```java
DiscordClient client = ...

client.eventBus().register(new Object() {
    @Subscribe
    public void onMessage(MessageCreateEvent event) {
      // Called each time a new message is posted to a channel the user is in.
    }
});

// Triggers connection to Discord, accessing the model will do the same thing.
client.startEmittingEvents();
```

### Accessing data

Almost all data can be accessed through Observable collections representing the relationships between objects.

In the following example find a server with the name "<Servername>" and print all text channels within that guild:

```java
DiscordClient client = ...

client.guilds()
        .first(g -> "<ServerName>".equals(g.getName()))
        .flatMap(Guild::getChannels)
        .filter(c -> c.getType() == Channel.Type.TEXT)
        .subscribe(c -> {
            System.out.printf("Guild: %s - Channel: %s%n", c.getGuild().getName(), c.getName());
        });
```

All observables are also lazy; this means that it can be used multiple times and it will always return the most up-to-date answer at that time.

```java
Observable<Channel.Public> publicChannels = client.guilds()
        .filter(c -> "<ServerName>".equals(c.getName()))
        .flatMap(Guild::getChannels);

publicChannels.subscribe(System.out::println);

// undetermined amount of time later

publicChannels.subscribe(System.out::println);
```

More examples can be found for [Java](https://github.com/Kiskae/DiscordKt/tree/master/src/main/java/net/serverpeon/discord/samples) and [Kotlin](https://github.com/Kiskae/DiscordKt/tree/master/src/main/kotlin/net/serverpeon/discord/samples).

## Opinionated Design

The primary motivation writing this client is the fact that many of the existing Java clients/wrappers do not encapsulate the asynchronous nature of the api very well.
This client deals with these issues by encapsulating all of the data with well-defined asynchronous libraries.

- **RxJava** Encapsulates all relationships between objects in the data-model. This means all relations are lazy by default.
If the user saves a filtered `Observable<Channel.Public>` derived from the channels in a specific guild then whenever this observable is subscribed it will have an up-to-date list.
- **CompletableFuture** Any actions that are immediately executed are represented as *Java8 CompletableFutures*. This means the user can asynchronously listen for the completion or failure of the action without blocking the thread.
- **EventBus** *Guava*'s EventBus is a well-known and battle-tested eventbus implementation. It allows for the easy subscription and publishing of arbitrary objects and listeners.
- **Retrofit** *(Implementation)* Used to wrap the Discord REST api and provide a type-safe interface for the actions it presents.
- **javax.websocket** *(Implementation)* A standard and efficient websocket implementation based on Glassfish's Grizzly client.

## TODO

- Reimplement retries after disconnecting from discord's servers.
- Determine how to best implement the voice interfaces.
- Export more information from the events. (https://github.com/Kiskae/DiscordKt/issues/2)
- Should presence updates be exposed as events? Discord is quite spammy with these.
