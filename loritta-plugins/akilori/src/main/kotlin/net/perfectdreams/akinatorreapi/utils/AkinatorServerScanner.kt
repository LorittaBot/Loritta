package net.perfectdreams.akinatorreapi.utils

import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import net.perfectdreams.akinatorreapi.AkinatorClient

fun main() {
    runBlocking {
        val http = AkinatorClient.http.get<String>("https://global3.akinator.com/ws/instances_v2.php?media_id=14&footprint=cd8e6509f3420878e18d75b9831b317f&mode=https")
    }
}