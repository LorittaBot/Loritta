package net.perfectdreams.loritta.plugin.loribroker.utils

import java.lang.RuntimeException

/**
 * Used when the [TradingViewRelayConnector] is disconnected from the relay.
 */
class DisconnectedRelayException(message: String) : RuntimeException(message)