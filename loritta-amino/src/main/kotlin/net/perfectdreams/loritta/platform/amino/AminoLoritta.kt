package net.perfectdreams.loritta.platform.amino

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.config.GeneralConfig
import com.mrpowergamerbr.loritta.utils.config.GeneralInstanceConfig
import net.perfectdreams.aminoreapi.AminoClientBuilder
import net.perfectdreams.loritta.api.platform.LorittaBot
import net.perfectdreams.loritta.api.platform.PlatformFeature
import net.perfectdreams.loritta.platform.amino.commands.AminoCommandManager
import net.perfectdreams.loritta.platform.amino.listeners.EventListener
import net.perfectdreams.loritta.platform.amino.utils.config.GeneralAminoConfig
import net.perfectdreams.loritta.utils.Emotes

class AminoLoritta(val aminoConfig: GeneralAminoConfig, config: GeneralConfig, instanceConfig: GeneralInstanceConfig) : LorittaBot(config, instanceConfig) {
    override val supportedFeatures = listOf(
            PlatformFeature.IMAGE_UPLOAD
    )

    override val commandManager = AminoCommandManager(this)

    init {
        println("Init stuff...")

        Loritta.ASSETS = instanceConfig.loritta.folders.assets

        Emotes.emoteManager = Emotes.EmoteManager.DefaultEmoteManager()

        loadLocales()
        loadLegacyLocales()

        println("Initialized!")
    }

    suspend fun start() {
        println("Loading plugins...")
        pluginManager.loadPlugins()

        println("Setting up input thread...")

        val client = AminoClientBuilder()
                .setDeviceId(aminoConfig.amino.deviceId)
                .withCredientials(aminoConfig.amino.email, aminoConfig.amino.password)
                .connectToWebSocket(true)
                .enableCache(true)
                .addEventListener(EventListener(this))
                .connect()
    }
}