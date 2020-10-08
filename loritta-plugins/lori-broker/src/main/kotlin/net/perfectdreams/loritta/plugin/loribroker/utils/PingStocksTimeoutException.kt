package net.perfectdreams.loritta.plugin.loribroker.utils

import java.lang.RuntimeException

/**
 * Used when the [TradingViewRelayConnector] didn't receive a ping, happens when the last received
 * packet was sent a long time ago.
 */
class PingStocksTimeoutException(message: String) : RuntimeException(message)