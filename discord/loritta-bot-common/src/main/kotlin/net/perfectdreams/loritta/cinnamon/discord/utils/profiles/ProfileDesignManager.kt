package net.perfectdreams.loritta.cinnamon.discord.utils.profiles

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import dev.kord.core.entity.User
import dev.kord.rest.Image
import kotlinx.coroutines.flow.toList
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.utils.*
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageFormatType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils.toByteArray
import net.perfectdreams.loritta.cinnamon.discord.utils.images.readImageFromResources
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingProfileSettings
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingUserProfile
import net.perfectdreams.loritta.cinnamon.pudding.tables.BotVotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.DonationConfigs
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.awt.image.BufferedImage
import kotlin.time.Duration.Companion.hours

class ProfileDesignManager(val loritta: LorittaCinnamon) {
    companion object {
        private val FREE_EMOJIS_GUILDS = listOf(
            Snowflake(297732013006389252), // Apartamento da Loritta
            Snowflake(320248230917046282), // SparklyPower
            Snowflake(417061847489839106), // Rede Dark
            Snowflake(769892417025212497), // Kuraminha's House
            Snowflake(769030809159073795)  // Saddest of the Sads
        )
    }

    val designs = mutableListOf<ProfileCreator>()
    val defaultProfileDesign: StaticProfileCreator
        get() = designs.first { it.internalName == "defaultDark" } as StaticProfileCreator

    // val badges = mutableListOf<Badge>()

    // TODO: Fix this
    /* fun registerBadge(badge: Badge) {
        badges.add(badge)
    }

    fun unregisterBadge(badge: Badge) {
        badges.remove(badge)
    } */

    fun registerDesign(design: StaticProfileCreator) {
        designs.removeIf { it.internalName == design.internalName }
        designs.add(design)
    }

    fun unregisterDesign(design: StaticProfileCreator) {
        designs.removeIf { it.internalName == design.internalName }
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

        // ===[ DISCORD USER FLAGS BADGES ]===
        // TODO: Fix this
        /* registerBadge(DiscordUserFlagBadge.DiscordStaffBadge())
        registerBadge(DiscordUserFlagBadge.DiscordPartnerBadge())
        registerBadge(DiscordUserFlagBadge.DiscordVerifiedDeveloperBadge())
        registerBadge(DiscordUserFlagBadge.DiscordHypesquadEventsBadge())
        registerBadge(DiscordUserFlagBadge.DiscordEarlySupporterBadge())
        registerBadge(DiscordUserFlagBadge.DiscordBraveryHouseBadge())
        registerBadge(DiscordUserFlagBadge.DiscordBrillanceHouseBadge())
        registerBadge(DiscordUserFlagBadge.DiscordBalanceHouseBadge())

        registerBadge(DiscordNitroBadge())

        registerBadge(ArtistBadge()) */
    }
    suspend fun createProfile(
        loritta: LorittaCinnamon,
        i18nContext: I18nContext,
        sender: User,
        userToBeViewed: User,
        guild: Guild?
    ): ProfileCreationResult {
        val userProfile = loritta.pudding.users.getOrCreateUserProfile(UserId(userToBeViewed.id))
        val profileSettings = userProfile.getProfileSettings()
        val profileCreator = loritta.profileDesignManager.designs.firstOrNull { it.internalName == profileSettings.activeProfileDesign }
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

        val badges = getUserBadges(
            loritta,
            userToBeViewed,
            userProfile,
            mutualGuildsInAllClusters
        )

        val premiumPlan = UserPremiumPlans.getPlanFromValue(loritta.pudding.payments.getActiveMoneyFromDonations(userProfile.id))

        val allowedDiscordEmojis = if (premiumPlan.customEmojisInAboutMe)
            null // Null = All emojis are allowed
        else {
            // If the user does not have the custom emojis in about me feature, let's allow them to use specific guild's emojis
            loritta.redisConnection {
                FREE_EMOJIS_GUILDS.flatMap { snowflake ->
                    it.hgetAll(loritta.redisKeys.discordGuildEmojis(snowflake))
                        .keys
                        .map { Snowflake(it) }
                }
            }
        }

        val aboutMe = profileSettings.aboutMe ?: i18nContext.get(I18nKeysData.Profiles.DefaultAboutMe)

        val modifiedAboutMe = aboutMe
            // Discord Relative Time Formatting
            .replace(Regex("<t:(\\d+):R>")) {
                val epochSecond = it.groupValues[1].toLong()
                DateUtils.formatDiscordLikeRelativeDate(i18nContext, epochSecond * 1_000, System.currentTimeMillis())
            }

        val imageAsByteArray = when (profileCreator) {
            is StaticProfileCreator -> {
                profileCreator.create(
                    transformUserToProfileUserInfoData(sender),
                    transformUserToProfileUserInfoData(userToBeViewed),
                    userProfile,
                    guild,
                    badges,
                    i18nContext,
                    loritta.getUserProfileBackground(userProfile),
                    modifiedAboutMe,
                    allowedDiscordEmojis
                ).toByteArray(ImageFormatType.PNG)
            }
            else -> error("Unsupported Profile Creator Type $profileCreator")
        }

        return ProfileCreationResult(
            imageAsByteArray,
            userProfile,
            profileSettings,
            allowedDiscordEmojis,
            aboutMe,
            modifiedAboutMe
        )
    }

    private fun transformUserToProfileUserInfoData(user: User) = ProfileUserInfoData(
        user.id,
        user.username,
        user.discriminator,
        user.effectiveAvatar
            .cdnUrl
            .toUrl {
                this.format = Image.Format.PNG
            }
    )

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
        loritta: LorittaCinnamon,
        user: User,
        profile: PuddingUserProfile,
        mutualGuilds: Set<Long>,
        failIfClusterIsOffline: Boolean = false
    ): List<BufferedImage> {
        val hasUpvoted = loritta.pudding.transaction {
            BotVotes.select {
                BotVotes.userId eq user.id.toLong() and (BotVotes.votedAt greaterEq System.currentTimeMillis() - (12.hours.inWholeMilliseconds))
            }.count() != 0L
        }

        // TODO: Fix this
        // val hasNotifyMeRoleJob = GlobalScope.async(loritta.coroutineDispatcher) { hasRole(Constants.PORTUGUESE_SUPPORT_GUILD_ID, "334734175531696128") }
        // val isLorittaPartnerJob = GlobalScope.async(loritta.coroutineDispatcher) { hasRole(Constants.PORTUGUESE_SUPPORT_GUILD_ID, "434512654292221952") }
        // val isTranslatorJob = GlobalScope.async(loritta.coroutineDispatcher) { hasRole(Constants.PORTUGUESE_SUPPORT_GUILD_ID, "385579854336360449") }
        // val isGitHubContributorJob = GlobalScope.async(loritta.coroutineDispatcher) { hasRole(Constants.PORTUGUESE_SUPPORT_GUILD_ID, "505144985591480333") }
        // val isPocketDreamsStaffJob = GlobalScope.async(loritta.coroutineDispatcher) { hasRole(Constants.SPARKLYPOWER_GUILD_ID, "332650495522897920") }
        // val hasLoriStickerArt = loritta.fanArtArtists.any { it.id == user.id }
        // val isLoriBodyguardJob = GlobalScope.async(loritta.coroutineDispatcher) { hasRole(Constants.PORTUGUESE_SUPPORT_GUILD_ID, "351473717194522647") }
        // val isLoriSupportJob = GlobalScope.async(loritta.coroutineDispatcher) { hasRole(Constants.PORTUGUESE_SUPPORT_GUILD_ID, "399301696892829706") }

        // val hasNotifyMeRole = hasNotifyMeRoleJob.await()
        // val isLorittaPartner = isLorittaPartnerJob.await()
        // val isTranslator = isTranslatorJob.await()
        // val isGitHubContributor = isGitHubContributorJob.await()
        // val isPocketDreamsStaff = isPocketDreamsStaffJob.await()
        // val isLoriBodyguard = isLoriBodyguardJob.await()
        // val isLoriSupport = isLoriSupportJob.await()

        val badges = mutableListOf<BufferedImage>()

        // TODO: Fix this
        /* badges.addAll(
            loritta.profileDesignManager.badges.filter { it.checkIfUserDeservesBadge(user, profile, mutualGuilds) }
                .sortedByDescending { it.priority }
                .map {
                    readImage(File(Loritta.ASSETS, it.badgeFileName))
                }
        ) */

        // TODO: Fix this
        // if (isLoriBodyguard) badges += ImageIO.read(File(Loritta.ASSETS + "supervisor.png"))
        // if (isPocketDreamsStaff) badges += ImageIO.read(File(Loritta.ASSETS + "pocketdreams_staff.png"))
        // if (isLoriSupport) badges += ImageIO.read(File(Loritta.ASSETS + "support.png"))
        // if (hasLoriStickerArt) badges += ImageIO.read(File(Loritta.ASSETS + "sticker_badge.png"))

        val money = loritta.pudding.payments.getActiveMoneyFromDonations(UserId(user.id))

        if (money != 0.0) {
            if (money >= 99.99) {
                badges += readImageFromResources("/badges/super_donator.png")
            }

            badges += readImageFromResources("/badges/donator.png")
        }

        // TODO: Fix this
        // if (isLorittaPartner) badges += ImageIO.read(File(Loritta.ASSETS + "lori_hype.png"))
        // if (isTranslator) badges += ImageIO.read(File(Loritta.ASSETS + "translator.png"))
        // if (isGitHubContributor) badges += ImageIO.read(File(Loritta.ASSETS + "github_contributor.png"))

        // TODO: Fix this
        // if (user.idLong == 249508932861558785L || user.idLong == 336892460280315905L)
        //   badges += ImageIO.read(File(Loritta.ASSETS + "loritta_sweater.png"))

        val dssNamespace = loritta.dreamStorageService.getCachedNamespaceOrRetrieve()

        loritta.pudding.transaction {
            val results = (ServerConfigs innerJoin DonationConfigs)
                .select {
                    DonationConfigs.customBadge eq true and (ServerConfigs.id inList mutualGuilds)
                }

            for (result in results) {
                val guildId = result[ServerConfigs.id].value
                val customBadge = result[DonationConfigs.customBadge]
                val badgeFile = result[DonationConfigs.customBadgeFile]
                val badgeMediaType = result[DonationConfigs.customBadgePreferredMediaType]

                // TODO: Only enable it if the server still has an active DonationConfigs custom plan
                if (customBadge && badgeFile != null && badgeMediaType != null /* && ServerPremiumPlans.getPlanFromValue(donationKeysValue).hasCustomBadge */) {
                    val extension = MediaTypeUtils.convertContentTypeToExtension(badgeMediaType)
                    val badge = ImageUtils.downloadImage("${loritta.config.services.dreamStorageService.url}/$dssNamespace/${StoragePaths.CustomBadge(guildId, badgeFile).join()}.$extension", bypassSafety = true)

                    if (badge != null) {
                        badges += badge
                    }
                }
            }
        }

        // TODO: Fix this
        // if (hasNotifyMeRole) badges += ImageIO.read(File(Loritta.ASSETS + "notify_me.png"))
        // if (user.id == loritta.discordConfig.discord.clientId) badges += ImageIO.read(File(Loritta.ASSETS + "loritta_badge.png"))
        if (user.isBot) badges += readImageFromResources("/badges/bot.png")
        // TODO: Fix this
        // val marriage = loritta.newSuspendedTransaction { profile.marriage }
        // if (marriage != null) {
        //     if (System.currentTimeMillis() - marriage.marriedSince > 2_592_000_000) {
        //         badges += ImageIO.read(File(Loritta.ASSETS + "blob_snuggle.png"))
        //     }
        //     badges += ImageIO.read(File(Loritta.ASSETS + "ring.png"))
        // }
        // if (hasUpvoted) badges += ImageIO.read(File(Loritta.ASSETS + "upvoted_badge.png"))

        return badges
    }

    // TODO: Fix this
    /*
    /**
     * Checks if the user has the role in the specified guild
     *
     * @param guildId the guild ID
     * @param roleId  the role ID
     * @return if the user has the role
     */ suspend fun hasRole(guildId: String, roleId: String): Boolean {
        val cluster = DiscordUtils.getLorittaClusterForGuildId(guildId.toLong())

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
    } */

    class ProfileCreationResult(
        val image: ByteArray,
        val userProfile: PuddingUserProfile,
        val profileSettings: PuddingProfileSettings,
        val allowedDiscordEmojis: List<Snowflake>?,
        val aboutMe: String,
        val modifiedAboutMe: String
    )
}