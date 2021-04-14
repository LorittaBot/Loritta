package net.perfectdreams.loritta.platform.interaktions

import kotlinx.coroutines.runBlocking
import net.perfectdreams.discordinteraktions.InteractionsServer
import net.perfectdreams.loritta.commands.`fun`.CoinFlipExecutor
import net.perfectdreams.loritta.commands.`fun`.RateWaifuExecutor
import net.perfectdreams.loritta.commands.`fun`.declarations.CoinFlipCommand
import net.perfectdreams.loritta.commands.`fun`.declarations.RateWaifuCommand
import net.perfectdreams.loritta.commands.misc.PingAyayaExecutor
import net.perfectdreams.loritta.commands.misc.PingExecutor
import net.perfectdreams.loritta.commands.misc.declarations.PingCommand
import net.perfectdreams.loritta.common.LorittaBot
import net.perfectdreams.loritta.common.locale.BaseLocale
import java.io.File

class LorittaInteraKTions : LorittaBot() {
    val config = File("token.txt").readLines()
    val interactions = InteractionsServer(
        applicationId = config[2].toLong(),
        publicKey = config[1],
        token = config[0]
    )
    val commandManager = CommandManager(this, interactions.commandManager)

    fun start() {
        commandManager.register(
            PingCommand,
            PingExecutor(),
            PingAyayaExecutor()
        )

        commandManager.register(
            CoinFlipCommand,
            CoinFlipExecutor(random)
        )

        commandManager.register(
            RateWaifuCommand,
            RateWaifuExecutor()
        )

        runBlocking {
            commandManager.convertToInteraKTions(BaseLocale("default", mapOf(), mapOf()))
        }

        interactions.start()
    }
}