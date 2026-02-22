package net.perfectdreams.loritta.loricoolcards.generator

import dev.minn.jda.ktx.coroutines.await
import io.ktor.client.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.UserSnowflake
import net.perfectdreams.dreamstorageservice.client.DreamStorageServiceClient
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.discord.utils.images.readImageFromResources
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingBackground
import net.perfectdreams.loritta.cinnamon.pudding.services.UsersService
import net.perfectdreams.loritta.cinnamon.pudding.services.fromRow
import net.perfectdreams.loritta.cinnamon.pudding.tables.*
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsEvents
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsFinishedAlbumUsers
import net.perfectdreams.loritta.cinnamon.pudding.utils.PaymentReason
import net.perfectdreams.loritta.common.locale.LorittaLanguageManager
import net.perfectdreams.loritta.common.loricoolcards.CardRarity
import net.perfectdreams.loritta.common.utils.MediaTypeUtils
import net.perfectdreams.loritta.common.utils.StoragePaths
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.loricoolcards.generator.utils.config.LoriCoolCardsGeneratorProductionStickersConfig
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Payment
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.loricoolcards.LoriCoolCardsManager
import net.perfectdreams.loritta.morenitta.loricoolcards.StickerMetadata
import net.perfectdreams.loritta.morenitta.profile.ProfileDesignManager
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import net.perfectdreams.loritta.morenitta.profile.badges.*
import net.perfectdreams.loritta.morenitta.utils.GraphicsFonts
import net.perfectdreams.loritta.morenitta.utils.ImageFormat
import net.perfectdreams.loritta.morenitta.utils.extensions.getEffectiveAvatarUrl
import net.perfectdreams.loritta.morenitta.utils.readConfigurationFromFile
import net.perfectdreams.loritta.serializable.Background
import net.perfectdreams.loritta.serializable.BackgroundStorageType
import net.perfectdreams.loritta.serializable.BackgroundVariation
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.selectAll
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import kotlin.math.ceil
import java.util.*
import java.util.concurrent.Executors
import javax.imageio.ImageIO

suspend fun main() {
    // Speeds up image loading/writing/etc
    // https://stackoverflow.com/a/44170254/7271796
    ImageIO.setUseCache(false)

    val configurationFile = File(System.getProperty("conf") ?: "./loricoolcards-production-stickers-generator.conf")

    if (!configurationFile.exists()) {
        println("Missing configuration file!")
        System.exit(1)
        return
    }

    val config = readConfigurationFromFile<LoriCoolCardsGeneratorProductionStickersConfig>(configurationFile)

    generateCards(config)
}

suspend fun generateCards(config: LoriCoolCardsGeneratorProductionStickersConfig) {
    val folderName = "production_v17_befopti"
    val http = HttpClient {}

    println("Max memory: ${Runtime.getRuntime().maxMemory()}")
    val fonts = GraphicsFonts()
    val i18nContext = LorittaLanguageManager(LorittaBot::class).defaultI18nContext
    val dreamStorageService = DreamStorageServiceClient(
        config.dreamStorageService.url,
        config.dreamStorageService.token,
        http
    )

    val loriCoolCardsManager = LoriCoolCardsManager(fonts)

    val jda = JDABuilder.createLight(config.botToken)
        .build()
        .awaitReady()

    val sqlCommandsFile = File("/mnt/HDDThings/Pictures/Loritta/LoriCoolCards/$folderName/sql_commands.sql")
    if (sqlCommandsFile.exists()) {
        println("SQL commands file already exists!")
        readLine()
        sqlCommandsFile.delete()
    }

    val cardGensData = mutableListOf<LoriCoolCardsManager.CardGenData>()

    val pudding = Pudding.createPostgreSQLPudding(
        LorittaBot.SCHEMA_VERSION,
        config.pudding.address,
        config.pudding.database,
        config.pudding.username,
        config.pudding.password
    )

    val badges = listOf(
        DiscordUserFlagBadge.DiscordPartnerBadge(),
        DiscordUserFlagBadge.DiscordVerifiedDeveloperBadge(),
        DiscordUserFlagBadge.DiscordHypesquadEventsBadge(),
        DiscordUserFlagBadge.DiscordEarlySupporterBadge(),
        DiscordUserFlagBadge.DiscordBraveryHouseBadge(),
        DiscordUserFlagBadge.DiscordBrillianceHouseBadge(),
        DiscordUserFlagBadge.DiscordBalanceHouseBadge(),
        DiscordUserFlagBadge.DiscordActiveDeveloperBadge(),
        DiscordUserFlagBadge.DiscordModeratorProgramAlumniBadge(),
        DiscordUserFlagBadge.DiscordStaffBadge(),

        DiscordNitroBadge(pudding),

        // ArtistBadge(loritta),

        // MerchBuyerBadge(loritta),
        HalloweenBadge(pudding),
        Christmas2019Badge(pudding),
        Christmas2022Badge(pudding),
        Easter2023Badge(pudding),
        GabrielaBadge(pudding),
        PantufaBadge(pudding),
        // PremiumBadge(loritta),
        // SuperPremiumBadge(loritta),
        MarriedBadge(pudding),
        GrassCutterBadge(pudding),
        // SparklyMemberBadge(loritta),
        // LorittaStaffBadge(loritta),
        // SparklyStaffBadge(loritta),
        StonksBadge(pudding),
        StickerFanBadge(pudding),
        ReactionEventBadge.Halloween2024ReactionEventBadge(pudding),
        ReactionEventBadge.Halloween2024ReactionEventSuperBadge(pudding),
        BratBadge(pudding),
        ReactionEventBadge.Christmas2024ReactionEventBadge(pudding),
        ReactionEventBadge.Christmas2024ReactionEventSuperBadge(pudding),
        ReactionEventBadge.Anniversary2025ReactionEventBadge(pudding),
        ReactionEventBadge.Anniversary2025ReactionEventSuperBadge(pudding),
        TopLoveLetterBadge(pudding),
        ReactionEventBadge.Halloween2025ReactionEventBadge(pudding),
        ReactionEventBadge.Halloween2025ReactionEventSuperBadge(pudding),
        ReactionEventBadge.Christmas2025ReactionEventBadge(pudding),
        ReactionEventBadge.Christmas2025ReactionEventSuperBadge(pudding)
    )

    // Badges that requires a "Loritta" instance, so, to avoid changing the badges too much, we just pretend that the badge is valid and carry on with our lives
    val hardcodedBadges = listOf(
        HardcodedBadge(
            UUID.fromString("81788d4a-7e6c-415f-8832-d55573f8c40b"),
            ProfileDesignManager.I18N_BADGES_PREFIX.Artist.Title,
            "artist.png",
        ),
        HardcodedBadge(
            UUID.fromString("54839157-767a-425f-8cf2-5ebfab4f9c13"),
            ProfileDesignManager.I18N_BADGES_PREFIX.SparklyStaff.Title,
            "sparkly_staff.png",
        ),
        HardcodedBadge(
            UUID.fromString("2ef616dd-dd0e-4a17-98e1-10a6bfa7d6a6"),
            ProfileDesignManager.I18N_BADGES_PREFIX.Premium.Title,
            "donator.png",
        ),
        HardcodedBadge(
            UUID.fromString("fa286d07-6e55-473a-9aec-84f6c6337a02"),
            ProfileDesignManager.I18N_BADGES_PREFIX.SuperPremium.Title,
            "super_donator.png",
        ),
        HardcodedBadge(
            UUID.fromString("00b2b958-55bd-4daa-8822-eafd29b933ca"),
            ProfileDesignManager.I18N_BADGES_PREFIX.MerchBuyer.Title,
            "lori_caneca.png",
        ),
        HardcodedBadge(
            UUID.fromString("7facf3a6-9f29-4841-9eb0-b27312a0ceb1"),
            ProfileDesignManager.I18N_BADGES_PREFIX.SparklyMember.Title,
            "sparkly_member.png",
        ),
        HardcodedBadge(
            UUID.fromString("8f707b11-55cb-4b8c-aae3-62e382e268fc"),
            ProfileDesignManager.I18N_BADGES_PREFIX.LorittaStaff.Title,
            "loritta_staff.png",
        ),
        HardcodedBadge(
            UUID.fromString("54839157-767a-425f-8cf2-5ebfab4f9c13"),
            ProfileDesignManager.I18N_BADGES_PREFIX.SparklyStaff.Title,
            "sparkly_staff.png",
        )
    )

    var cardId = 1

    // Avatars that will be falled back to user avatar
    val blacklistedUserAvatarsIds = mapOf<Long, String>(
        // 492701937347723265L to "https://cdn.discordapp.com/attachments/739823666891849729/1302006414738194563/avatar_nsfw.png?ex=67268b77&is=672539f7&hm=ddf2388347944804ac19749b76787fd9c16a055a67bba9393b0d724b1b46a421&"
        // 755138747938504871L to "https://cdn.discordapp.com/attachments/1082340413156892682/1257402203015221360/IMG_5412.jpg?ex=6684468e&is=6682f50e&hm=c6efc25a8f97c0b0df7573c50b29b06f3685d292b3782936425a4b865dd82b31&"
    )

    val staffIds = listOf(
        297153970613387264L,
        123170274651668480L,
        197308318119755776L,
        400683515873591296L,
        351760430991147010L,
        437731723350900739L,
        472085605623529496L,
        670071034074103854L,
        716468730799980587L,
        197501878399926272L
    )

    // TODO: Sort staff profiles by specific order
    val staffProfilesTemporary = pudding.transaction {
        Profiles.innerJoin(UserSettings)
            .selectAll()
            .where {
                Profiles.id inList staffIds
            }
            .toList()
    }

    // Sort staff profiles by specific order
    val staffProfiles = listOf(
        staffProfilesTemporary.first { it[Profiles.id].value == 297153970613387264L }, // Loritta

        staffProfilesTemporary.first { it[Profiles.id].value == 123170274651668480L }, // Power
        staffProfilesTemporary.first { it[Profiles.id].value == 197308318119755776L }, // JvGm45
        staffProfilesTemporary.first { it[Profiles.id].value == 400683515873591296L }, // Stéphany

        staffProfilesTemporary.first { it[Profiles.id].value == 351760430991147010L }, // Arth
        staffProfilesTemporary.first { it[Profiles.id].value == 437731723350900739L }, // nathaan
        staffProfilesTemporary.first { it[Profiles.id].value == 472085605623529496L }, // José

        staffProfilesTemporary.first { it[Profiles.id].value == 670071034074103854L }, // Hech
        staffProfilesTemporary.first { it[Profiles.id].value == 716468730799980587L }, // Furalha
        staffProfilesTemporary.first { it[Profiles.id].value == 197501878399926272L }, // Paum
    )

    val moneyProfiles = pudding.transaction {
        val lastEvent = LoriCoolCardsEvents.selectAll()
            .orderBy(LoriCoolCardsEvents.startsAt, SortOrder.DESC)
            .limit(1)
            .first()

        LoriCoolCardsFinishedAlbumUsers
            .innerJoin(Profiles, { LoriCoolCardsFinishedAlbumUsers.user }, { Profiles.id })
            .innerJoin(UserSettings)
            .selectAll()
            .where {
                LoriCoolCardsFinishedAlbumUsers.event eq lastEvent[LoriCoolCardsEvents.id].value and (Profiles.id notInSubQuery UsersService.validBannedUsersList(System.currentTimeMillis()) and (Profiles.id notInList staffIds) and (Profiles.id notInSubQuery UsersService.botTokenUsersList()))
            }
            .orderBy(LoriCoolCardsFinishedAlbumUsers.finishedAt, SortOrder.ASC)
            .limit(500)
            .toList()
    }

    val preCachedUsers = mutableMapOf<Long, User>()

    println("Pre caching users via retrieve members...")
    val lorittaGuild = jda.getGuildById(config.guildIdToPreCacheUsersFrom)!!
    val chunkedUserIds = (staffProfiles.map { it[Profiles.id].value } + moneyProfiles.map { it[Profiles.id].value }).chunked(100)
    for (userIds in chunkedUserIds) {
        val members = lorittaGuild.retrieveMembers(userIds.map { UserSnowflake.fromId(it) })
        members.await().forEach {
            preCachedUsers[it.idLong] = it.user
        }
    }
    println("Precached users: ${preCachedUsers.size}")
    val jobs = mutableListOf<Job>()

    val fileMutex = Mutex()
    // Dispatch all user retrieve requests at once, don't block the other requests
    // Just let it rip, sadly there isn't a nice workaround to avoid the generations

    // We don't use a semaphore because we want to let all requests run at once, just block at the retrieve user anyway
    // As much as it would be fun to just let it rip, the pool has max 8 connections so it gets blocked on the pool
    val semaphore = Semaphore(128) // For some reason more than 1 permits slow down the card banner generation code (why?)
    // ANSWER: synchronization on IndexColorModel caused this

    val fullJobStart = Clock.System.now()
    (staffProfiles + moneyProfiles).forEachIndexed { index, it ->
        val thisCardId = cardId++

        jobs.add(
            GlobalScope.async {
                try {
                    semaphore.withPermit {
                        println(it[Profiles.id].toString() + ": " + it[Profiles.money])

                        val user = preCachedUsers[it[Profiles.id].value] ?: jda.retrieveUserById(it[Profiles.id].value)
                            .await()

                        val activeBackgroundId = it[UserSettings.activeBackground]
                        val activeBadgeId = it[UserSettings.activeBadge]

                        val activeBackgroundUrl = getUserProfileBackgroundUrl(
                            pudding,
                            dreamStorageService,
                            "https://assets.perfectdreams.media/loritta/backgrounds/",
                            it[Profiles.id].value,
                            it[UserSettings.id].value,
                            "defaultDark",
                            activeBackgroundId?.value ?: Background.DEFAULT_BACKGROUND_ID,
                        )

                        println("Active Background: ${activeBackgroundId?.value} / $activeBackgroundUrl")

                        val activeBadge = badges.firstOrNull { it.id == activeBadgeId }

                        val badgeImage: BufferedImage?
                        val badgeTitle: String?

                        if (activeBadge != null) {
                            if (activeBadge.checkIfUserDeservesBadge(
                                    ProfileUserInfoData(
                                        user.idLong,
                                        user.name,
                                        user.discriminator,
                                        user.effectiveAvatarUrl,
                                        user.isBot,
                                        user.flags
                                    ),
                                    pudding.transaction { Profile.wrapRow(it) },
                                    setOf()
                                )
                            ) {
                                badgeImage = readImageFromResources("/badges/${activeBadge.badgeFileName}")
                                badgeTitle = i18nContext.get(activeBadge.title)
                            } else {
                                badgeImage = null
                                badgeTitle = null
                            }
                        } else {
                            val hardcodedBadge = hardcodedBadges.firstOrNull { it.id == activeBadgeId }
                            if (hardcodedBadge != null) {
                                badgeImage = readImageFromResources("/badges/${hardcodedBadge.badgeFileName}")
                                badgeTitle = i18nContext.get(hardcodedBadge.title)
                            } else {
                                badgeImage = null
                                badgeTitle = null
                            }
                        }

                        val moneyIndex = moneyProfiles.indexOf(it)
                        if (it[Profiles.id].value !in staffIds && moneyIndex == -1)
                            error("Something went wrong!")

                        val userAvatarUrl = blacklistedUserAvatarsIds[it[Profiles.id].value] ?: user.getEffectiveAvatarUrl(ImageFormat.PNG)
                        val cardGenData = LoriCoolCardsManager.CardGenData(
                            thisCardId.toString().padStart(4, '0'),
                            if (it[Profiles.id].value in staffIds)
                                CardRarity.MYTHIC
                            else if (moneyIndex in 0 until 10) {
                                // 10 stickers = legendary
                                CardRarity.LEGENDARY
                            } else if (moneyIndex in 0 until 50) {
                                // 40 stickers = epic
                                CardRarity.EPIC
                            } else if (moneyIndex in 0 until 150) {
                                // 100 stickers = rare
                                CardRarity.RARE
                            } else if (moneyIndex in 0 until 300) {
                                // 150 stickers = uncommon
                                CardRarity.UNCOMMON
                                // 200 stickers (the rest) = common
                            } else if (moneyIndex in 0 until 500) {
                                CardRarity.COMMON
                            } else error("Whoops $index"),
                            user.name.lowercase(),
                            ImageIO.read(URL(userAvatarUrl)),
                            ImageIO.read(URL(activeBackgroundUrl)),
                            badgeTitle,
                            badgeImage
                        )

                        val id = cardGenData.id
                        val rarity = cardGenData.cardRarity
                        val name = cardGenData.name
                        val outputName = "$id-$name-${it[Profiles.id].value}"

                        val start = Clock.System.now()
                        val frontFacingCard = loriCoolCardsManager.generateFrontFacingSticker(cardGenData)

                        ImageIO.write(
                            frontFacingCard,
                            "png",
                            File("/mnt/HDDThings/Pictures/Loritta/LoriCoolCards/$folderName/sticker-$outputName-front.png")
                        )

                        println("Took ${Clock.System.now() - start} to generate the front facing card for ${user.idLong}")

                        val start2 = Clock.System.now()
                        val stickerReceivedGIF = loriCoolCardsManager.generateStickerReceivedGIF(
                            cardGenData.cardRarity,
                            frontFacingCard,
                            LoriCoolCardsManager.StickerReceivedRenderType.LoriCoolCardsEvent
                        )

                        File("/mnt/HDDThings/Pictures/Loritta/LoriCoolCards/$folderName/sticker-$outputName-animated.gif")
                        File("/mnt/HDDThings/Pictures/Loritta/LoriCoolCards/$folderName/sticker-$outputName-animated.gif")
                            .writeBytes(stickerReceivedGIF)

                        println("Took ${Clock.System.now() - start2} to generate the card animated GIF for ${user.idLong}")
                        println("Took ${Clock.System.now() - start} to generate everything for ${user.idLong}")

                        fileMutex.withLock {
                            sqlCommandsFile.appendText("INSERT INTO loricoolcardseventcards (event, card_id, rarity, title, card_front_image_url, card_received_image_url, metadata) VALUES (1, '#$id', '${rarity.name}', '$name', 'https://stuff.loritta.website/loricoolcards/production/v4/stickers/sticker-$outputName-front.png', 'https://stuff.loritta.website/loricoolcards/production/v4/stickers/sticker-$outputName-animated.gif', '${Json.encodeToString<StickerMetadata>(StickerMetadata.DiscordUserStickerMetadata(user.idLong))}');\n")
                        }
                    }
                } catch (e: Exception) {
                    println("Fail!")
                    e.printStackTrace()
                }
            }
        )
    }

    GlobalScope.launch(Executors.newFixedThreadPool(1).asCoroutineDispatcher()) {
        while (true) {
            println("Generations: ${jobs.count { it.isCompleted }}/${jobs.size} - Progress: ${Clock.System.now() - fullJobStart}")
            delay(1_000)
        }
    }

    jobs.forEach { it.join() }

    println("Finished in ${Clock.System.now() - fullJobStart}")
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
    pudding: Pudding,
    dreamStorageService: DreamStorageServiceClient,
    etherealGambiServiceUrl: String,
    userId: Long,
    settingsId: Long,
    activeProfileDesignInternalName: String,
    activeBackgroundInternalName: String
): String {
    val defaultBlueBackground = pudding.backgrounds.getBackground(Background.DEFAULT_BACKGROUND_ID)!!
    var background = pudding.backgrounds.getBackground(activeBackgroundInternalName) ?: defaultBlueBackground

    if (background.id == Background.RANDOM_BACKGROUND_ID) {
        // If the user selected a random background, we are going to get all the user's backgrounds and choose a random background from the list
        val allBackgrounds = mutableListOf(defaultBlueBackground)

        allBackgrounds.addAll(
            pudding.transaction {
                (BackgroundPayments innerJoin Backgrounds).selectAll().where {
                    BackgroundPayments.userId eq userId
                }.map {
                    val data = Background.fromRow(it)
                    PuddingBackground(
                        pudding,
                        data
                    )
                }
            }
        )

        background = allBackgrounds.random()
    }

    if (background.id == Background.CUSTOM_BACKGROUND_ID) {
        // Custom background
        val donationValue = getActiveMoneyFromDonations(pudding, userId)
        val plan = UserPremiumPlans.getPlanFromValue(donationValue)

        if (plan.customBackground) {
            val dssNamespace = dreamStorageService.getCachedNamespaceOrRetrieve()
            val resultRow = pudding.transaction {
                CustomBackgroundSettings.selectAll().where { CustomBackgroundSettings.settings eq settingsId }
                    .firstOrNull()
            }

            // If the path exists, then the background (probably!) exists
            if (resultRow != null) {
                val file = resultRow[net.perfectdreams.loritta.cinnamon.pudding.tables.CustomBackgroundSettings.file]
                val extension = MediaTypeUtils.convertContentTypeToExtension(resultRow[net.perfectdreams.loritta.cinnamon.pudding.tables.CustomBackgroundSettings.preferredMediaType])
                return "${dreamStorageService.baseUrl}/$dssNamespace/${StoragePaths.CustomBackground(userId, file).join()}.$extension"
            }
        }

        // If everything fails, change the background to the default blue background
        // This is required because the current background is "CUSTOM", so Loritta will try getting the default variation of the custom background...
        // but that doesn't exist!
        background = defaultBlueBackground
    }

    val dssNamespace = dreamStorageService.getCachedNamespaceOrRetrieve()
    val variation = background.getVariationForProfileDesign(activeProfileDesignInternalName)
    return when (variation.storageType) {
        BackgroundStorageType.DREAM_STORAGE_SERVICE -> getDreamStorageServiceBackgroundUrlWithCropParameters(dreamStorageService.baseUrl, dssNamespace, variation)
        BackgroundStorageType.ETHEREAL_GAMBI -> getEtherealGambiBackgroundUrl(etherealGambiServiceUrl, variation)
    }
}

private fun getDreamStorageServiceBackgroundUrl(
    dreamStorageServiceUrl: String,
    namespace: String,
    background: BackgroundVariation
): String {
    val extension = MediaTypeUtils.convertContentTypeToExtension(background.preferredMediaType)
    return "$dreamStorageServiceUrl/$namespace/${StoragePaths.Background(background.file).join()}.$extension"
}

private fun getDreamStorageServiceBackgroundUrlWithCropParameters(
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

private fun getEtherealGambiBackgroundUrl(etherealGambiServiceUrl: String, background: BackgroundVariation): String {
    val extension = MediaTypeUtils.convertContentTypeToExtension(background.preferredMediaType)
    return etherealGambiServiceUrl.removeSuffix("/") + "/" + background.file + ".$extension"
}

suspend fun getActiveMoneyFromDonations(pudding: Pudding, userId: Long): Double {
    return pudding.transaction { _getActiveMoneyFromDonations(userId) }
}

fun _getActiveMoneyFromDonations(userId: Long): Double {
    return Payment.find {
        (Payments.expiresAt greaterEq System.currentTimeMillis()) and
                (Payments.reason eq PaymentReason.DONATION) and
                (Payments.userId eq userId)
    }.sumByDouble {
        // This is a weird workaround that fixes users complaining that 19.99 + 19.99 != 40 (it equals to 39.38()
        ceil(it.money.toDouble())
    }
}

data class HardcodedBadge(
    val id: UUID,
    val title: StringI18nData,
    val badgeFileName: String
)