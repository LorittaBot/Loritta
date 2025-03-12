package net.perfectdreams.loritta.morenitta.profile

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.nullArray
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User.UserFlag
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageFormatType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils.toByteArray
import net.perfectdreams.loritta.cinnamon.discord.utils.images.readImageFromResources
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingBackground
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingUserProfile
import net.perfectdreams.loritta.cinnamon.pudding.services.fromRow
import net.perfectdreams.loritta.cinnamon.pudding.tables.BackgroundPayments
import net.perfectdreams.loritta.cinnamon.pudding.tables.Backgrounds
import net.perfectdreams.loritta.cinnamon.pudding.tables.CustomBackgroundSettings
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.MediaTypeUtils
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.StoragePaths
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.dao.ProfileDesign
import net.perfectdreams.loritta.morenitta.dao.ProfileSettings
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.gifs.GifSequenceWriter
import net.perfectdreams.loritta.morenitta.profile.badges.*
import net.perfectdreams.loritta.morenitta.profile.profiles.*
import net.perfectdreams.loritta.morenitta.utils.*
import net.perfectdreams.loritta.morenitta.utils.extensions.readImage
import net.perfectdreams.loritta.serializable.Background
import net.perfectdreams.loritta.serializable.BackgroundStorageType
import net.perfectdreams.loritta.serializable.BackgroundVariation
import net.perfectdreams.loritta.serializable.UserId
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.imageio.stream.FileImageOutputStream

class ProfileDesignManager(val loritta: LorittaBot) {
	companion object {
		private val FREE_EMOJIS_GUILDS = listOf(
			297732013006389252L, // Apartamento da Loritta
			320248230917046282L, // SparklyPower
			417061847489839106L, // Rede Dark
			769892417025212497L, // Kuraminha's House
			769030809159073795L  // Saddest of the Sads
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

		registerDesign(LoriCoolCardsStickerReceivedProfileCreator.LoriCoolCardsStickerReceivedCommonProfileCreator(loritta))
		registerDesign(LoriCoolCardsStickerReceivedProfileCreator.LoriCoolCardsStickerReceivedUncommonProfileCreator(loritta))
		registerDesign(LoriCoolCardsStickerReceivedProfileCreator.LoriCoolCardsStickerReceivedRareProfileCreator(loritta))
		registerDesign(LoriCoolCardsStickerReceivedProfileCreator.LoriCoolCardsStickerReceivedEpicProfileCreator(loritta))
		registerDesign(LoriCoolCardsStickerReceivedProfileCreator.LoriCoolCardsStickerReceivedLegendaryProfileCreator(loritta))
		registerDesign(LoriCoolCardsStickerReceivedProfileCreator.LoriCoolCardsStickerReceivedMythicProfileCreator(loritta))
		registerDesign(LoriCoolCardsStickerReceivedProfileCreator.LoriCoolCardsStickerReceivedCommonUserBackgroundProfileCreator(loritta))
		registerDesign(LoriCoolCardsStickerReceivedProfileCreator.LoriCoolCardsStickerReceivedUncommonUserBackgroundProfileCreator(loritta))
		registerDesign(LoriCoolCardsStickerReceivedProfileCreator.LoriCoolCardsStickerReceivedRareUserBackgroundProfileCreator(loritta))
		registerDesign(LoriCoolCardsStickerReceivedProfileCreator.LoriCoolCardsStickerReceivedEpicUserBackgroundProfileCreator(loritta))
		registerDesign(LoriCoolCardsStickerReceivedProfileCreator.LoriCoolCardsStickerReceivedLegendaryUserBackgroundProfileCreator(loritta))
		registerDesign(LoriCoolCardsStickerReceivedProfileCreator.LoriCoolCardsStickerReceivedMythicUserBackgroundProfileCreator(loritta))

		registerDesign(LoriCoolCardsStickerReceivedPlainProfileCreator.LoriCoolCardsStickerReceivedPlainCommonProfileCreator(loritta))
		registerDesign(LoriCoolCardsStickerReceivedPlainProfileCreator.LoriCoolCardsStickerReceivedPlainUncommonProfileCreator(loritta))
		registerDesign(LoriCoolCardsStickerReceivedPlainProfileCreator.LoriCoolCardsStickerReceivedPlainRareProfileCreator(loritta))
		registerDesign(LoriCoolCardsStickerReceivedPlainProfileCreator.LoriCoolCardsStickerReceivedPlainEpicProfileCreator(loritta))
		registerDesign(LoriCoolCardsStickerReceivedPlainProfileCreator.LoriCoolCardsStickerReceivedPlainLegendaryProfileCreator(loritta))
		registerDesign(LoriCoolCardsStickerReceivedPlainProfileCreator.LoriCoolCardsStickerReceivedPlainMythicProfileCreator(loritta))
		registerDesign(LoriCoolCardsStickerReceivedPlainProfileCreator.LoriCoolCardsStickerReceivedPlainCommonUserBackgroundProfileCreator(loritta))
		registerDesign(LoriCoolCardsStickerReceivedPlainProfileCreator.LoriCoolCardsStickerReceivedPlainUncommonUserBackgroundProfileCreator(loritta))
		registerDesign(LoriCoolCardsStickerReceivedPlainProfileCreator.LoriCoolCardsStickerReceivedPlainRareUserBackgroundProfileCreator(loritta))
		registerDesign(LoriCoolCardsStickerReceivedPlainProfileCreator.LoriCoolCardsStickerReceivedPlainEpicUserBackgroundProfileCreator(loritta))
		registerDesign(LoriCoolCardsStickerReceivedPlainProfileCreator.LoriCoolCardsStickerReceivedPlainLegendaryUserBackgroundProfileCreator(loritta))
		registerDesign(LoriCoolCardsStickerReceivedPlainProfileCreator.LoriCoolCardsStickerReceivedPlainMythicUserBackgroundProfileCreator(loritta))

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

		registerBadge(DiscordNitroBadge(loritta.pudding))

		registerBadge(ArtistBadge(loritta))

		registerBadge(MerchBuyerBadge(loritta))
		registerBadge(HalloweenBadge(loritta.pudding))
		registerBadge(Christmas2019Badge(loritta.pudding))
		registerBadge(Christmas2022Badge(loritta.pudding))
		registerBadge(Easter2023Badge(loritta.pudding))
		registerBadge(GabrielaBadge(loritta.pudding))
		registerBadge(PantufaBadge(loritta.pudding))
		registerBadge(PremiumBadge(loritta))
		registerBadge(SuperPremiumBadge(loritta))
		registerBadge(MarriedBadge(loritta.pudding))
		registerBadge(GrassCutterBadge(loritta.pudding))
		registerBadge(SparklyMemberBadge(loritta))
		registerBadge(LorittaStaffBadge(loritta))
		registerBadge(SparklyStaffBadge(loritta))
		registerBadge(StonksBadge(loritta.pudding))
		registerBadge(StickerFanBadge(loritta.pudding))
		registerBadge(ReactionEventBadge.Halloween2024ReactionEventBadge(loritta.pudding))
		registerBadge(ReactionEventBadge.Halloween2024ReactionEventSuperBadge(loritta.pudding))
		registerBadge(BratBadge(loritta.pudding))
		registerBadge(ReactionEventBadge.Christmas2024ReactionEventBadge(loritta.pudding))
		registerBadge(ReactionEventBadge.Christmas2024ReactionEventSuperBadge(loritta.pudding))
	}

	suspend fun createProfile(
		loritta: LorittaBot,
		i18nContext: I18nContext,
		locale: BaseLocale,
		sender: ProfileUserInfoData,
		userToBeViewed: ProfileUserInfoData,
		guild: ProfileGuildInfoData?,
		profileCreator: ProfileCreator
	): ProfileCreationResult {
		val userProfile = loritta.getOrCreateLorittaProfile(userToBeViewed.id)
		val profileSettings = loritta.newSuspendedTransaction { userProfile.settings }

		// We need the mutual guilds to retrieve the user's guild badges.
		// However, because bots can be in a LOT of guilds (causing GC pressure), so we will just return an empty array.
		// Bots could also cause a lot of badges to be downloaded, because they are in a lot of guilds.
		//
		// After all, does it *really* matter that bots won't have any badges? ¯\_(ツ)_/¯
		val mutualGuildsInAllClusters = if (userToBeViewed.isBot)
			setOf()
		else
			loritta.pudding.transaction {
				GuildProfiles.select(GuildProfiles.guildId)
					.where { GuildProfiles.userId eq userToBeViewed.id and (GuildProfiles.isInGuild eq true) }
					.map { it[GuildProfiles.guildId] }
					.toSet()
			}

		// TODO: This should be removed, use the badgesData list below when all badges are migrated to the new system!
		val badges = getUserBadgesImages(
			userToBeViewed,
			userProfile,
			mutualGuildsInAllClusters
		)

		val badgesData = loritta.profileDesignManager.getUserBadges(
			userToBeViewed,
			userProfile,
			setOf(), // We don't need this
			failIfClusterIsOffline = false // We also don't need this
		)

		val equippedBadge = badgesData.firstOrNull { it.id == profileSettings.activeBadge }

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
			listOf<Long>()
		}

		val aboutMe = profileSettings.aboutMe ?: i18nContext.get(I18nKeysData.Profiles.DefaultAboutMe)

		val modifiedAboutMe = aboutMe
			// Discord Relative Time Formatting
			.replace(Regex("<t:(\\d+):R>")) {
				val epochSecond = it.groupValues[1].toLong()
				DateUtils.formatDiscordLikeRelativeDate(i18nContext, epochSecond * 1_000, System.currentTimeMillis())
			}

		val userProfileBackground = getUserProfileBackground(userProfile, profileCreator)

		val (imageAsByteArray, imageFormat) = when (profileCreator) {
			is StaticProfileCreator -> {
				Pair(
					profileCreator.create(
						sender,
						userToBeViewed,
						userProfile,
						guild,
						badges,
						badgesData,
						equippedBadge,
						locale,
						i18nContext,
						userProfileBackground,
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
					badgesData,
					equippedBadge,
					locale,
					i18nContext,
					userProfileBackground,
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
			is RawProfileCreator -> {
				profileCreator.create(
					sender,
					userToBeViewed,
					userProfile,
					guild,
					badges,
					badgesData,
					equippedBadge,
					locale,
					i18nContext,
					userProfileBackground,
					modifiedAboutMe,
					allowedDiscordEmojis
				)
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
		user.idLong,
		user.name,
		user.discriminator,
		user.effectiveAvatarUrl,
		user.isBot,
		user.flags
	)

	fun transformUserToProfileUserInfoData(cachedUserInfo: CachedUserInfo, profileSettings: ProfileSettings) = ProfileUserInfoData(
		cachedUserInfo.id,
		cachedUserInfo.name,
		cachedUserInfo.discriminator,
		cachedUserInfo.effectiveAvatarUrl,
		false,
		UserFlag.getFlags(profileSettings.discordAccountFlags),
	)

	fun transformGuildToProfileGuildInfoData(guild: Guild): ProfileGuildInfoData {
		if (guild.isDetached) {
			return ProfileGuildInfoData(
				guild.idLong,
				// Fallback to the guild ID if the guild is detached
				guild.id,
				null
			)
		}

		return ProfileGuildInfoData(
			guild.idLong,
			guild.name,
			guild.iconUrl
		)
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
	suspend fun getUserBadgesImages(
		user: ProfileUserInfoData,
		profile: Profile,
		mutualGuilds: Set<Long>,
		failIfClusterIsOffline: Boolean = false
	): List<BufferedImage> {
		// TODO: This is slow, how could we fix this?
		//  I think one of the ways is "dynamically" querying badges in the profile viewer, instead all badges directly
		val userId = user.id.toLong()

		val hasUpvoted = loritta.newSuspendedTransaction {
			net.perfectdreams.loritta.cinnamon.pudding.tables.BotVotes.selectAll().where {
				net.perfectdreams.loritta.cinnamon.pudding.tables.BotVotes.userId eq userId.toLong() and (net.perfectdreams.loritta.cinnamon.pudding.tables.BotVotes.votedAt greaterEq System.currentTimeMillis() - (Constants.ONE_HOUR_IN_MILLISECONDS * 12))
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
			val results = (net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerConfigs innerJoin net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.DonationConfigs)
				.selectAll().where {
					net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.DonationConfigs.customBadge eq true and (net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerConfigs.id inList mutualGuilds)
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

	/**
	 * Gets an user's profile background
	 *
	 * @param id the user's ID
	 * @return the background image
	 */
	suspend fun getUserProfileBackground(id: Long, profileDesignInternalNameOverride: String? = null) = getUserProfileBackground(loritta.getOrCreateLorittaProfile(id), profileDesignInternalNameOverride)

	/**
	 * Gets an user's profile background image or, if the user has a custom background, loads the custom background.
	 *
	 * To avoid exceeding the available memory, profiles are loaded from the "cropped_profiles" folder,
	 * which has all the images in 800x600 format.
	 *
	 * @param background the user's background
	 * @return the background image
	 */
	suspend fun getUserProfileBackground(profile: PuddingUserProfile, profileDesignInternalNameOverride: String? = null): BufferedImage {
		val backgroundUrl = getUserProfileBackgroundUrl(profile, profileDesignInternalNameOverride)
		val response = loritta.http.get(backgroundUrl) {
			userAgent(loritta.lorittaCluster.getUserAgent(this@ProfileDesignManager.loritta))
		}

		val bytes = response.readBytes()

		return net.perfectdreams.loritta.cinnamon.discord.utils.images.readImage(bytes.inputStream())
	}

	/**
	 * Gets an user's profile background image or, if the user has a custom background, loads the custom background.
	 *
	 * To avoid exceeding the available memory, profiles are loaded from the "cropped_profiles" folder,
	 * which has all the images in 800x600 format.
	 *
	 * @param background the user's background
	 * @return the background image
	 */
	suspend fun getUserProfileBackground(profile: Profile, profileDesignInternalNameOverride: String? = null): BufferedImage {
		val backgroundUrl = getUserProfileBackgroundUrl(profile, profileDesignInternalNameOverride)
		val response = loritta.http.get(backgroundUrl) {
			userAgent(loritta.lorittaCluster.getUserAgent(this@ProfileDesignManager.loritta))
		}

		val bytes = response.readBytes()

		return readImage(bytes.inputStream())
	}

	/**
	 * Gets an user's profile background image or, if the user has a custom background, loads the custom background.
	 *
	 * To avoid exceeding the available memory, profiles are loaded from the "cropped_profiles" folder,
	 * which has all the images in 800x600 format.
	 *
	 * @param background the user's background
	 * @param profileCreator the profile creator being used, overrides the [ProfileSettings.activeProfileDesign] option
	 * @return the background image
	 */
	suspend fun getUserProfileBackground(profile: Profile, profileCreator: ProfileCreator, profileDesignInternalNameOverride: String? = null): BufferedImage {
		val backgroundUrl = getUserProfileBackgroundUrl(profile, profileCreator, profileDesignInternalNameOverride)
		val response = loritta.http.get(backgroundUrl) {
			userAgent(loritta.lorittaCluster.getUserAgent(this@ProfileDesignManager.loritta))
		}

		val bytes = response.readBytes()

		return readImage(bytes.inputStream())
	}

	/**
	 * Gets an user's profile background URL
	 *
	 * @param userId the user's ID
	 * @return the background image
	 */
	suspend fun getUserProfileBackgroundUrl(userId: Long) = getUserProfileBackgroundUrl(loritta.getOrCreateLorittaProfile(userId))

	/**
	 * Gets an user's profile background URL
	 *
	 * This does *not* crop the profile background
	 *
	 * @param profile the user's profile
	 * @return the background image
	 */
	suspend fun getUserProfileBackgroundUrl(profile: Profile, profileDesignInternalNameOverride: String? = null): String {
		// This is bad
		val (settingsId, activeProfileDesignInternalName, activeBackgroundInternalName) = loritta.newSuspendedTransaction {
			val settingsId = profile.settings.id.value
			val activeProfileDesignInternalName = profile.settings.activeProfileDesignInternalName?.value
			val activeBackgroundInternalName = profile.settings.activeBackgroundInternalName?.value

			Triple(settingsId, activeProfileDesignInternalName, activeBackgroundInternalName)
		}

		return getUserProfileBackgroundUrl(profile.userId, settingsId, profileDesignInternalNameOverride ?: activeProfileDesignInternalName ?: ProfileDesign.DEFAULT_PROFILE_DESIGN_ID, activeBackgroundInternalName ?: Background.DEFAULT_BACKGROUND_ID)
	}

	/**
	 * Gets an user's profile background URL
	 *
	 * This does *not* crop the profile background
	 *
	 * @param profile the user's profile
	 * @param profileCreator the profile creator being used, overrides the [ProfileSettings.activeProfileDesign] option
	 * @return the background image
	 */
	suspend fun getUserProfileBackgroundUrl(profile: Profile, profileCreator: ProfileCreator, profileDesignInternalNameOverride: String? = null): String {
		// This is bad
		val (settingsId, _, activeBackgroundInternalName) = loritta.newSuspendedTransaction {
			val settingsId = profile.settings.id.value
			val activeProfileDesignInternalName = profile.settings.activeProfileDesignInternalName?.value
			val activeBackgroundInternalName = profile.settings.activeBackgroundInternalName?.value

			Triple(settingsId, activeProfileDesignInternalName, activeBackgroundInternalName)
		}

		return getUserProfileBackgroundUrl(profile.userId, settingsId, profileCreator.internalName, profileDesignInternalNameOverride ?: activeBackgroundInternalName ?: Background.DEFAULT_BACKGROUND_ID)
	}

	/**
	 * Gets an user's profile background URL
	 *
	 * This does *not* crop the profile background
	 *
	 * @param profile the user's profile
	 * @return the background image
	 */
	suspend fun getUserProfileBackgroundUrl(profile: PuddingUserProfile, profileDesignInternalNameOverride: String? = null): String {
		val profileSettings = profile.getProfileSettings()
		val activeProfileDesignInternalName = profileSettings.activeProfileDesign
		val activeBackgroundInternalName = profileSettings.activeBackground
		return getUserProfileBackgroundUrl(profile.id.value.toLong(), profileSettings.id, profileDesignInternalNameOverride ?: activeProfileDesignInternalName ?: ProfileDesign.DEFAULT_PROFILE_DESIGN_ID, activeBackgroundInternalName ?: Background.DEFAULT_BACKGROUND_ID)
	}

	/**
	 * Gets an user's profile background URL
	 *
	 * This does *not* crop the profile background
	 *
	 * @param profile the user's profile
	 * @return the background image
	 */
	suspend fun getUserProfileBackgroundUrl(
		userId: Long,
		settingsId: Long,
		activeProfileDesignInternalName: String,
		activeBackgroundInternalName: String
	): String {
		val defaultBlueBackground = loritta.pudding.backgrounds.getBackground(Background.DEFAULT_BACKGROUND_ID)!!
		var background = loritta.pudding.backgrounds.getBackground(activeBackgroundInternalName) ?: defaultBlueBackground

		if (background.id == Background.RANDOM_BACKGROUND_ID) {
			// If the user selected a random background, we are going to get all the user's backgrounds and choose a random background from the list
			val allBackgrounds = mutableListOf(defaultBlueBackground)

			allBackgrounds.addAll(
				loritta.newSuspendedTransaction {
					(BackgroundPayments innerJoin Backgrounds).selectAll().where {
						BackgroundPayments.userId eq userId
					}.map {
						val data = Background.fromRow(it)
						PuddingBackground(
							loritta.pudding,
							data
						)
					}
				}
			)

			background = allBackgrounds.random()
		}

		if (background.id == Background.CUSTOM_BACKGROUND_ID) {
			// Custom background
			val donationValue = loritta.getActiveMoneyFromDonations(userId)
			val plan = UserPremiumPlans.getPlanFromValue(donationValue)

			if (plan.customBackground) {
				val dssNamespace = loritta.dreamStorageService.getCachedNamespaceOrRetrieve()
				val resultRow = loritta.newSuspendedTransaction {
					CustomBackgroundSettings.selectAll().where { CustomBackgroundSettings.settings eq settingsId }
						.firstOrNull()
				}

				// If the path exists, then the background (probably!) exists
				if (resultRow != null) {
					val file = resultRow[net.perfectdreams.loritta.cinnamon.pudding.tables.CustomBackgroundSettings.file]
					val extension = MediaTypeUtils.convertContentTypeToExtension(resultRow[net.perfectdreams.loritta.cinnamon.pudding.tables.CustomBackgroundSettings.preferredMediaType])
					return "${loritta.config.loritta.dreamStorageService.url}/$dssNamespace/${StoragePaths.CustomBackground(userId, file).join()}.$extension"
				}
			}

			// If everything fails, change the background to the default blue background
			// This is required because the current background is "CUSTOM", so Loritta will try getting the default variation of the custom background...
			// but that doesn't exist!
			background = defaultBlueBackground
		}

		val dssNamespace = loritta.dreamStorageService.getCachedNamespaceOrRetrieve()
		val variation = background.getVariationForProfileDesign(activeProfileDesignInternalName)
		return when (variation.storageType) {
			BackgroundStorageType.DREAM_STORAGE_SERVICE -> getDreamStorageServiceBackgroundUrlWithCropParameters(loritta.config.loritta.dreamStorageService.url, dssNamespace, variation)
			BackgroundStorageType.ETHEREAL_GAMBI -> getEtherealGambiBackgroundUrl(variation)
		}
	}

	fun getDreamStorageServiceBackgroundUrl(
		dreamStorageServiceUrl: String,
		namespace: String,
		background: BackgroundVariation
	): String {
		val extension = MediaTypeUtils.convertContentTypeToExtension(background.preferredMediaType)
		return "$dreamStorageServiceUrl/$namespace/${StoragePaths.Background(background.file).join()}.$extension"
	}

	fun getDreamStorageServiceBackgroundUrlWithCropParameters(
		dreamStorageServiceUrl: String,
		namespace: String,
		variation: BackgroundVariation
	): String {
		var url = getDreamStorageServiceBackgroundUrl(dreamStorageServiceUrl, namespace, variation)
		val crop = variation.crop
		if (crop != null)
			url += "?crop_x=${crop.x}&crop_y=${crop.y}&crop_width=${crop.width}&crop_height=${crop.height}"
		return url
	}

	fun getEtherealGambiBackgroundUrl(background: BackgroundVariation): String {
		val extension = MediaTypeUtils.convertContentTypeToExtension(background.preferredMediaType)
		return loritta.config.loritta.etherealGambiService.url.removeSuffix("/") + "/" + background.file + ".$extension"
	}

	class ProfileCreationResult(
		val image: ByteArray,
		val userProfile: Profile,
		val profileSettings: ProfileSettings,
		val allowedDiscordEmojis: List<Long>?,
		val aboutMe: String,
		val modifiedAboutMe: String,
		val imageFormat: ImageFormat
	)
}