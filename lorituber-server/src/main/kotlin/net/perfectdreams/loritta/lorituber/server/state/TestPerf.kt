package net.perfectdreams.loritta.lorituber.server.state

import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import net.perfectdreams.loritta.lorituber.LoriTuberVibes
import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentCategory
import net.perfectdreams.loritta.lorituber.server.state.data.LoriTuberVideoData
import java.util.*
import kotlin.time.measureTimedValue

fun main() {
    val uuid = UUID.randomUUID()
    val videos = (0 until 100_000).map {
        LoriTuberVideoData(
            uuid,
            "Loritta is so cute!",
            true,
            0,
            LoriTuberVideoContentCategory.GAMES,
            10,
            10,
            10,
            10,
            LoriTuberVibes(0),
            LoriTuberVibes(0),
            0,
            0,
            0,
            mapOf(),
            listOf()
        )
    }

    while (true) {
        val json = measureTimedValue {
            var alloc = 0
            for (video in videos) {
                alloc += Json.encodeToString(video).length
            }
            alloc
        }
        println("JSON: $json")

        val protobuf = measureTimedValue {
            var alloc = 0
            for (video in videos) {
                alloc += ProtoBuf.encodeToByteArray(video).size
            }
            alloc
        }
        println("Protobuf: $protobuf")

        val cbor = measureTimedValue {
            var alloc = 0
            for (video in videos) {
                alloc += Cbor.encodeToByteArray(video).size
            }
            alloc
        }
        println("CBOR: $cbor")
    }
}