package net.perfectdreams.loritta.platform.interaktions

import kotlinx.coroutines.runBlocking
import net.perfectdreams.discordinteraktions.InteractionsServer
import net.perfectdreams.loritta.commands.`fun`.CoinFlipExecutor
import net.perfectdreams.loritta.commands.`fun`.RateWaifuExecutor
import net.perfectdreams.loritta.commands.`fun`.VemDeZapExecutor
import net.perfectdreams.loritta.commands.`fun`.declarations.CoinFlipCommand
import net.perfectdreams.loritta.commands.`fun`.declarations.RateWaifuCommand
import net.perfectdreams.loritta.commands.`fun`.declarations.VemDeZapCommand
import net.perfectdreams.loritta.commands.misc.PingAyayaExecutor
import net.perfectdreams.loritta.commands.misc.PingExecutor
import net.perfectdreams.loritta.commands.misc.declarations.PingCommand
import net.perfectdreams.loritta.commands.utils.AnagramExecutor
import net.perfectdreams.loritta.commands.utils.CalculatorExecutor
import net.perfectdreams.loritta.commands.utils.ChooseExecutor
import net.perfectdreams.loritta.commands.utils.ECBManager
import net.perfectdreams.loritta.commands.utils.MoneyExecutor
import net.perfectdreams.loritta.commands.utils.declarations.AnagramCommand
import net.perfectdreams.loritta.commands.utils.declarations.CalculatorCommand
import net.perfectdreams.loritta.commands.utils.declarations.ChooseCommand
import net.perfectdreams.loritta.commands.utils.declarations.MoneyCommand
import net.perfectdreams.loritta.common.LorittaBot
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleManager
import net.perfectdreams.loritta.platform.interaktions.emotes.DiscordEmoteManager
import java.io.File

class LorittaInteraKTions : LorittaBot() {
    val config = File("token.txt").readLines()
    val interactions = InteractionsServer(
        applicationId = config[2].toLong(),
        publicKey = config[1],
        token = config[0]
    )
    val commandManager = CommandManager(this, interactions.commandManager)
    override val emotes = Emotes(
        DiscordEmoteManager(
            mapOf("chino_ayaya" to "discord:a:chino_AYAYA:696984642594537503")
        )
    )
    val localeManager = LocaleManager(
        File("L:\\RandomProjects\\LorittaInteractions\\locales")
    )

    fun start() {
        localeManager.loadLocales()

        commandManager.register(
            PingCommand,
            PingExecutor(),
            PingAyayaExecutor(emotes)
        )

        commandManager.register(
            CoinFlipCommand,
            CoinFlipExecutor(emotes, random)
        )

        commandManager.register(
            RateWaifuCommand,
            RateWaifuExecutor(emotes)
        )

        commandManager.register(
            CalculatorCommand,
            CalculatorExecutor(emotes)
        )

        commandManager.register(
            AnagramCommand,
            AnagramExecutor(emotes)
        )

        commandManager.register(
            MoneyCommand,
            MoneyExecutor(emotes, ECBManager())
        )

        commandManager.register(
            ChooseCommand,
            ChooseExecutor(emotes)
        )

        commandManager.register(
            VemDeZapCommand,
            VemDeZapExecutor(emotes, random)
        )

        runBlocking {
            commandManager.convertToInteraKTions(
                localeManager.getLocaleById("default")
            )
        }

        interactions.start()
    }
}