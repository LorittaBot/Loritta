package net.perfectdreams.loritta

import com.mrpowergamerbr.loritta.utils.Constants
import mu.KotlinLogging
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.listeners.AddReactionFurryAminoPtListener
import net.perfectdreams.loritta.listeners.AddReactionListener
import net.perfectdreams.loritta.modules.AddReactionForHeathecliffModule
import net.perfectdreams.loritta.modules.QuirkyModule
import net.perfectdreams.loritta.modules.ThankYouLoriModule
import net.perfectdreams.loritta.platform.discord.legacy.plugin.DiscordPlugin
import net.perfectdreams.loritta.profile.badges.CanecaBadge
import net.perfectdreams.loritta.profile.badges.HalloweenBadge
import net.perfectdreams.loritta.utils.HoconUtils.decodeFromFile
import java.io.File

class QuirkyStuff(name: String, loritta: LorittaBot) : DiscordPlugin(name, loritta) {
    var topDonatorsRank: TopDonatorsRank? = null
    var topVotersRank: TopVotersRank? = null
    var sponsorsAdvertisement: SponsorsAdvertisement? = null

    override fun onEnable() {
        val config = Constants.HOCON.decodeFromFile<QuirkyConfig>(File(dataFolder, "config.conf"))

        if (config.topDonatorsRank.enabled) {
            logger.info { "Top Donators Rank is enabled! Enabling top donators rank stuff... :3"}
            topDonatorsRank = TopDonatorsRank(this, config).apply {
                this.start()
            }
        }

        if (config.topVotersRank.enabled) {
            logger.info { "Top Voters Rank is enabled! Enabling top voters rank stuff... :3"}
            topVotersRank = TopVotersRank(this, config).apply {
                this.start()
            }
        }

        if (config.sponsorsAdvertisement.enabled) {
            logger.info { "Sponsors Advertisement is enabled! Enabling sponsors advertisement stuff... :3"}
            sponsorsAdvertisement = SponsorsAdvertisement(this, config).apply {
                this.start()
            }
        }

        registerEventListeners(
                AddReactionListener(config),
                AddReactionFurryAminoPtListener(config)
        )

        registerMessageReceivedModules(
                QuirkyModule(config),
                ThankYouLoriModule(config),
                AddReactionForHeathecliffModule()
        )

        // ===[ HALLOWEEN 2019 ]===
        // registerCommand(DocesCommand())
        registerBadge(HalloweenBadge())
        registerBadge(CanecaBadge(config))
    }

    override fun onDisable() {
        super.onDisable()
        topDonatorsRank?.task?.cancel()
        topVotersRank?.task?.cancel()
        sponsorsAdvertisement?.task?.cancel()
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}