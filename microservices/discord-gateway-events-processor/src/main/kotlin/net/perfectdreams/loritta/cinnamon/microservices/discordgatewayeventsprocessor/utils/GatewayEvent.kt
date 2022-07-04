package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils

/**
 * Wraps a Discord Gateway Event JSON string.
 *
 * Because gateway events can be super large, the [jsonAsString] variable can be set to null, indicating that the string can be garbage collected.
 */
class GatewayEvent(var jsonAsString: String?)