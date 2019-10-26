package net.perfectdreams.loritta

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.coroutines.*
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Icon
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.config.FanArtArtist
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull
import java.io.File

class ChangeBanner(val m: QuirkyStuff, val config: QuirkyConfig) {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	var task: Job? = null
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

		if (currentBannerIndex + 1 > config.changeBanner.banners.size)
			currentBannerIndex = 0

		logger.info { "Current banner index is $currentBannerIndex"}

		val randomBanner = bannersFolder.listFiles().first { it.name == config.changeBanner.banners[currentBannerIndex] }

		val artistId = randomBanner.nameWithoutExtension.split("-").first()

		val fanArtArtist = loritta.fanArtArtists.first { it.id == artistId }
		logger.info { "Using banner $randomBanner by $fanArtArtist"}

		currentBannerIndex++
		currentBannerIndexFile.writeText(currentBannerIndex.toString())

		return Pair(randomBanner, fanArtArtist)
	}

	fun start() {
		logger.info { "Starting Banner Changing Task..." }
		task = GlobalScope.launch(LorittaLauncher.loritta.coroutineDispatcher) {
			while (true) {
				val currentMillisRelativeToTheCurrentHour = System.currentTimeMillis() % config.changeBanner.timeMod
				logger.info { "Banner will be changed in ${currentMillisRelativeToTheCurrentHour}ms!"}
				delay(config.changeBanner.timeMod - currentMillisRelativeToTheCurrentHour) // Vamos esperar até a próxima hora!

				changeBanner()
			}
		}
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

			artistUser = lorittaShards.getUserById(discordId)
		}

		randomBanner!!

		for (guildId in config.changeBanner.guilds) {
			val guild = lorittaShards.getGuildById(guildId) ?: continue

			val icon = withContext(Dispatchers.IO) {
				Icon.from(
						randomBanner.first
				)
			}

			guild.manager.setBanner(icon).await()
		}

		for (channelId in config.changeBanner.channels) {
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