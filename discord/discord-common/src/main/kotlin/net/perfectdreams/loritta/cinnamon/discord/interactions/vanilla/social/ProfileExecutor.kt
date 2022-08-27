package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social

import dev.kord.core.entity.User
import dev.kord.rest.Image
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.utils.*
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageFormatType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils.toByteArray
import net.perfectdreams.loritta.cinnamon.discord.utils.images.readImage
import net.perfectdreams.loritta.cinnamon.discord.utils.images.readImageFromResources
import net.perfectdreams.loritta.cinnamon.discord.utils.profiles.NostalgiaProfileCreator
import net.perfectdreams.loritta.cinnamon.discord.utils.profiles.ProfileUserInfoData
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingUserProfile
import net.perfectdreams.loritta.cinnamon.pudding.tables.BotVotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.DonationConfigs
import net.perfectdreams.loritta.cinnamon.utils.TodoFixThisData
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.time.Duration.Companion.hours

class ProfileExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val user = optionalUser("user", TodoFixThisData)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        // TODO: Check if the user is banned
        val userToBeViewed = args[options.user] ?: context.user

        context.deferChannelMessage()

        val profileCreator = NostalgiaProfileCreator.NostalgiaDarkProfileCreator(loritta)
        val userProfile = loritta.services.users.getOrCreateUserProfile(UserId(userToBeViewed.id))
        val profileSettings = userProfile.getProfileSettings()

        // We need the mutual guilds to retrieve the user's guild badges.
        // However, because bots can be in a LOT of guilds (causing GC pressure), so we will just return a empty array.
        // Bots could also cause a lot of badges to be downloaded, because they are in a lot of guilds.
        //
        // After all, does it *really* matter that bots won't have any badges? ¯\_(ツ)_/¯
        val mutualGuildsInAllClusters = if (userToBeViewed.isBot)
            setOf()
        else
            loritta.services.transaction {
                GuildProfiles.slice(GuildProfiles.guildId)
                    .select { GuildProfiles.userId eq userToBeViewed.id.toLong() and (GuildProfiles.isInGuild eq true) }
                    .map { it[GuildProfiles.guildId] }
                    .toSet()
            }

        val badges = getUserBadges(
            userToBeViewed,
            userProfile,
            mutualGuildsInAllClusters
        )

        val image = profileCreator.create(
            ProfileUserInfoData(
                context.user.id,
                context.user.username,
                context.user.discriminator,
                context.user.effectiveAvatar
                    .cdnUrl
                    .toUrl {
                        this.format = Image.Format.PNG
                    }
            ),
            ProfileUserInfoData(
                userToBeViewed.id,
                userToBeViewed.username,
                userToBeViewed.discriminator,
                userToBeViewed.effectiveAvatar
                    .cdnUrl
                    .toUrl {
                        this.format = Image.Format.PNG
                    }
            ),
            userProfile,
            if (context is GuildApplicationCommandContext) loritta.kord.getGuild(context.guildId) else null,
            badges,
            context.i18nContext,
            loritta.getUserProfileBackground(userProfile),
            profileSettings.aboutMe ?: "" // TODO: Fix this by providing a proper default description
        )

        context.sendMessage {
            content = "O comando ainda não está pronto! Use `+perfil` para ver o seu perfil com todos os frufrus dele!"

            addFile("profile.png", image.toByteArray(ImageFormatType.PNG).inputStream())
        }
    }

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
        user: User,
        profile: PuddingUserProfile,
        mutualGuilds: Set<Long>,
        failIfClusterIsOffline: Boolean = false
    ): List<BufferedImage> {
        val hasUpvoted = loritta.services.transaction {
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

        val money = loritta.services.payments.getActiveMoneyFromDonations(UserId(user.id))

        if (money != 0.0) {
            badges += readImageFromResources("/badges/donator.png")

            if (money >= 99.99) {
                badges += readImageFromResources("/badges/super_donator.png")
            }
        }

        // TODO: Fix this
        // if (isLorittaPartner) badges += ImageIO.read(File(Loritta.ASSETS + "lori_hype.png"))
        // if (isTranslator) badges += ImageIO.read(File(Loritta.ASSETS + "translator.png"))
        // if (isGitHubContributor) badges += ImageIO.read(File(Loritta.ASSETS + "github_contributor.png"))

        // TODO: Fix this
        // if (user.idLong == 249508932861558785L || user.idLong == 336892460280315905L)
        //   badges += ImageIO.read(File(Loritta.ASSETS + "loritta_sweater.png"))

        val dssNamespace = loritta.dreamStorageService.getCachedNamespaceOrRetrieve()

        loritta.services.transaction {
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
    } */
}
