package net.perfectdreams.loritta.cinnamon.discord.utils.metrics

import io.prometheus.client.Histogram
import java.time.Duration

/**
 * Used to hold Prometheus instrumentations
 */
// TODO: Refactor
object InteractionsMetrics : PrometheusMetrics() {
    val UPDATE_PERIOD = Duration.ofSeconds(5L)

    val EXECUTED_COMMAND_LATENCY_COUNT: Histogram = Histogram.build()
        .name("cinnamon_executed_command_latency")
        .help("Executed Command Latency")
        .labelNames("command", "executor")
        .buckets(0.5, 1.0, 2.0, 3.0, 4.0, 5.0, 7.5, 10.0, 15.0, 30.0)
        .create()

    val EXECUTED_SELECT_MENU_LATENCY_COUNT: Histogram = Histogram.build()
        .name("cinnamon_executed_select_menu_latency")
        .help("Executed Select Menu Latency")
        .labelNames("command", "executor")
        .buckets(0.5, 1.0, 2.0, 3.0, 4.0, 5.0, 7.5, 10.0, 15.0, 30.0)
        .create()

    val EXECUTED_BUTTON_LATENCY_COUNT: Histogram = Histogram.build()
        .name("cinnamon_executed_button_click_latency")
        .help("Executed Button Click Latency")
        .labelNames("command", "executor")
        .buckets(0.5, 1.0, 2.0, 3.0, 4.0, 5.0, 7.5, 10.0, 15.0, 30.0)
        .create()

    val EXECUTED_AUTOCOMPLETE_LATENCY_COUNT: Histogram = Histogram.build()
        .name("cinnamon_executed_autocomplete_latency")
        .help("Executed Autocomplete Latency")
        .labelNames("handler")
        .buckets(0.5, 1.0, 2.0, 3.0, 4.0, 5.0, 7.5, 10.0, 15.0, 30.0)
        .create()

    val EXECUTED_MODAL_SUBMIT_LATENCY_COUNT: Histogram = Histogram.build()
        .name("cinnamon_executed_modal_submit_latency")
        .help("Executed Modal Submit Latency")
        .labelNames("command", "executor")
        .buckets(0.5, 1.0, 2.0, 3.0, 4.0, 5.0, 7.5, 10.0, 15.0, 30.0)
        .create()

    fun registerInteractions() {
        EXECUTED_COMMAND_LATENCY_COUNT.register<Histogram>()
        EXECUTED_SELECT_MENU_LATENCY_COUNT.register<Histogram>()
        EXECUTED_BUTTON_LATENCY_COUNT.register<Histogram>()
        EXECUTED_AUTOCOMPLETE_LATENCY_COUNT.register<Histogram>()
        EXECUTED_MODAL_SUBMIT_LATENCY_COUNT.register<Histogram>()
    }
}