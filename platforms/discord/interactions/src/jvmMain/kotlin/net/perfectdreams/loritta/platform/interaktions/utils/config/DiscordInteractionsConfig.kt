package net.perfectdreams.loritta.platform.interaktions.utils.config

import com.typesafe.config.ConfigFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import net.perfectdreams.loritta.common.utils.ConfigUtils
import net.perfectdreams.loritta.discord.LorittaDiscord
import net.perfectdreams.loritta.discord.LorittaDiscordConfig
import java.io.File

@Serializable
class DiscordInteractionsConfig(
    val publicKey: String,
    val registerGlobally: Boolean,
    val guildsToBeRegistered: List<Long>
)

@OptIn(ExperimentalSerializationApi::class)
fun ConfigUtils.parseDiscordInteractionsConfig(): DiscordInteractionsConfig {
    val file = File("discord-interactions.conf")
    if (file.exists().not()) {
        file.createNewFile()
        file.writeBytes(LorittaDiscord::class.java.getResourceAsStream("/discord-interactions.conf").readAllBytes())
    }
    return Hocon {}.decodeFromConfig(ConfigFactory.parseFile(File("discord-interactions.conf")))
}