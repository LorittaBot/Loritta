package net.perfectdreams.loritta

import com.fasterxml.jackson.module.kotlin.readValue
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import mu.KotlinLogging
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.commands.BirthdayCommand
import net.perfectdreams.loritta.commands.DocesCommand
import net.perfectdreams.loritta.commands.LoriToolsQuirkyStuffCommand
import net.perfectdreams.loritta.commands.SouTopDoadorCommand
import net.perfectdreams.loritta.listeners.AddReactionForLoriBanListener
import net.perfectdreams.loritta.listeners.AddReactionFurryAminoPtListener
import net.perfectdreams.loritta.listeners.AddReactionListener
import net.perfectdreams.loritta.listeners.GetCandyListener
import net.perfectdreams.loritta.modules.*
import net.perfectdreams.loritta.platform.discord.plugin.DiscordPlugin
import net.perfectdreams.loritta.profile.badges.CanecaBadge
import net.perfectdreams.loritta.profile.badges.HalloweenBadge
import net.perfectdreams.loritta.tables.BoostedCandyChannels
import net.perfectdreams.loritta.tables.CollectedCandies
import net.perfectdreams.loritta.tables.Halloween2019Players
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

class QuirkyStuff(name: String, loritta: LorittaBot) : DiscordPlugin(name, loritta) {
    private val REQUIRED_TO_RECEIVE_DREAM_BOOST = 20.00.toBigDecimal()

    var changeBanner: ChangeBanner? = null
    var topDonatorsRank: TopDonatorsRank? = null
    var topVotersRank: TopVotersRank? = null
    var birthdaysRank: BirthdaysRank? = null
    var sponsorsAdvertisement: SponsorsAdvertisement? = null

    override fun onEnable() {
        val config = Constants.HOCON_MAPPER.readValue<QuirkyConfig>(File(dataFolder, "config.conf"))

        if (config.changeBanner.enabled) {
            logger.info { "Change Banner is enabled! Enabling banner stuff... :3"}
            changeBanner = ChangeBanner(this, config).apply {
                this.start()
            }
        }

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

        birthdaysRank = BirthdaysRank(
                this,
                config
        ).apply {
            this.start()
        }

        registerEventListeners(
                AddReactionListener(config),
                GetCandyListener(config),
                AddReactionForLoriBanListener(config),
                AddReactionFurryAminoPtListener(config)
        )

        registerMessageReceivedModules(
                QuirkyModule(config),
                ThankYouLoriModule(config),
                DropCandyModule(config),
                AddReactionForStaffLoriBanModule(config),
                AddReactionForHeathecliffModule()
        )

        registerCommand(LoriToolsQuirkyStuffCommand(this))
        registerCommand(SouTopDoadorCommand(config))
        registerCommand(BirthdayCommand(this))

        // ===[ HALLOWEEN 2019 ]===
        registerCommand(DocesCommand())
        registerBadge(HalloweenBadge())
        registerBadge(CanecaBadge(config))

        transaction(Databases.loritta) {
            SchemaUtils.createMissingTablesAndColumns(
                    Halloween2019Players,
                    CollectedCandies,
                    BoostedCandyChannels
            )
        }

        onGuildReady { guild, mongoServerConfig ->
            birthdaysRank?.updateBirthdayRank(guild, mongoServerConfig)
        }

        onGuildMemberJoinListeners { member, guild, mongoServerConfig ->
            val shouldBeUpdated = transaction(Databases.loritta) {
                Profile.findById(member.idLong)?.settings?.birthday != null
            }

            if (shouldBeUpdated)
                birthdaysRank?.updateBirthdayRank(guild, mongoServerConfig)
        }

        onGuildMemberLeaveListeners { member, guild, mongoServerConfig ->
            val shouldBeUpdated = transaction(Databases.loritta) {
                Profile.findById(member.idLong)?.settings?.birthday != null
            }

            if (shouldBeUpdated)
                birthdaysRank?.updateBirthdayRank(guild, mongoServerConfig)
        }
    }

    override fun onDisable() {
        super.onDisable()
        changeBanner?.task?.cancel()
        topDonatorsRank?.task?.cancel()
        topVotersRank?.task?.cancel()
        birthdaysRank?.task?.cancel()
        sponsorsAdvertisement?.task?.cancel()
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}