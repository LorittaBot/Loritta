package net.perfectdreams.loritta.plugin.autobannerchanger

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Icon
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.config.FanArtArtist
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull
import java.io.File

class BannerChanger(val loritta: Loritta, val m: AutoBannerChangerPlugin, val config: AutoBannerChangerConfig) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun start(): suspend CoroutineScope.() -> Unit = {
        while (true) {
            val currentMillisRelativeToTheCurrentHour = System.currentTimeMillis() % config.timeMod
            logger.info { "Banner will be changed in ${currentMillisRelativeToTheCurrentHour}ms!" }
            delay(config.timeMod - currentMillisRelativeToTheCurrentHour) // Vamos esperar até a próxima hora!

            try {
                changeBanner()
            } catch (e: Exception) {
                logger.warn(e) { "Something went wrong when updating the banner" }
            }
        }
    }

    var currentBannerIndex = -1

    fun selectRandomBanner(): Pair<File, FanArtArtist> {
        logger.info { "Selecting new banner for the guild..." }
        val bannersFolder = File(m.dataFolder, "banners/")

        val currentBannerIndexFile = File(m.dataFolder, "current_banner_index")
        if (currentBannerIndex == -1) {
            currentBannerIndex = if (currentBannerIndexFile.exists())
                currentBannerIndexFile.readText().toIntOrNull() ?: 0
            else
                0
        }

        if (currentBannerIndex + 1 > config.banners.size)
            currentBannerIndex = 0

        logger.info { "Current banner index is $currentBannerIndex, file name is ${config.banners[currentBannerIndex]}"}

        val randomBanner = bannersFolder.listFiles().first { it.name == config.banners[currentBannerIndex] }

        val artistId = randomBanner.nameWithoutExtension.split("-").first()

        val fanArtArtist = loritta.fanArtArtists.first { it.id == artistId }
        logger.info { "Using banner $randomBanner by $fanArtArtist"}

        currentBannerIndex++
        currentBannerIndexFile.writeText(currentBannerIndex.toString())

        return Pair(randomBanner, fanArtArtist)
    }

    suspend fun changeBanner() {
        var artistUser: User? = null
        var randomBanner: Pair<File, FanArtArtist>? = null

        while (artistUser == null) {
            randomBanner = selectRandomBanner()
            logger.info { "New banner is ${randomBanner.first} by ${randomBanner.second.id}!" }

            val fanArtArtist = randomBanner.second
            val discordId = fanArtArtist.socialNetworks
                    ?.firstIsInstanceOrNull<FanArtArtist.SocialNetwork.DiscordSocialNetwork>()
                    ?.id

            artistUser = lorittaShards.retrieveUserById(discordId)
        }

        randomBanner!!

        for (guildId in config.guilds) {
            val guild = lorittaShards.getGuildById(guildId) ?: continue

            val icon = withContext(Dispatchers.IO) {
                Icon.from(
                        randomBanner.first
                )
            }

            guild.manager.setBanner(icon).await()
        }

        for (channelId in config.channels) {
            val channel = lorittaShards.getTextChannelById(channelId.toString()) ?: continue

            channel.sendMessage(
                    EmbedBuilder()
                            .setThumbnail(artistUser?.effectiveAvatarUrl)
                            .setTitle("Eu alterei o banner! \uD83C\uDFA8")
                            .setDescription("O banner atual foi feito por ${artistUser?.name}! ${Emotes.LORI_HAPPY}")
                            .setColor(Constants.LORITTA_AQUA)
                            .build()
            ).await()
        }
    }
}