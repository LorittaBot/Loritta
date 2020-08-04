package net.perfectdreams.loritta.plugin.loribroker.utils

import java.lang.RuntimeException

/**
 * Used when the [TradingViewRelayConnector] data is outdated, happens when the last received
 * packet was sent a long time ago.
 */
class OutdatedStocksDataException(message: String) : RuntimeException(message)