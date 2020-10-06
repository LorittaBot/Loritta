package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.nullArray
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.gifs.GifSequenceWriter
import com.mrpowergamerbr.loritta.profile.ProfileUserInfoData
import com.mrpowergamerbr.loritta.tables.DonationConfigs
import com.mrpowergamerbr.loritta.tables.ServerConfigs
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.tables.BannedUsers
import net.perfectdreams.loritta.tables.BotVotes
import net.perfectdreams.loritta.utils.ClusterOfflineException
import net.perfectdreams.loritta.utils.DiscordUtils
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.ServerPremiumPlans
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.imageio.stream.FileImageOutputStream

class PerfilCommand : AbstractCommand("profile", listOf("perfil"), CommandCategory.SOCIAL) {
	companion object {
		/**
		 * Gets the user's badges, the user's mutual guilds will be retrieved
		 *
		 * @param user                   the user
		 * @param profile                the user's profile
		 * @param failIfClusterIsOffline if true, the method will throw a [ClusterOfflineException] if the queried cluster is offline
		 * @return a list containing all the images of the user's badges
		 */
		suspend fun getUserBadges(user: User, profile: Profile, failIfClusterIsOffline: Boolean = false): List<BufferedImage> {
			val mutualGuilds = try {
				lorittaShards.queryMutualGuildsInAllLorittaClusters(user.id)
			} catch (e: ClusterOfflineException) {
				if (failIfClusterIsOffline)
					throw e
				listOf<JsonObject>()
			}

			return getUserBadges(user, profile, mutualGuilds, failIfClusterIsOffline)
		}

		/**
		 * Gets the user's badges, the user's mutual guilds will be retrieved
		 *
		 * @param user                   the user
		 * @param profile                the user's profile
		 * @param mutualGuilds           the user's mutual guilds, retrieved via [LorittaShards.queryMutualGuildsInAllLorittaClusters]
		 * @param failIfClusterIsOffline if true, the method will throw a [ClusterOfflineException] if the queried cluster is offline
		 * @return a list containing all the images of the user's badges
		 */
		suspend fun getUserBadges(user: User, profile: Profile, mutualGuilds: List<JsonObject>, failIfClusterIsOffline: Boolean = false): List<BufferedImage> {
			/**
			 * Checks if the user has the role in the specified guild
			 *
			 * @param guildId the guild ID
			 * @param roleId  the role ID
			 * @return if the user has the role
			 */
			suspend fun hasRole(guildId: String, roleId: String): Boolean {
				val cluster = DiscordUtils.getLorittaClusterForGuildId(guildId.toLong())

				val usersWithRolesPayload = try {
					lorittaShards.queryCluster(cluster, "/api/v1/guilds/$guildId/users-with-any-role/$roleId")
							.await()
							.obj
				} catch (e: ClusterOfflineException) {
					if (failIfClusterIsOffline)
						throw e
					return false
				}

				val membersArray = usersWithRolesPayload["members"].nullArray ?: return false

				val usersWithRoles = membersArray.map { it["id"].string }

				return usersWithRoles.contains(user.id)
			}

			val hasUpvoted = loritta.newSuspendedTransaction {
				BotVotes.select {
					BotVotes.userId eq user.idLong and (BotVotes.votedAt greaterEq System.currentTimeMillis() - (Constants.ONE_HOUR_IN_MILLISECONDS * 12))
				}.count() != 0L
			}

			val hasNotifyMeRoleJob = GlobalScope.async(loritta.coroutineDispatcher) { hasRole(Constants.PORTUGUESE_SUPPORT_GUILD_ID, "334734175531696128") }
			val isLorittaPartnerJob = GlobalScope.async(loritta.coroutineDispatcher) { hasRole(Constants.PORTUGUESE_SUPPORT_GUILD_ID, "434512654292221952") }
			val isTranslatorJob = GlobalScope.async(loritta.coroutineDispatcher) { hasRole(Constants.PORTUGUESE_SUPPORT_GUILD_ID, "385579854336360449") }
			val isGitHubContributorJob = GlobalScope.async(loritta.coroutineDispatcher) { hasRole(Constants.PORTUGUESE_SUPPORT_GUILD_ID, "505144985591480333") }
			val isPocketDreamsStaffJob = GlobalScope.async(loritta.coroutineDispatcher) { hasRole(Constants.SPARKLYPOWER_GUILD_ID, "332650495522897920") }
			val hasLoriStickerArt = loritta.fanArtArtists.any { it.id == user.id }

			val hasNotifyMeRole = hasNotifyMeRoleJob.await()
			val isLorittaPartner = isLorittaPartnerJob.await()
			val isTranslator = isTranslatorJob.await()
			val isGitHubContributor = isGitHubContributorJob.await()
			val isPocketDreamsStaff = isPocketDreamsStaffJob.await()

			val badges = mutableListOf<BufferedImage>()

			badges.addAll(
					loritta.profileDesignManager.badges.filter { it.checkIfUserDeservesBadge(user, profile, mutualGuilds) }
							.sortedByDescending { it.priority }
							.map {
								ImageIO.read(File(Loritta.ASSETS, it.badgeFileName))
							}
			)

			if (user.lorittaSupervisor) badges += ImageIO.read(File(Loritta.ASSETS + "supervisor.png"))
			if (isPocketDreamsStaff) badges += ImageIO.read(File(Loritta.ASSETS + "pocketdreams_staff.png"))
			if (user.support) badges += ImageIO.read(File(Loritta.ASSETS + "support.png"))
			if (hasLoriStickerArt) badges += ImageIO.read(File(Loritta.ASSETS + "sticker_badge.png"))

			val money = loritta.getActiveMoneyFromDonationsAsync(user.idLong)

			if (money != 0.0) {
				badges += ImageIO.read(File(Loritta.ASSETS + "donator.png"))

				if (money >= 99.99) {
					badges += ImageIO.read(File(Loritta.ASSETS + "super_donator.png"))
				}
			}

			if (isLorittaPartner) badges += ImageIO.read(File(Loritta.ASSETS + "lori_hype.png"))
			if (isTranslator) badges += ImageIO.read(File(Loritta.ASSETS + "translator.png"))
			if (isGitHubContributor) badges += ImageIO.read(File(Loritta.ASSETS + "github_contributor.png"))

			if (user.idLong == 249508932861558785L || user.idLong == 336892460280315905L)
				badges += ImageIO.read(File(Loritta.ASSETS + "loritta_sweater.png"))

			loritta.newSuspendedTransaction {
				var specialCase = false

				val results = if (user.idLong == loritta.discordConfig.discord.clientId.toLong()) { // Como estamos em MUITOS servidores, um in list dá problema! E como a gente é fofis, vamos apenas pegar todos os servidores
					(ServerConfigs innerJoin DonationConfigs)
							.select {
								// Então iremos pegar apenas
								DonationConfigs.customBadge eq true
							}
				} else if (30_000 > mutualGuilds.size) { // Se está em menos de 30k servidores, o PostgreSQL ainda suporta pegar via inList
					(ServerConfigs innerJoin DonationConfigs)
							.select {
								DonationConfigs.customBadge eq true and (ServerConfigs.id inList mutualGuilds.map { it["id"].string.toLong() })
							}
				} else {
					specialCase = true
					// Aqui temos bots grandes demais para suportar, nós *iremos* pegar todos, mas iremos filtrar client side (oof)
					(ServerConfigs innerJoin DonationConfigs)
							.select {
								// Então iremos pegar apenas
								DonationConfigs.customBadge eq true
							}
				}

				val configs = ServerConfig.wrapRows(results)

				for (config in configs) {
					if (specialCase && mutualGuilds.any { it["id"].string.toLong() == config.id.value })
						continue

					val donationKeysValue = config.getActiveDonationKeysValue()
					if (ServerPremiumPlans.getPlanFromValue(donationKeysValue).hasCustomBadge) {
						val badge = LorittaUtils.downloadImage("${loritta.instanceConfig.loritta.website.url}/assets/img/badges/custom/${config.guildId}.png?t=${System.currentTimeMillis()}", bypassSafety = true)

						if (badge != null) {
							badges += badge
						}
					}
				}
			}

			if (hasNotifyMeRole) badges += ImageIO.read(File(Loritta.ASSETS + "notify_me.png"))
			if (user.id == loritta.discordConfig.discord.clientId) badges += ImageIO.read(File(Loritta.ASSETS + "loritta_badge.png"))
			if (user.isBot) badges += ImageIO.read(File(Loritta.ASSETS + "robot_badge.png"))
			val marriage = loritta.newSuspendedTransaction { profile.marriage }
			if (marriage != null) {
				if (System.currentTimeMillis() - marriage.marriedSince > 2_592_000_000) {
					badges += ImageIO.read(File(Loritta.ASSETS + "blob_snuggle.png"))
				}
				badges += ImageIO.read(File(Loritta.ASSETS + "ring.png"))
			}
			if (hasUpvoted) badges += ImageIO.read(File(Loritta.ASSETS + "upvoted_badge.png"))

			return badges
		}
	}

	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["PERFIL_DESCRIPTION"]
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {
		var userProfile = context.lorittaUser.profile

		val contextUser = context.getUserAt(0)
		val user = contextUser ?: context.userHandle

		if (contextUser != null) {
			userProfile = loritta.getOrCreateLorittaProfile(contextUser.id)
		}

		val settings = loritta.newSuspendedTransaction { userProfile.settings }
		val bannedState = userProfile.getBannedState()

		if (contextUser != null && bannedState != null) {
			context.reply(
                    LorittaReply(
                            "${contextUser.asMention} está **banido**",
                            "\uD83D\uDE45"
                    ),
                    LorittaReply(
                            "**Motivo:** `${bannedState[BannedUsers.reason]}`",
                            "✍"
                    )
			)
			return
		}
		if (contextUser == null && context.args.isNotEmpty() && (context.args.first() == "shop" || context.args.first() == "loja")) {
			context.reply(LorittaReply(context.locale["commands.social.profile.profileshop", "${loritta.instanceConfig.loritta.website.url}user/@me/dashboard/profiles"], Emotes.LORI_OWO))
			return
		}

		// Para pegar o "Jogando" do usuário, nós precisamos pegar uma guild que o usuário está
		val mutualGuilds = lorittaShards.getMutualGuilds(user)
		val mutualGuildsInAllClusters = lorittaShards.queryMutualGuildsInAllLorittaClusters(user.id)
		val member = mutualGuilds.firstOrNull()?.getMember(user)
		val badges = getUserBadges(user, userProfile, mutualGuildsInAllClusters)

		var aboutMe: String? = null

		if (userProfile.userId == loritta.discordConfig.discord.clientId.toLong()) {
			aboutMe = locale["PERFIL_LORITTA_DESCRIPTION"]
		}

		if (userProfile.userId == 390927821997998081L) {
			aboutMe = "Olá, eu me chamo Pantufa, sou da equipe do SparklyPower (e eu sou a melhor ajudante de lá! :3), e, é claro, a melhor amiga da Lori!"
		}

		if (settings.aboutMe != null && settings.aboutMe != "A Loritta é minha amiga!") {
			aboutMe = settings.aboutMe
		}

		if (aboutMe == null) {
			aboutMe = "A Loritta é a minha amiga! Sabia que você pode alterar este texto usando \"${context.config.commandPrefix}sobremim\"? :3"
		}

		val activeProfile = settings.activeProfileDesignInternalName?.value ?: "defaultDark"
		val profileCreator = loritta.profileDesignManager.designs.first { it.internalName == activeProfile }

		val background = loritta.getUserProfileBackground(userProfile)

		val senderUserInfo = ProfileUserInfoData(
				context.userHandle.idLong,
				context.userHandle.name,
				context.userHandle.discriminator,
				context.userHandle.effectiveAvatarUrl
		)

		val profileUserInfo = ProfileUserInfoData(
				user.idLong,
				user.name,
				user.discriminator,
				user.effectiveAvatarUrl
		)

		val images = profileCreator.createGif(
				senderUserInfo,
				profileUserInfo,
				userProfile,
				context.guild,
				badges,
				locale,
				background,
				aboutMe,
				member
		)

		if (images.size == 1) {
			context.sendFile(images.first(), "lori_profile.png", "📝 **|** " + context.getAsMention(true) + context.legacyLocale["PEFIL_PROFILE"]) // E agora envie o arquivo
		} else {
			// Montar a GIF
			val fileName = Loritta.TEMP + "profile-" + System.currentTimeMillis() + ".gif"

			val output = FileImageOutputStream(File(fileName))
			val writer = GifSequenceWriter(output, BufferedImage.TYPE_INT_ARGB, 10, true)

			for (image in images)
				writer.writeToSequence(image)

			writer.close()
			output.close()

			val outputFile = File(fileName)
			MiscUtils.optimizeGIF(outputFile)

			context.sendFile(outputFile, "lori_profile.gif", "📝 **|** " + context.getAsMention(true) + context.legacyLocale["PEFIL_PROFILE"]) // E agora envie o arquivo
		}
	}
}