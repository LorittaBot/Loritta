package net.perfectdreams.loritta.cinnamon.platform.utils.metrics

import io.prometheus.client.Histogram
import java.time.Duration

/**
 * Used to hold Prometheus instrumentations
 */
object Prometheus {
    val UPDATE_PERIOD = Duration.ofSeconds(5L)

    val EXECUTED_COMMAND_LATENCY_COUNT = Histogram.build()
        .name("executed_command_latency")
        .help("Executed Command Latency")
        .labelNames("command", "executor")
        .buckets(0.5, 1.0, 2.0, 3.0, 4.0, 5.0, 7.5, 10.0, 15.0, 30.0)
        .create()

    val EXECUTED_SELECT_MENU_LATENCY_COUNT = Histogram.build()
        .name("executed_select_menu_latency")
        .help("Executed Select Menu Latency")
        .labelNames("command", "executor")
        .buckets(0.5, 1.0, 2.0, 3.0, 4.0, 5.0, 7.5, 10.0, 15.0, 30.0)
        .create()

    val EXECUTED_BUTTON_CLICK_LATENCY_COUNT = Histogram.build()
        .name("executed_button_click_latency")
        .help("Executed Button Click Latency")
        .labelNames("command", "executor")
        .buckets(0.5, 1.0, 2.0, 3.0, 4.0, 5.0, 7.5, 10.0, 15.0, 30.0)
        .create()

    val EXECUTED_AUTOCOMPLETE_LATENCY_COUNT = Histogram.build()
        .name("executed_autocomplete_latency")
        .help("Executed Autocomplete Latency")
        .labelNames("command", "executor")
        .buckets(0.5, 1.0, 2.0, 3.0, 4.0, 5.0, 7.5, 10.0, 15.0, 30.0)
        .create()

    fun register() {
        JFRExports.register()

        EXECUTED_COMMAND_LATENCY_COUNT.register<Histogram>()
        EXECUTED_SELECT_MENU_LATENCY_COUNT.register<Histogram>()
        EXECUTED_BUTTON_CLICK_LATENCY_COUNT.register<Histogram>()
        EXECUTED_AUTOCOMPLETE_LATENCY_COUNT.register<Histogram>()
    }
}