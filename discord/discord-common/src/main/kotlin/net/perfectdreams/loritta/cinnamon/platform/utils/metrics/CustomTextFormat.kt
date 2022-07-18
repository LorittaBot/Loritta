package net.perfectdreams.loritta.cinnamon.platform.utils.metrics

import io.prometheus.client.Collector
import io.prometheus.client.Collector.MetricFamilySamples
import java.io.IOException
import java.io.Writer
import java.util.*

/**
 * A custom Prometheus TextFormat that adds the job and instance to every label
 */
class CustomTextFormat(
    private val job: String,
    private val instance: String
) {
    // From Prometheus "TextFormat" class
    /**
     * Write out the text version 0.0.4 of the given MetricFamilySamples.
     */
    @Throws(IOException::class)
    fun write004(writer: Writer, mfs: Enumeration<MetricFamilySamples>) {
        val omFamilies: MutableMap<String, MetricFamilySamples> = TreeMap()
        /* See http://prometheus.io/docs/instrumenting/exposition_formats/
     * for the output format specification. */
        while (mfs.hasMoreElements()) {
            val metricFamilySamples = mfs.nextElement()
            val name = metricFamilySamples.name
            writer.write("# HELP ")
            writer.write(name)
            if (metricFamilySamples.type == Collector.Type.COUNTER) {
                writer.write("_total")
            }
            if (metricFamilySamples.type == Collector.Type.INFO) {
                writer.write("_info")
            }
            writer.write(' '.code)
            writeEscapedHelp(writer, metricFamilySamples.help)
            writer.write('\n'.code)
            writer.write("# TYPE ")
            writer.write(name)
            if (metricFamilySamples.type == Collector.Type.COUNTER) {
                writer.write("_total")
            }
            if (metricFamilySamples.type == Collector.Type.INFO) {
                writer.write("_info")
            }
            writer.write(' '.code)
            writer.write(typeString(metricFamilySamples.type))
            writer.write('\n'.code)
            val createdName = name + "_created"
            val gcountName = name + "_gcount"
            val gsumName = name + "_gsum"
            for (sample in metricFamilySamples.samples) {
                /* OpenMetrics specific sample, put in a gauge at the end. */
                if (sample.name == createdName || sample.name == gcountName || sample.name == gsumName) {
                    var omFamily = omFamilies[sample.name]
                    if (omFamily == null) {
                        omFamily = MetricFamilySamples(
                            sample.name,
                            Collector.Type.GAUGE,
                            metricFamilySamples.help,
                            ArrayList()
                        )
                        omFamilies[sample.name] = omFamily
                    }
                    omFamily.samples.add(sample)
                    continue
                }
                writer.write(sample.name)
                val labelNames = sample.labelNames
                    .toMutableList()
                    .apply {
                        this.addAll(0, listOf("job", "instance"))
                    }
                val labelValues = sample.labelValues
                    .toMutableList()
                    .apply {
                        this.addAll(0, listOf(job, instance))
                    }
                if (labelNames.size > 0) {
                    writer.write('{'.code)
                    for (i in labelNames.indices) {
                        writer.write(labelNames[i])
                        writer.write("=\"")
                        writeEscapedLabelValue(writer, labelValues[i])
                        writer.write("\",")
                    }
                    writer.write('}'.code)
                }
                writer.write(' '.code)
                writer.write(Collector.doubleToGoString(sample.value))
                if (sample.timestampMs != null) {
                    writer.write(' '.code)
                    writer.write(sample.timestampMs.toString())
                }
                writer.write('\n'.code)
            }
        }
        // Write out any OM-specific samples.
        if (omFamilies.isNotEmpty()) {
            write004(writer, Collections.enumeration(omFamilies.values))
        }
    }

    @Throws(IOException::class)
    private fun writeEscapedHelp(writer: Writer, s: String) {
        for (i in 0 until s.length) {
            val c = s[i]
            when (c) {
                '\\' -> writer.append("\\\\")
                '\n' -> writer.append("\\n")
                else -> writer.append(c)
            }
        }
    }

    @Throws(IOException::class)
    private fun writeEscapedLabelValue(writer: Writer, s: String) {
        for (i in 0 until s.length) {
            val c = s[i]
            when (c) {
                '\\' -> writer.append("\\\\")
                '\"' -> writer.append("\\\"")
                '\n' -> writer.append("\\n")
                else -> writer.append(c)
            }
        }
    }

    private fun typeString(t: Collector.Type): String {
        return when (t) {
            Collector.Type.GAUGE -> "gauge"
            Collector.Type.COUNTER -> "counter"
            Collector.Type.SUMMARY -> "summary"
            Collector.Type.HISTOGRAM -> "histogram"
            Collector.Type.GAUGE_HISTOGRAM -> "histogram"
            Collector.Type.STATE_SET -> "gauge"
            Collector.Type.INFO -> "gauge"
            else -> "untyped"
        }
    }
}