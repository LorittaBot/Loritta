package net.perfectdreams.loritta.cinnamon.platform.utils.giveaway

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorWrapper

class EndGiveawayTask(
    private val loritta: LorittaCinnamon
) : Runnable {
    companion object {
        private val logger = KotlinLogging.logger {}
        private const val weekInMilliseconds = 604800000
    }

    override fun run() {
        runBlocking {
            val giveawaysToFinish = loritta.services.giveaways.getActiveGiveaways()

            giveawaysToFinish.forEach {
                /* if (it.finishAt <= System.currentTimeMillis() - weekInMilliseconds) {
                    it.deleteGiveaway()

                    return@forEach
                } */

                if (it.finishAt <= System.currentTimeMillis()) {
                    // TODO: Fix this workaround, while this does work, it isn't that good
                    val serverConfig = loritta.services.serverConfigs.getServerConfigRoot(
                        it.guildId.toULong()
                    )?.data ?: SlashCommandExecutorWrapper.NonGuildServerConfigRoot

                    // Patches and workarounds!!!
                    val localeId = when (serverConfig.localeId) {
                        "default" -> "pt"
                        "en-us" -> "en"
                        else -> serverConfig.localeId
                    }

                    GiveawayManager.finishGiveaway(
                        it,
                        loritta.rest,
                        loritta.languageManager.getI18nContextById(localeId),
                        null
                    )
                }
            }
        }
    }
}