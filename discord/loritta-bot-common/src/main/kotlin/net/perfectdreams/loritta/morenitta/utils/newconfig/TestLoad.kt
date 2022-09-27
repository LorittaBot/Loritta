package net.perfectdreams.loritta.morenitta.utils.newconfig

import com.typesafe.config.ConfigFactory
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import java.io.File

fun main() {
    val hocon = Hocon {}
    val r = hocon.decodeFromConfig<BaseConfig>(ConfigFactory.parseFile(File("./loritta.conf")))
    println(r)
}