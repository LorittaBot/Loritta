package net.perfectdreams.loritta.utils.metrics

import io.prometheus.client.Collector
import io.prometheus.client.Counter
import io.prometheus.client.Gauge
import io.prometheus.client.Histogram
import jdk.jfr.EventSettings
import jdk.jfr.consumer.RecordedEvent
import jdk.jfr.consumer.RecordedObject
import jdk.jfr.consumer.RecordingStream
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Consumer

/**
 * Exports values from Java Flight Recorder (JFR) to Prometheus
 *
 * From Mantaro: https://github.com/Mantaro/MantaroBot/blob/master/src/main/java/net/kodehawa/mantarobot/utils/exporters/JFRExports.java
 */
object JFRExports {
    private val log: Logger = LoggerFactory.getLogger(JFRExports::class.java)
    private val REGISTERED: AtomicBoolean = AtomicBoolean(false)
    private const val NANOSECONDS_PER_SECOND = 1E9

    //jdk.SafepointBegin, jdk.SafepointStateSynchronization, jdk.SafepointEnd
    private val SAFEPOINTS: Histogram = Histogram.build()
            .name("jvm_safepoint_pauses_seconds")
            .help("Safepoint pauses by buckets")
            .labelNames("type") // ttsp, operation
            .buckets(0.005, 0.010, 0.025, 0.050, 0.100, 0.200, 0.400, 0.800, 1.600, 3.0, 5.0, 10.0)
            .create()
    private val SAFEPOINTS_TTSP: Histogram.Child = SAFEPOINTS.labels("ttsp")
    private val SAFEPOINTS_OPERATION: Histogram.Child = SAFEPOINTS.labels("operation")

    //jdk.GarbageCollection
    private val GC_PAUSES: Histogram = Histogram.build()
            .name("jvm_gc_pauses_seconds")
            .help("Longest garbage collection pause per collection")
            .labelNames("name", "cause")
            .buckets(0.005, 0.010, 0.025, 0.050, 0.100, 0.200, 0.400, 0.800, 1.600, 3.0, 5.0, 10.0)
            .create()

    //jdk.GarbageCollection
    private val GC_PAUSES_SUM: Histogram = Histogram.build()
            .name("jvm_gc_sum_of_pauses_seconds")
            .help("Sum of garbage collection pauses per collection")
            .labelNames("name", "cause")
            .buckets(0.005, 0.010, 0.025, 0.050, 0.100, 0.200, 0.400, 0.800, 1.600, 3.0, 5.0, 10.0)
            .create()

    //jdk.GCReferenceStatistics
    private val REFERENCE_STATISTICS: Counter = Counter.build()
            .name("jvm_reference_statistics")
            .help("Number of java.lang.ref references by type")
            .labelNames("type")
            .create()

    //jdk.ExecuteVMOperation
    private val VM_OPERATIONS: Counter = Counter.build()
            .name("jvm_vm_operations")
            .help("Executed VM operations")
            .labelNames("operation", "safepoint")
            .create()

    //jdk.NetworkUtilization
    private val NETWORK_READ = Gauge.build()
            .name("jvm_network_read")
            .help("Bits read from the network per second")
            .labelNames("interface")
            .create()

    //jdk.NetworkUtilization
    private val NETWORK_WRITE = Gauge.build()
            .name("jvm_network_write")
            .help("Bits written to the network per second")
            .labelNames("interface")
            .create()

    //jdk.JavaThreadStatistics
    private val THREADS_CURRENT = Gauge.build()
            .name("jvm_threads_current")
            .help("Current thread count of the JVM")
            .create()

    //jdk.JavaThreadStatistics
    private val THREADS_DAEMON = Gauge.build()
            .name("jvm_threads_daemon")
            .help("Daemon thread count of the JVM")
            .create()

    //jdk.CPULoad
    private val CPU_USER = Gauge.build()
            .name("jvm_cpu_user")
            .help("User CPU usage of the JVM")
            .create()

    //jdk.CPULoad
    private val CPU_SYSTEM = Gauge.build()
            .name("jvm_cpu_system")
            .help("System CPU usage of the JVM")
            .create()

    //jdk.CPULoad
    private val CPU_MACHINE = Gauge.build()
            .name("jvm_cpu_machine")
            .help("CPU usage of the machine the JVM is running on")
            .create()

    //jdk.GCHeapSummary, jdk.MetaspaceSummary
    private val MEMORY_USAGE = Gauge.build() // remove _jfr suffix if we remove the standard exports
            .name("jvm_memory_bytes_used_jfr")
            .help("Bytes of memory used by the JVM")
            .labelNames("area") //heap, nonheap
            .create()
    private val MEMORY_USAGE_HEAP = MEMORY_USAGE.labels("heap")
    private val MEMORY_USAGE_NONHEAP = MEMORY_USAGE.labels("nonheap")

    fun register() {
        if (!REGISTERED.compareAndSet(false, true)) return
        SAFEPOINTS.register<Histogram>()
        GC_PAUSES.register<Histogram>()
        GC_PAUSES_SUM.register<Histogram>()
        REFERENCE_STATISTICS.register<Counter>()
        VM_OPERATIONS.register<Counter>()
        NETWORK_READ.register<Collector>()
        NETWORK_WRITE.register<Collector>()
        THREADS_CURRENT.register<Collector>()
        THREADS_DAEMON.register<Collector>()
        CPU_USER.register<Collector>()
        CPU_SYSTEM.register<Collector>()
        CPU_MACHINE.register<Collector>()
        MEMORY_USAGE.register<Collector>()
        val rs = RecordingStream()
        rs.setReuse(true)
        rs.setOrdered(true)

        //////////////////////// HOTSPOT INTERNALS ////////////////////////
        /*
         * https://github.com/openjdk/jdk/blob/6fd44901ec8b10e30dd7e25fb7024eb75d1e6042/src/hotspot/share/runtime/safepoint.cpp
         *
         * void SafepointSynchronize::begin() {
         *   EventSafepointBegin begin_event;
         *   SafepointTracing::begin(VMThread::vm_op_type());
         *   Universe::heap()->safepoint_synchronize_begin();
         *   Threads_lock->lock();
         *   int nof_threads = Threads::number_of_threads();
         *   <snip>
         *   EventSafepointStateSynchronization sync_event;
         *   arm_safepoint();
         *   int iterations = synchronize_threads(...);
         *   <snip>
         *   post_safepoint_synchronize_event(...);
         *   <snip>
         *   post_safepoint_begin_event(...);
         *   <snip>
         * }
         *
         * void SafepointSynchronize::end() {
         *   EventSafepointEnd event;
         *
         *   disarm_safepoint();
         *
         *   Universe::heap()->safepoint_synchronize_end();
         *
         *   SafepointTracing::end();
         *
         *   post_safepoint_end_event(event, safepoint_id());
         * }
         *
         * https://github.com/openjdk/jdk/blob/9f334a16401a0a4ae0a06d342f19750f694487cb/src/hotspot/share/gc/shared/collectedHeap.hpp#L202
         *
         *   // Stop and resume concurrent GC threads interfering with safepoint operations
         *   virtual void safepoint_synchronize_begin() {}
         *   virtual void safepoint_synchronize_end() {}
         */
        /*
         * EventSafepointStateSynchronization starts at roughly the same time java threads
         * start getting paused (by arm_safepoint()), while EventSafepointBegin also includes
         * time to stop concurrent gc threads and acquire Threads_lock.
         *
         * EventSafepointEnd start is roughly the time java threads *start* getting resumed,
         * but it's end is after java threads are done being resumed.
         */

        // time to safepoint
        val ttsp = LongLongRingBuffer(16)
        val safepointDuration = LongLongRingBuffer(16)

        /*
         * jdk.SafepointBegin {
         *   startTime = 23:18:00.149
         *   duration = 53,3 ms
         *   safepointId = 32
         *   totalThreadCount = 16
         *   jniCriticalThreadCount = 0
         * }
         */event(rs, "jdk.SafepointBegin", Consumer<RecordedEvent> { e -> logTTSP(ttsp, e) })

        /*
         * jdk.SafepointStateSynchronization {
         *   startTime = 16:11:44.439
         *   duration = 0,0155 ms
         *   safepointId = 6
         *   initialThreadCount = 0
         *   runningThreadCount = 0
         *   iterations = 1
         * }
         */
        //jdk.SafepointStateSynchronization starts after jdk.SafepointBegin,
        //but gets posted before, so add to the buffer here and flip the order
        //of the subtraction when calculating the time diff
        event(rs, "jdk.SafepointStateSynchronization", Consumer<RecordedEvent> { e ->
            ttsp.add(e.getLong("safepointId"), nanoTime(e.getStartTime()))
            safepointDuration.add(e.getLong("safepointId"), nanoTime(e.getStartTime()))
        })

        /*
         * jdk.SafepointEnd {
         *   startTime = 16:05:45.797
         *   duration = 0,00428 ms
         *   safepointId = 21
         * }
         */
        event(rs, "jdk.SafepointEnd", Consumer<RecordedEvent> { e -> logSafepointOperation(safepointDuration, e) })

        /*
         * jdk.GarbageCollection {
         *   startTime = 23:28:04.913
         *   duration = 7,65 ms
         *   gcId = 1
         *   name = "G1New"
         *   cause = "G1 Evacuation Pause"
         *   sumOfPauses = 7,65 ms
         *   longestPause = 7,65 ms
         * }
         */
        event(rs, "jdk.GarbageCollection", Consumer<RecordedEvent> { e ->
            GC_PAUSES.labels(e.getString("name"), e.getString("cause"))
                    .observe(e.getDuration("longestPause").toNanos() / NANOSECONDS_PER_SECOND)
            GC_PAUSES_SUM.labels(e.getString("name"), e.getString("cause"))
                    .observe(e.getDuration("sumOfPauses").toNanos() / NANOSECONDS_PER_SECOND)
        })

        /*
         * jdk.GCReferenceStatistics {
         *   startTime = 23:36:09.323
         *   gcId = 1
         *   type = "Weak reference"
         *   count = 91
         * }
         */
        event(rs, "jdk.GCReferenceStatistics", Consumer<RecordedEvent> { e -> REFERENCE_STATISTICS.labels(e.getString("type")).inc(e.getLong("count").toDouble()) })

        /*
         * jdk.ExecuteVMOperation {
         *   startTime = 01:03:41.642
         *   duration = 13,4 ms
         *   operation = "G1CollectFull"
         *   safepoint = true
         *   blocking = true
         *   caller = "main" (javaThreadId = 1)
         *   safepointId = 18
         * }
         */
        event(rs, "jdk.ExecuteVMOperation", Consumer<RecordedEvent> { e -> VM_OPERATIONS.labels(e.getString("operation"), java.lang.String.valueOf(e.getBoolean("safepoint"))).inc() })

        /*
         * jdk.NetworkUtilization {
         *   startTime = 23:28:03.716
         *   networkInterface = N/A
         *   readRate = 4,4 kbps
         *   writeRate = 3,3 kbps
         * }
         */
        event(rs, "jdk.NetworkUtilization", Consumer<RecordedEvent> { e ->
            var itf = e.getString("networkInterface")
            if (itf == null) itf = "N/A"
            NETWORK_READ.labels(itf).set(e.getLong("readRate").toDouble())
            NETWORK_WRITE.labels(itf).set(e.getLong("writeRate").toDouble())
        }).withPeriod(Prometheus.UPDATE_PERIOD)

        /*
         * jdk.JavaThreadStatistics {
         *   startTime = 01:13:57.686
         *   activeCount = 12
         *   daemonCount = 10
         *   accumulatedCount = 13
         *   peakCount = 13
         * }
         */
        event(rs, "jdk.JavaThreadStatistics", Consumer<RecordedEvent> { e ->
            val count = e.getLong("activeCount").toDouble()
            THREADS_CURRENT.set(count)
            THREADS_DAEMON.set(e.getLong("daemonCount").toDouble())
        }).withPeriod(Prometheus.UPDATE_PERIOD)

        /*
         * jdk.CPULoad {
         *   startTime = 23:22:50.114
         *   jvmUser = 31,88%
         *   jvmSystem = 8,73%
         *   machineTotal = 40,60%
         * }
         */
        event(rs, "jdk.CPULoad", Consumer<RecordedEvent> { e ->
            val user = e.getFloat("jvmUser").toDouble()
            val system = e.getFloat("jvmSystem").toDouble()
            val machine = e.getFloat("machineTotal").toDouble()
            CPU_USER.set(user)
            CPU_SYSTEM.set(system)
            CPU_MACHINE.set(machine)
        }).withPeriod(Prometheus.UPDATE_PERIOD)

        /*
         * jdk.GCHeapSummary {
         *   startTime = 01:35:46.792
         *   gcId = 19
         *   when = "After GC"
         *   heapSpace = {
         *     start = 0x701600000
         *     committedEnd = 0x702400000
         *     committedSize = 14,0 MB
         *     reservedEnd = 0x800000000
         *     reservedSize = 4,0 GB
         *   }
         *   heapUsed = 6,3 MB
         * }
         */
        event(rs, "jdk.GCHeapSummary", Consumer<RecordedEvent> { e -> MEMORY_USAGE_HEAP.set(e.getLong("heapUsed").toDouble()) })

        /*
         * jdk.MetaspaceSummary {
         *   startTime = 01:49:47.867
         *   gcId = 37
         *   when = "After GC"
         *   gcThreshold = 20,8 MB
         *   metaspace = {
         *     committed = 6,3 MB
         *     used = 5,6 MB
         *     reserved = 1,0 GB
         *   }
         *   dataSpace = {
         *     committed = 5,5 MB
         *     used = 5,0 MB
         *     reserved = 8,0 MB
         *   }
         *   classSpace = {
         *     committed = 768,0 kB
         *     used = 579,4 kB
         *     reserved = 1,0 GB
         *   }
         * }
         */
        event(rs, "jdk.MetaspaceSummaryX", Consumer<RecordedEvent> { e ->
            val amt = (getNestedUsed(e, "metaspace")
                    + getNestedUsed(e, "dataSpace")
                    + getNestedUsed(e, "classSpace"))
            MEMORY_USAGE_NONHEAP.set(amt.toDouble())
        }).withPeriod(Prometheus.UPDATE_PERIOD)

        // start AsyncInfoMonitor data collection
        rs.startAsync()
    }

    private fun event(rs: RecordingStream, name: String, c: Consumer<RecordedEvent>): EventSettings {
        //default to no stacktrace
        val s = rs.enable(name).withoutStackTrace()
        rs.onEvent(name, c)
        return s
    }

    private fun nanoTime(instant: Instant): Long {
        return instant.toEpochMilli() * 1000000L + instant.getNano()
    }

    private fun getNestedUsed(event: RecordedEvent, field: String): Long {
        return event.getValue<RecordedObject>(field).getLong("used")
    }

    private fun logTTSP(buffer: LongLongRingBuffer, event: RecordedEvent) {
        val id = event.getLong("safepointId")
        val time = buffer.remove(id)
        if (time == -1L) {
            //safepoint lost, buffer overwrote it
            //this shouldn't happen unless we get a
            //massive amount of safepoints at once
            log.error("Safepoint with id {} lost", id)
        } else {
            //the buffer contains the time of the synchronize event,
            //because that's what gets posted first, but the start event
            //stats before
            val elapsed = time - nanoTime(event.startTime)
            SAFEPOINTS_TTSP.observe(elapsed / NANOSECONDS_PER_SECOND)
        }
    }

    private fun logSafepointOperation(buffer: LongLongRingBuffer, event: RecordedEvent) {
        val id = event.getLong("safepointId")
        val time = buffer.remove(id)
        if (time == -1L) {
            //safepoint lost, buffer overwrote it
            //this shouldn't happen unless we get a
            //massive amount of safepoints at once
            log.error("Safepoint with id {} lost", id)
        } else {
            val elapsed = nanoTime(event.endTime) - time
            SAFEPOINTS_OPERATION.observe(elapsed / NANOSECONDS_PER_SECOND)
        }
    }

    private class LongLongRingBuffer internal constructor(size: Int) {
        private val table: LongArray
        private val size: Int
        private var index = -1
        fun add(id: Long, value: Long) {
            val idx = inc(index, size).also { index = it } * 2
            table[idx] = id
            table[idx + 1] = value
        }

        fun remove(id: Long): Long {
            for (i in 0 until size) {
                val idx = i * 2
                if (table[idx] == id) {
                    table[idx] = -1
                    return table[idx + 1]
                }
            }
            return -1
        }

        companion object {
            private fun inc(i: Int, modulus: Int): Int {
                var i = i
                if (++i >= modulus) i = 0
                return i
            }
        }

        init {
            table = LongArray(size * 2)
            this.size = size
            Arrays.fill(table, -1)
        }
    }
}