package net.perfectdreams.loritta.platform.interaktions.utils.config

import com.typesafe.config.ConfigFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import net.perfectdreams.loritta.common.utils.ConfigUtils
import net.perfectdreams.loritta.discord.LorittaDiscord
import java.io.File

@Serializable
data class PuddingConfig(
    val puddingUrl: String,
    val authorization: String
)

@OptIn(ExperimentalSerializationApi::class)
fun ConfigUtils.parsePuddingConfig(): PuddingConfig {
    val file = File("pudding.conf")
    if (file.exists().not()) {
        file.createNewFile()
        file.writeBytes(LorittaDiscord::class.java.getResourceAsStream("/pudding.conf").readAllBytes())
    }
    return Hocon {}.decodeFromConfig(ConfigFactory.parseFile(File("pudding.conf")))
}