package net.perfectdreams.loritta.morenitta.profile

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.nullArray
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import dev.kord.core.entity.User
import dev.kord.rest.Image
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.dv8tion.jda.api.entities.User.UserFlag
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.utils.*
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageFormatType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils.toByteArray
import net.perfectdreams.loritta.cinnamon.discord.utils.images.readImageFromResources
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.profile.badges.*
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.dao.ProfileDesign
import net.perfectdreams.loritta.morenitta.dao.ProfileSettings
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.gifs.GifSequenceWriter
import net.perfectdreams.loritta.morenitta.profile.profiles.*
import net.perfectdreams.loritta.morenitta.utils.*
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.imageio.stream.FileImageOutputStream

class ProfileDesignManager(val loritta: LorittaBot) {
	companion object {
		private val FREE_EMOJIS_GUILDS = listOf(
			Snowflake(297732013006389252), // Apartamento da Loritta
			Snowflake(320248230917046282), // SparklyPower
			Snowflake(417061847489839106), // Rede Dark
			Snowflake(769892417025212497), // Kuraminha's House
			Snowflake(769030809159073795)  // Saddest of the Sads
		)

		val I18N_BADGES_PREFIX = I18nKeysData.Profiles.Badges
	}

	val designs = mutableListOf<ProfileCreator>()
	val defaultProfileDesign: StaticProfileCreator
		get() = designs.first { it.internalName == ProfileDesign.DEFAULT_PROFILE_DESIGN_ID } as StaticProfileCreator

	val badges = mutableListOf<Badge>()

	fun registerBadge(badge: Badge) {
		if (badges.any { it.id == badge.id })
			error("Badge with ID ${badge.id} already exists!")

		badges.add(badge)
	}

	fun registerDesign(design: ProfileCreator) {
		designs.removeIf { it.internalName == design.internalName }
		designs.add(design)
	}

	init {
		registerDesign(NostalgiaProfileCreator.NostalgiaDarkProfileCreator(loritta))
		registerDesign(NostalgiaProfileCreator.NostalgiaBlurpleProfileCreator(loritta))
		registerDesign(NostalgiaProfileCreator.NostalgiaRedProfileCreator(loritta))
		registerDesign(NostalgiaProfileCreator.NostalgiaBlueProfileCreator(loritta))
		registerDesign(NostalgiaProfileCreator.NostalgiaGreenProfileCreator(loritta))
		registerDesign(NostalgiaProfileCreator.NostalgiaPurpleProfileCreator(loritta))
		registerDesign(NostalgiaProfileCreator.NostalgiaPinkProfileCreator(loritta))
		registerDesign(NostalgiaProfileCreator.NostalgiaOrangeProfileCreator(loritta))
		registerDesign(NostalgiaProfileCreator.NostalgiaYellowProfileCreator(loritta))
		registerDesign(NostalgiaProfileCreator.NostalgiaEaster2023ProfileCreator(loritta))
		registerDesign(DebugProfileCreator(loritta))
		registerDesign(DefaultProfileCreator(loritta))
		registerDesign(MSNProfileCreator(loritta))
		registerDesign(OrkutProfileCreator(loritta))
		registerDesign(PlainProfileCreator.PlainWhiteProfileCreator(loritta))
		registerDesign(PlainProfileCreator.PlainOrangeProfileCreator(loritta))
		registerDesign(PlainProfileCreator.PlainPurpleProfileCreator(loritta))
		registerDesign(PlainProfileCreator.PlainAquaProfileCreator(loritta))
		registerDesign(PlainProfileCreator.PlainGreenProfileCreator(loritta))
		registerDesign(PlainProfileCreator.PlainGreenHeartsProfileCreator(loritta))
		registerDesign(CowboyProfileCreator(loritta))
		registerDesign(NextGenProfileCreator(loritta))
		registerDesign(MonicaAtaProfileCreator(loritta))
		registerDesign(UndertaleProfileCreator(loritta))
		registerDesign(LoriAtaProfileCreator(loritta))
		registerDesign(Halloween2019ProfileCreator(loritta))
		registerDesign(Christmas2019ProfileCreator(loritta))
		registerDesign(LorittaChristmas2019ProfileCreator(loritta))

		// ===[ DISCORD USER FLAGS BADGES ]===
		registerBadge(DiscordUserFlagBadge.DiscordPartnerBadge())
		registerBadge(DiscordUserFlagBadge.DiscordVerifiedDeveloperBadge())
		registerBadge(DiscordUserFlagBadge.DiscordHypesquadEventsBadge())
		registerBadge(DiscordUserFlagBadge.DiscordEarlySupporterBadge())
		registerBadge(DiscordUserFlagBadge.DiscordBraveryHouseBadge())
		registerBadge(DiscordUserFlagBadge.DiscordBrillianceHouseBadge())
		registerBadge(DiscordUserFlagBadge.DiscordBalanceHouseBadge())
		registerBadge(DiscordUserFlagBadge.DiscordActiveDeveloperBadge())
		registerBadge(DiscordUserFlagBadge.DiscordModeratorProgramAlumniBadge())
		registerBadge(DiscordUserFlagBadge.DiscordStaffBadge())

		registerBadge(DiscordNitroBadge(loritta))

		registerBadge(ArtistBadge(loritta))

		registerBadge(MerchBuyerBadge(loritta))
		registerBadge(HalloweenBadge(loritta))
		registerBadge(Christmas2019Badge(loritta))
		registerBadge(Christmas2022Badge(loritta))
		registerBadge(Easter2023Badge(loritta))
		registerBadge(GabrielaBadge(loritta))
		registerBadge(PantufaBadge(loritta))
		registerBadge(PremiumBadge(loritta))
		registerBadge(SuperPremiumBadge(loritta))
		registerBadge(MarriedBadge(loritta))
		registerBadge(GrassCutterBadge(loritta))
		registerBadge(SparklyMemberBadge(loritta))
		registerBadge(LorittaStaffBadge(loritta))
		registerBadge(SparklyStaffBadge(loritta))
	}

	suspend fun createProfile(
		loritta: LorittaBot,
		i18nContext: I18nContext,
		locale: BaseLocale,
		sender: ProfileUserInfoData,
		userToBeViewed: ProfileUserInfoData,
		guild: ProfileGuildInfoData?
	): ProfileCreationResult {
		val userProfile = loritta.getOrCreateLorittaProfile(userToBeViewed.id.toLong())
		val profileSettings = loritta.newSuspendedTransaction { userProfile.settings }

		val profileCreator = loritta.profileDesignManager.designs.firstOrNull { it.internalName == (profileSettings.activeProfileDesignInternalName?.value ?: ProfileDesign.DEFAULT_PROFILE_DESIGN_ID) }
			?: loritta.profileDesignManager.defaultProfileDesign

		// We need the mutual guilds to retrieve the user's guild badges.
		// However, because bots can be in a LOT of guilds (causing GC pressure), so we will just return an empty array.
		// Bots could also cause a lot of badges to be downloaded, because they are in a lot of guilds.
		//
		// After all, does it *really* matter that bots won't have any badges? ¯\_(ツ)_/¯
		val mutualGuildsInAllClusters = if (userToBeViewed.isBot)
			setOf()
		else
			loritta.pudding.transaction {
				GuildProfiles.slice(GuildProfiles.guildId)
					.select { GuildProfiles.userId eq userToBeViewed.id.toLong() and (GuildProfiles.isInGuild eq true) }
					.map { it[GuildProfiles.guildId] }
					.toSet()
			}

		val badges = getUserBadgesImages(
			userToBeViewed,
			userProfile,
			mutualGuildsInAllClusters
		)

		val premiumPlan = UserPremiumPlans.getPlanFromValue(loritta.pudding.payments.getActiveMoneyFromDonations(UserId(userProfile.id.value)))

		val allowedDiscordEmojis = if (premiumPlan.customEmojisInAboutMe)
			null // Null = All emojis are allowed
		else {
			// If the user does not have the custom emojis in about me feature, let's allow them to use specific guild's emojis
			// TODO: Fix this
			/* loritta.redisConnection {
				FREE_EMOJIS_GUILDS.flatMap { snowflake ->
					it.hgetAll(loritta.redisKeys.discordGuildEmojis(snowflake))
						.keys
						.map { Snowflake(it) }
				}
			} */
			listOf<Snowflake>()
		}

		val aboutMe = profileSettings.aboutMe ?: i18nContext.get(I18nKeysData.Profiles.DefaultAboutMe)

		val modifiedAboutMe = aboutMe
			// Discord Relative Time Formatting
			.replace(Regex("<t:(\\d+):R>")) {
				val epochSecond = it.groupValues[1].toLong()
				DateUtils.formatDiscordLikeRelativeDate(i18nContext, epochSecond * 1_000, System.currentTimeMillis())
			}

		val (imageAsByteArray, imageFormat) = when (profileCreator) {
			is StaticProfileCreator -> {
				Pair(
					profileCreator.create(
						sender,
						userToBeViewed,
						userProfile,
						guild,
						badges,
						locale,
						i18nContext,
						loritta.getUserProfileBackground(userProfile),
						modifiedAboutMe,
						allowedDiscordEmojis
					).toByteArray(ImageFormatType.PNG),
					ImageFormat.PNG
				)
			}
			is AnimatedProfileCreator -> {
				val images = profileCreator.create(
					sender,
					userToBeViewed,
					userProfile,
					guild,
					badges,
					locale,
					i18nContext,
					loritta.getUserProfileBackground(userProfile),
					modifiedAboutMe,
					allowedDiscordEmojis
				)

				// Montar a GIF
				val fileName = LorittaBot.TEMP + "profile-" + System.currentTimeMillis() + ".gif"

				val output = FileImageOutputStream(File(fileName))
				val writer = GifSequenceWriter(output, BufferedImage.TYPE_INT_ARGB, 10, true)

				for (image in images)
					writer.writeToSequence(image)

				writer.close()
				output.close()

				val outputFile = File(fileName)
				loritta.gifsicle.optimizeGIF(outputFile)

				Pair(outputFile.readBytes(), ImageFormat.GIF)
			}
			else -> error("Unsupported Profile Creator Type $profileCreator")
		}

		return ProfileCreationResult(
			imageAsByteArray,
			userProfile,
			profileSettings,
			allowedDiscordEmojis,
			aboutMe,
			modifiedAboutMe,
			imageFormat
		)
	}

	fun transformUserToProfileUserInfoData(user: net.dv8tion.jda.api.entities.User) = ProfileUserInfoData(
		Snowflake(user.idLong),
		user.name,
		user.discriminator,
		user.effectiveAvatarUrl,
		user.isBot,
		user.flags
	)

	fun transformUserToProfileUserInfoData(user: User) = ProfileUserInfoData(
		user.id,
		user.username,
		user.discriminator,
		user.effectiveAvatar
			.cdnUrl
			.toUrl {
				this.format = Image.Format.PNG
			},
		user.isBot,
		UserFlag.getFlags(user.publicFlags?.code ?: 0)
	)

	fun transformUserToProfileUserInfoData(cachedUserInfo: CachedUserInfo, profileSettings: ProfileSettings) = ProfileUserInfoData(
		Snowflake(cachedUserInfo.id),
		cachedUserInfo.name,
		cachedUserInfo.discriminator,
		cachedUserInfo.effectiveAvatarUrl,
		false,
		UserFlag.getFlags(profileSettings.discordAccountFlags),
	)

	fun transformGuildToProfileGuildInfoData(guild: net.dv8tion.jda.api.entities.Guild) = ProfileGuildInfoData(
		Snowflake(guild.idLong),
		guild.name,
		guild.iconUrl,
	)

	fun transformGuildToProfileGuildInfoData(guild: Guild) = ProfileGuildInfoData(
		guild.id,
		guild.name,
		guild.getIconUrl(Image.Format.PNG)
	)

	/**
	 * Gets the user's badges, the user's mutual guilds will be retrieved
	 *
	 * @param userId                 the user
	 * @param profile                the user's profile
	 * @param mutualGuilds           the user's mutual guilds IDs
	 * @param failIfClusterIsOffline if true, the method will throw a [ClusterOfflineException] if the queried cluster is offline
	 * @return a list containing all the images of the user's badges
	 */
	suspend fun getUserBadgesImages(
		user: ProfileUserInfoData,
		profile: Profile,
		mutualGuilds: Set<Long>,
		failIfClusterIsOffline: Boolean = false
	): List<BufferedImage> {
		val userId = user.id.toLong()

		val hasUpvoted = loritta.newSuspendedTransaction {
			net.perfectdreams.loritta.morenitta.tables.BotVotes.select {
				net.perfectdreams.loritta.morenitta.tables.BotVotes.userId eq userId.toLong() and (net.perfectdreams.loritta.morenitta.tables.BotVotes.votedAt greaterEq System.currentTimeMillis() - (Constants.ONE_HOUR_IN_MILLISECONDS * 12))
			}.count() != 0L
		}

		val hasNotifyMeRoleJob = GlobalScope.async(loritta.coroutineDispatcher) { hasRole(userId, Constants.PORTUGUESE_SUPPORT_GUILD_ID, 334734175531696128, failIfClusterIsOffline) }
		val isLorittaPartnerJob = GlobalScope.async(loritta.coroutineDispatcher) { hasRole(userId, Constants.PORTUGUESE_SUPPORT_GUILD_ID, 434512654292221952, failIfClusterIsOffline) }
		val isTranslatorJob = GlobalScope.async(loritta.coroutineDispatcher) { hasRole(userId, Constants.PORTUGUESE_SUPPORT_GUILD_ID, 385579854336360449, failIfClusterIsOffline) }
		val isGitHubContributorJob = GlobalScope.async(loritta.coroutineDispatcher) { hasRole(userId, Constants.PORTUGUESE_SUPPORT_GUILD_ID, 505144985591480333, failIfClusterIsOffline) }

		val hasNotifyMeRole = hasNotifyMeRoleJob.await()
		val isLorittaPartner = isLorittaPartnerJob.await()
		val isTranslator = isTranslatorJob.await()
		val isGitHubContributor = isGitHubContributorJob.await()

		val badges = mutableListOf<BufferedImage>()

		badges.addAll(
			getUserBadges(user, profile, mutualGuilds)
				.mapNotNull {
					it.getImage()
				}
		)

		// if (hasFanArt) badges += ImageIO.read(File(LorittaBot.ASSETS + "sticker_badge.png"))
		if (isLorittaPartner) badges += ImageIO.read(File(LorittaBot.ASSETS + "lori_hype.png"))
		if (isTranslator) badges += ImageIO.read(File(LorittaBot.ASSETS + "translator.png"))
		if (isGitHubContributor) badges += ImageIO.read(File(LorittaBot.ASSETS + "github_contributor.png"))

		if (userId == 249508932861558785L || userId == 336892460280315905L)
			badges += ImageIO.read(File(LorittaBot.ASSETS + "loritta_sweater.png"))

		if (hasNotifyMeRole) badges += ImageIO.read(File(LorittaBot.ASSETS + "notify_me.png"))
		if (userId == loritta.config.loritta.discord.applicationId.toLong()) badges += ImageIO.read(File(LorittaBot.ASSETS + "loritta_badge.png"))
		if (user.isBot) badges += readImageFromResources("/badges/bot.png")
		val marriage = loritta.newSuspendedTransaction { profile.marriage }
		if (marriage != null) {
			if (System.currentTimeMillis() - marriage.marriedSince > 2_592_000_000) {
				badges += ImageIO.read(File(LorittaBot.ASSETS + "blob_snuggle.png"))
			}
		}
		if (hasUpvoted) badges += ImageIO.read(File(LorittaBot.ASSETS + "upvoted_badge.png"))

		return badges
	}

	/**
	 * Gets the user's badges, the user's mutual guilds will be retrieved
	 *
	 * @param userId                 the user
	 * @param profile                the user's profile
	 * @param mutualGuilds           the user's mutual guilds IDs
	 * @param failIfClusterIsOffline if true, the method will throw a [ClusterOfflineException] if the queried cluster is offline
	 * @return a list containing all the images of the user's badges
	 */
	suspend fun getUserBadges(
		user: ProfileUserInfoData,
		profile: Profile,
		mutualGuilds: Set<Long>,
		failIfClusterIsOffline: Boolean = false
	): List<Badge> {
		val dssNamespace = loritta.dreamStorageService.getCachedNamespaceOrRetrieve()

		val guildBadges = mutableListOf<Badge.GuildBadge>()

		loritta.newSuspendedTransaction {
			val results = (net.perfectdreams.loritta.morenitta.tables.ServerConfigs innerJoin net.perfectdreams.loritta.morenitta.tables.DonationConfigs)
				.select {
					net.perfectdreams.loritta.morenitta.tables.DonationConfigs.customBadge eq true and (net.perfectdreams.loritta.morenitta.tables.ServerConfigs.id inList mutualGuilds)
				}

			val configs = ServerConfig.wrapRows(results)

			for (config in configs) {
				val donationKeysValue = config.getActiveDonationKeysValueNested()
				val badgeFile = config.donationConfig?.customBadgeFile
				val badgeMediaType = config.donationConfig?.customBadgePreferredMediaType
				if (ServerPremiumPlans.getPlanFromValue(donationKeysValue).hasCustomBadge && badgeFile != null && badgeMediaType != null) {
					guildBadges.add(
						Badge.GuildBadge(
							loritta,
							config.guildId,
							I18N_BADGES_PREFIX.Guild.Title,
							I18N_BADGES_PREFIX.Guild.Description(config.guildId.toString()),
							badgeFile,
							badgeMediaType,
							dssNamespace,
							10
						)
					)
				}
			}
		}

		return (guildBadges + loritta.profileDesignManager.badges)
			.filter { it.checkIfUserDeservesBadge(user, profile, mutualGuilds) }
			.sortedByDescending { it.priority }
	}

	/**
	 * Checks if the user has the role in the specified guild
	 *
	 * @param guildId the guild ID
	 * @param roleId  the role ID
	 * @return if the user has the role
	 */
	suspend fun hasRole(userId: Long, guildId: Long, roleId: Long, failIfClusterIsOffline: Boolean = false): Boolean {
		val cluster = DiscordUtils.getLorittaClusterForGuildId(loritta, guildId)

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

		return usersWithRoles.contains(userId.toString())
	}

	class ProfileCreationResult(
		val image: ByteArray,
		val userProfile: Profile,
		val profileSettings: ProfileSettings,
		val allowedDiscordEmojis: List<Snowflake>?,
		val aboutMe: String,
		val modifiedAboutMe: String,
		val imageFormat: ImageFormat
	)
}