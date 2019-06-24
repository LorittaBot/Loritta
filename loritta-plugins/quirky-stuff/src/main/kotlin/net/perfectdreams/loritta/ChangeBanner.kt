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
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.config.FanArtArtist
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull
import java.io.File

class ChangeBanner(val m: QuirkyStuff, val config: QuirkyConfig) {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	var task: Job? = null

	fun selectRandomBanner(): Pair<File, FanArtArtist> {
		logger.info { "Selecting new banner for the guild..." }
		val bannersFolder = File(m.dataFolder, "banners/")

		var randomBanner: File?
		var fanArtArtist: FanArtArtist?

		do {
			val allBanners = bannersFolder.listFiles().filter { it.extension == "png" }
			logger.info { "There are ${allBanners.size} banners!" }
			randomBanner = allBanners.random()

			val artistId = randomBanner.nameWithoutExtension.split("-").first()
			fanArtArtist = loritta.fanArtArtists.firstOrNull { it.id == artistId }
		} while (randomBanner == null || fanArtArtist == null)

		return Pair(randomBanner, fanArtArtist)
	}

	fun start() {
		logger.info { "Starting Banner Changing Task..." }
		task = GlobalScope.launch(LorittaLauncher.loritta.coroutineDispatcher) {
			while (true) {
				val currentMillisRelativeToTheCurrentHour = System.currentTimeMillis() % config.changeBanner.timeMod
				logger.info { "Banner will be changed in ${currentMillisRelativeToTheCurrentHour}ms!"}
				delay(3_600_000 - currentMillisRelativeToTheCurrentHour) // Vamos esperar até a próxima hora!

				val randomBanner = selectRandomBanner()
				logger.info { "New banner is ${randomBanner.first} by ${randomBanner.second.id}!"}

				val fanArtArtist = randomBanner.second
				val discordId = fanArtArtist.socialNetworks
						?.firstIsInstanceOrNull<FanArtArtist.SocialNetwork.DiscordSocialNetwork>()
						?.id

				val artistUser = lorittaShards.getUserById(discordId)

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
	}
}