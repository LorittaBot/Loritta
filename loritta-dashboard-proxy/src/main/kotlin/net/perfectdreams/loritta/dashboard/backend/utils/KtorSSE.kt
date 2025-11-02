package net.perfectdreams.loritta.dashboard.backend.utils

import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.cacheControl
import io.ktor.server.response.header
import io.ktor.server.response.respondBytesWriter
import io.ktor.sse.ServerSentEvent
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.flow.Flow

/**
 * Method that responds an [ApplicationCall] by reading all the [SseEvent]s from the specified [eventFlow] [Flow]
 * and serializing them in a way that is compatible with the Server-Sent Events specification.
 *
 * You can read more about it here: https://www.html5rocks.com/en/tutorials/eventsource/basics/
 */
suspend fun ApplicationCall.respondSse(eventFlow: Flow<ServerSentEvent>) {
    // Makes SSE work behind nginx
    // https://stackoverflow.com/a/33414096/7271796
    response.header("X-Accel-Buffering", "no")
    response.cacheControl(CacheControl.NoCache(null))
    respondBytesWriter(contentType = ContentType.Text.EventStream) {
        eventFlow.collect { event ->
            writeSseEvent(event)
            flush()
        }
    }
}

/**
 * Writes an SSE event to a [ByteWriteChannel], does not flush the data
 */
suspend fun ByteWriteChannel.writeSseEvent(event: ServerSentEvent) {
    if (event.id != null) {
        writeStringUtf8("id: ${event.id}\n")
    }
    if (event.event != null) {
        writeStringUtf8("event: ${event.event}\n")
    }
    val data = event.data
    if (data != null) {
        for (dataLine in data.lines()) {
            writeStringUtf8("data: $dataLine\n")
        }
    }
    writeStringUtf8("\n")
}