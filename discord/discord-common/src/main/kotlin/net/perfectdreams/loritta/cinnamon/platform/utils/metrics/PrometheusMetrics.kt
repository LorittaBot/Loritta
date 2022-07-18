package net.perfectdreams.loritta.cinnamon.platform.utils.metrics

import io.prometheus.client.Counter
import io.prometheus.client.Histogram
import io.prometheus.client.SimpleCollector

open class PrometheusMetrics(val job: String, val instance: String) {
    private val metrics = mutableListOf<SimpleCollector.Builder<* ,*>>()

    fun createCounterWithLabels(name: String, help: String, vararg labels: String, action: Counter.Builder.() -> (Unit) = {}) = Counter.build(name, help)
        .labelNames(*withMetricLabels(labels))
        .apply(action)
        .also { metrics.add(it) }
        .let { WrappedCounter(this@PrometheusMetrics, it.register()) }

    fun createHistogramWithLabels(name: String, help: String, vararg labels: String, action: Histogram.Builder.() -> (Unit) = {}) = Histogram.build(name, help)
        .labelNames(*withMetricLabels(labels))
        .apply(action)
        .also { metrics.add(it) }
        .let { WrappedHistogram(this@PrometheusMetrics, it.register()) }

    private fun withMetricLabels(labels: Array<out String>) = labels.toMutableList().apply {
        this.addAll(0, listOf("job", "instance"))
    }.toTypedArray()

    internal fun withMetricsLabelValues(labels: Array<out String>) = labels.toMutableList().apply {
        this.addAll(0, listOf(job, instance))
    }.toTypedArray()

    class WrappedCounter(private val metrics: PrometheusMetrics, private val counter: Counter) {
        fun labels(vararg labels: String) = counter.labels(*metrics.withMetricsLabelValues(labels))
    }

    class WrappedHistogram(private val metrics: PrometheusMetrics, private val histogram: Histogram) {
        fun labels(vararg labels: String) = histogram.labels(*metrics.withMetricsLabelValues(labels))
    }
}