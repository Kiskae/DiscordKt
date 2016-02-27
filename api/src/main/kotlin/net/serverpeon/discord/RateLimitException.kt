package net.serverpeon.discord

import java.time.Duration

/**
 * Exception thrown if Discord indicates the API has reached its rate limit.
 *
 * @property retryAfter Amount of time the client should wait before sending any more messages.
 */
abstract class RateLimitException(msg: String, val retryAfter: Duration)
: RuntimeException("$msg - Retry after: $retryAfter")