package net.perfectdreams.loritta.platform.twitter.utils.config

import com.typesafe.config.ConfigFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import net.perfectdreams.loritta.common.utils.ConfigUtils
import java.io.File

@Serializable
class TwitterConfig(
    val consumerKey: String,
    val consumerSecret: String,
    val accessToken: String,
    val accessTokenSecret: String
)

@OptIn(ExperimentalSerializationApi::class)
fun ConfigUtils.parseTwitterConfig(): TwitterConfig {
    val file = File("twitter.conf")
    if (file.exists().not()) {
        file.createNewFile()
        file.writeBytes(TwitterConfig::class.java.getResourceAsStream("/twitter.conf").readAllBytes())
    }
    return Hocon {}.decodeFromConfig(ConfigFactory.parseFile(File("twitter.conf")))
}