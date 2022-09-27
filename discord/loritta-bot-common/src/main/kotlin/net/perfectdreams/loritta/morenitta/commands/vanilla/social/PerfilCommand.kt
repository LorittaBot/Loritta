package net.perfectdreams.loritta.morenitta.commands.vanilla.social

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.nullArray
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.gifs.GifSequenceWriter
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import net.perfectdreams.loritta.morenitta.tables.DonationConfigs
import net.perfectdreams.loritta.morenitta.tables.GuildProfiles
import net.perfectdreams.loritta.morenitta.tables.ServerConfigs
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.LorittaUtils
import net.perfectdreams.loritta.morenitta.utils.MiscUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.MediaTypeUtils
import net.perfectdreams.loritta.common.utils.StoragePaths
import net.perfectdreams.loritta.morenitta.tables.BotVotes
import net.perfectdreams.loritta.morenitta.utils.AccountUtils
import net.perfectdreams.loritta.morenitta.utils.ClusterOfflineException
import net.perfectdreams.loritta.morenitta.utils.DiscordUtils
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.utils.ImageFormat
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.morenitta.utils.extensions.getEffectiveAvatarUrl
import net.perfectdreams.loritta.morenitta.utils.extensions.readImage
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.imageio.stream.FileImageOutputStream

class PerfilCommand(loritta: LorittaBot) : AbstractCommand(loritta, "profile", listOf("perfil"), net.perfectdreams.loritta.common.commands.CommandCategory.SOCIAL) {
	companion object {
		/**
		 * Gets the user's badges, the user's mutual guilds will be retrieved
		 *
		 * @param user                   the user
		 * @param profile                the user's profile
		 * @param mutualGuilds           the user's mutual guilds IDs
		 * @param failIfClusterIsOffline if true, the method will throw a [ClusterOfflineException] if the queried cluster is offline
		 * @return a list containing all the images of the user's badges
		 */
		suspend fun getUserBadges(
			loritta: LorittaBot,
			user: User,
			profile: Profile,
			mutualGuilds: Set<Long>,
			failIfClusterIsOffline: Boolean = false
		): List<BufferedImage> {
			/**
			 * Checks if the user has the role in the specified guild
			 *
			 * @param guildId the guild ID
			 * @param roleId  the role ID
			 * @return if the user has the role
			 */
			suspend fun hasRole(loritta: LorittaBot, guildId: String, roleId: String): Boolean {
				val cluster = DiscordUtils.getLorittaClusterForGuildId(loritta, guildId.toLong())

				val usersWithRolesPayload = try {
					loritta.lorittaShards.queryCluster(cluster, "/api/v1/guilds/$guildId/users-with-any-role/$roleId")
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

			val hasNotifyMeRoleJob = GlobalScope.async(loritta.coroutineDispatcher) { hasRole(loritta, Constants.PORTUGUESE_SUPPORT_GUILD_ID, "334734175531696128") }
			val isLorittaPartnerJob = GlobalScope.async(loritta.coroutineDispatcher) { hasRole(loritta, Constants.PORTUGUESE_SUPPORT_GUILD_ID, "434512654292221952") }
			val isTranslatorJob = GlobalScope.async(loritta.coroutineDispatcher) { hasRole(loritta, Constants.PORTUGUESE_SUPPORT_GUILD_ID, "385579854336360449") }
			val isGitHubContributorJob = GlobalScope.async(loritta.coroutineDispatcher) { hasRole(loritta, Constants.PORTUGUESE_SUPPORT_GUILD_ID, "505144985591480333") }
			val isPocketDreamsStaffJob = GlobalScope.async(loritta.coroutineDispatcher) { hasRole(loritta, Constants.SPARKLYPOWER_GUILD_ID, "332650495522897920") }
			val hasLoriStickerArt = loritta.fanArtArtists.any { it.id == user.id }
			val isLoriBodyguardJob = GlobalScope.async(loritta.coroutineDispatcher) { hasRole(loritta, Constants.PORTUGUESE_SUPPORT_GUILD_ID, "351473717194522647") }
			val isLoriSupportJob = GlobalScope.async(loritta.coroutineDispatcher) { hasRole(loritta, Constants.PORTUGUESE_SUPPORT_GUILD_ID, "399301696892829706") }

			val hasNotifyMeRole = hasNotifyMeRoleJob.await()
			val isLorittaPartner = isLorittaPartnerJob.await()
			val isTranslator = isTranslatorJob.await()
			val isGitHubContributor = isGitHubContributorJob.await()
			val isPocketDreamsStaff = isPocketDreamsStaffJob.await()
			val isLoriBodyguard = isLoriBodyguardJob.await()
			val isLoriSupport = isLoriSupportJob.await()

			val badges = mutableListOf<BufferedImage>()

			badges.addAll(
				loritta.profileDesignManager.badges.filter { it.checkIfUserDeservesBadge(user, profile, mutualGuilds) }
					.sortedByDescending { it.priority }
					.map {
						readImage(File(LorittaBot.ASSETS, it.badgeFileName))
					}
			)

			if (isLoriBodyguard) badges += ImageIO.read(File(LorittaBot.ASSETS + "supervisor.png"))
			if (isPocketDreamsStaff) badges += ImageIO.read(File(LorittaBot.ASSETS + "pocketdreams_staff.png"))
			if (isLoriSupport) badges += ImageIO.read(File(LorittaBot.ASSETS + "support.png"))
			if (hasLoriStickerArt) badges += ImageIO.read(File(LorittaBot.ASSETS + "sticker_badge.png"))

			val money = loritta.getActiveMoneyFromDonationsAsync(user.idLong)

			if (money != 0.0) {
				badges += ImageIO.read(File(LorittaBot.ASSETS + "donator.png"))

				if (money >= 99.99) {
					badges += ImageIO.read(File(LorittaBot.ASSETS + "super_donator.png"))
				}
			}

			if (isLorittaPartner) badges += ImageIO.read(File(LorittaBot.ASSETS + "lori_hype.png"))
			if (isTranslator) badges += ImageIO.read(File(LorittaBot.ASSETS + "translator.png"))
			if (isGitHubContributor) badges += ImageIO.read(File(LorittaBot.ASSETS + "github_contributor.png"))

			if (user.idLong == 249508932861558785L || user.idLong == 336892460280315905L)
				badges += ImageIO.read(File(LorittaBot.ASSETS + "loritta_sweater.png"))

			val dssNamespace = loritta.dreamStorageService.getCachedNamespaceOrRetrieve()

			loritta.newSuspendedTransaction {
				val results = (ServerConfigs innerJoin DonationConfigs)
					.select {
						DonationConfigs.customBadge eq true and (ServerConfigs.id inList mutualGuilds)
					}

				val configs = ServerConfig.wrapRows(results)

				for (config in configs) {
					val donationKeysValue = config.getActiveDonationKeysValueNested()
					val badgeFile = config.donationConfig?.customBadgeFile
					val badgeMediaType = config.donationConfig?.customBadgePreferredMediaType
					if (ServerPremiumPlans.getPlanFromValue(donationKeysValue).hasCustomBadge && badgeFile != null && badgeMediaType != null) {
						val extension = MediaTypeUtils.convertContentTypeToExtension(badgeMediaType)
						val badge = LorittaUtils.downloadImage(loritta, "${loritta.config.loritta.dreamStorageService.url}/$dssNamespace/${StoragePaths.CustomBadge(config.guildId, badgeFile).join()}.$extension", bypassSafety = true)

						if (badge != null) {
							badges += badge
						}
					}
				}
			}

			if (hasNotifyMeRole) badges += ImageIO.read(File(LorittaBot.ASSETS + "notify_me.png"))
			if (user.id == loritta.config.loritta.discord.applicationId.toString()) badges += ImageIO.read(File(LorittaBot.ASSETS + "loritta_badge.png"))
			if (user.isBot) badges += ImageIO.read(File(LorittaBot.ASSETS + "robot_badge.png"))
			val marriage = loritta.newSuspendedTransaction { profile.marriage }
			if (marriage != null) {
				if (System.currentTimeMillis() - marriage.marriedSince > 2_592_000_000) {
					badges += ImageIO.read(File(LorittaBot.ASSETS + "blob_snuggle.png"))
				}
				badges += ImageIO.read(File(LorittaBot.ASSETS + "ring.png"))
			}
			if (hasUpvoted) badges += ImageIO.read(File(LorittaBot.ASSETS + "upvoted_badge.png"))

			return badges
		}
	}

	override fun getDescriptionKey() = LocaleKeyData("commands.command.profile.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.profile.examples")

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun getUsage() = arguments {
		argument(ArgumentType.USER) {
			optional = true
		}
	}

	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		var userProfile = context.lorittaUser.profile

		val contextUser = context.getUserAt(0)
		val user = contextUser ?: context.userHandle

		if (contextUser != null) {
			userProfile = loritta.getOrCreateLorittaProfile(contextUser.id)
		}

		if (AccountUtils.checkAndSendMessageIfUserIsBanned(context, userProfile))
			return

		val settings = loritta.newSuspendedTransaction { userProfile.settings }

		if (contextUser == null && context.args.isNotEmpty() && (context.args.first() == "shop" || context.args.first() == "loja")) {
			context.reply(LorittaReply(context.locale["commands.command.profile.profileshop", "${loritta.config.loritta.website.url}user/@me/dashboard/profiles"], Emotes.LORI_OWO))
			return
		}

		// We need the mutual guilds to retrieve the user's guild badges.
		// However because bots can be in a LOT of guilds (causing GC pressure), so we will just return a empty array.
		// Bots could also cause a lot of badges to be downloaded, because they are in a lot of guilds.
		//
		// After all, does it *really* matter that bots won't have any badges? ¯\_(ツ)_/¯
		val mutualGuildsInAllClusters = if (user.isBot)
			setOf()
		else
			loritta.newSuspendedTransaction {
				GuildProfiles.slice(GuildProfiles.guildId)
					.select { GuildProfiles.userId eq user.idLong and (GuildProfiles.isInGuild eq true) }
					.map { it[GuildProfiles.guildId] }
					.toSet()
			}

		val badges = getUserBadges(loritta, user, userProfile, mutualGuildsInAllClusters)

		var aboutMe: String? = null

		if (userProfile.userId == loritta.config.loritta.discord.applicationId.toString().toLong()) {
			aboutMe = locale["commands.command.profile.lorittaDescription"]
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
			context.userHandle
				.getEffectiveAvatarUrl(ImageFormat.PNG)
		)

		val profileUserInfo = ProfileUserInfoData(
			user.idLong,
			user.name,
			user.discriminator,
			user.getEffectiveAvatarUrl(ImageFormat.PNG)
		)

		val images = profileCreator.createGif(
			senderUserInfo,
			profileUserInfo,
			userProfile,
			context.guild,
			badges,
			locale,
			background,
			aboutMe
		)

		if (images.size == 1) {
			context.sendFile(images.first(), "lori_profile.png", "📝 **|** " + context.getAsMention(true) + context.locale["commands.command.profile.profile"]) // E agora envie o arquivo
		} else {
			// Montar a GIF
			val fileName = LorittaBot.TEMP + "profile-" + System.currentTimeMillis() + ".gif"

			val output = FileImageOutputStream(File(fileName))
			val writer = GifSequenceWriter(output, BufferedImage.TYPE_INT_ARGB, 10, true)

			for (image in images)
				writer.writeToSequence(image)

			writer.close()
			output.close()

			val outputFile = File(fileName)
			MiscUtils.optimizeGIF(outputFile)

			context.sendFile(outputFile, "lori_profile.gif", "📝 **|** " + context.getAsMention(true) + context.locale["commands.command.profile.profile"]) // E agora envie o arquivo
		}
	}
}