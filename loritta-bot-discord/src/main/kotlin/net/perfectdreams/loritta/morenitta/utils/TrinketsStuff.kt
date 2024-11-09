package net.perfectdreams.loritta.morenitta.utils

import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.*
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.selectFirstOrNull
import net.perfectdreams.loritta.common.utils.Rarity
import net.perfectdreams.loritta.serializable.BackgroundStorageType
import net.perfectdreams.loritta.serializable.Rectangle
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*

/**
 * Inserts and updates shop items data
 */
object TrinketsStuff {
    private val logger = KotlinLogging.logger {}

    // ===[ PROFILE GROUP UUIDS ]===
    /**
     * "Center Right Focus" Profile Group ID, background focus is in the center-right of the profile
     */
    private val CENTER_RIGHT_FOCUS_DESIGN = UUID.fromString("d77948dc-0263-49f9-9da0-60b76ca14af8")
    /**
     * "Center Top Focus" Profile Group ID, background focus is in the top-center of the profile
     */
    private val CENTER_TOP_FOCUS_DESIGN = UUID.fromString("bc4c8654-ddd2-48c6-8427-7e8c9f7426c0")

    private val centerRightFocusDesigns = listOf(
        "defaultBlurple",
        "defaultRed",
        "defaultBlue",
        "defaultGreen",
        "defaultPurple",
        "defaultPink",
        "defaultYellow",
        "defaultOrange",
        "defaultDark",
        "defaultEaster2023"
    )

    private val topFocusDesigns = listOf(
        "plainWhite",
        "plainOrange",
        "plainPurple",
        "plainAqua",
        "plainGreen",
        "plainGreenHearts",
        "cowboy",
        "halloween2019",
        "christmas2019",
        "lorittaChristmas2019",
        "undertaleBattle",
        "msn"
    )

    private val sets = setOf(
        "cuteFoodFaces",
        "sadCats",
        "discord",
        "shibas",
        "stevenUniverse",
        "akira",
        "hamptonAndTheHampsters",
        "gravityFalls",
        "lorittaBirthday2020",
        "lowPoly",
        "landscapes",
        "jojo",
        "transformice",
        "brawlStars",
        "hollowKnight",
        "devilMayCry",
        "leagueOfLegends",
        "adventureTime",
        "amongUs",
        "luckyStar",
        "sonic"
    )

    fun updateTrinkets(pudding: Pudding) {
        runBlocking {
            pudding.transaction(repetitions = Int.MAX_VALUE) {
                // ===[ SETS ]===
                for (set in sets) {
                    Sets.insertIgnore {
                        it[Sets.internalName] = set
                    }
                }

                // ===[ PROFILE DESIGNS ]===
                profileDesigns()

                // ===[ PROFILE GROUPS ]===
                // Validate if the profile designs exist
                if (
                    centerRightFocusDesigns.size != ProfileDesigns
                        .select {
                            ProfileDesigns.id inList centerRightFocusDesigns
                        }.count().toInt()
                ) {
                    logger.warn { "There are missing profile designs the database related to the centerRightFocusDesigns! We are going to skip the trinkets update process..." }
                    return@transaction
                }

                if (
                    topFocusDesigns.size != ProfileDesigns
                        .select {
                            ProfileDesigns.id inList topFocusDesigns
                        }.count().toInt()
                ) {
                    logger.warn { "There are missing profile designs the database related to the topFocusDesigns! We are going to skip the trinkets update process..." }
                    return@transaction
                }

                ProfileDesignGroups.upsert(ProfileDesignGroups.id) {
                    it[ProfileDesignGroups.id] = CENTER_RIGHT_FOCUS_DESIGN
                }

                ProfileDesignGroups.upsert(ProfileDesignGroups.id) {
                    it[ProfileDesignGroups.id] = CENTER_TOP_FOCUS_DESIGN
                }

                // ===[ PROFILE GROUP ENTRIES ]===
                ProfileDesignGroupEntries.deleteWhere {
                    ProfileDesignGroupEntries.profileDesignGroup eq CENTER_RIGHT_FOCUS_DESIGN and (ProfileDesignGroupEntries.profileDesign notInList centerRightFocusDesigns)
                }
                for (profileDesign in centerRightFocusDesigns) {
                    ProfileDesignGroupEntries.upsert(
                        ProfileDesignGroupEntries.profileDesign,
                        ProfileDesignGroupEntries.profileDesignGroup
                    ) {
                        it[ProfileDesignGroupEntries.profileDesign] = profileDesign
                        it[ProfileDesignGroupEntries.profileDesignGroup] = CENTER_RIGHT_FOCUS_DESIGN
                    }
                }

                ProfileDesignGroupEntries.deleteWhere {
                    ProfileDesignGroupEntries.profileDesignGroup eq CENTER_TOP_FOCUS_DESIGN and (ProfileDesignGroupEntries.profileDesign notInList topFocusDesigns)
                }
                for (profileDesign in topFocusDesigns) {
                    ProfileDesignGroupEntries.upsert(
                        ProfileDesignGroupEntries.profileDesign,
                        ProfileDesignGroupEntries.profileDesignGroup
                    ) {
                        it[ProfileDesignGroupEntries.profileDesign] = profileDesign
                        it[ProfileDesignGroupEntries.profileDesignGroup] = CENTER_TOP_FOCUS_DESIGN
                    }
                }

                backgrounds()
            }
        }
    }

    // You may be wondering "wow, splitting up the backgrounds in a separate function to be more tidier, awesome!" but there's a reason why it is like that!
    // Looks like that, if you have a lot of functions in a single suspendable function, stuff breaks and explodes!
    // e: java.lang.StackOverflowError
    //        at org.jetbrains.kotlin.cfg.ControlFlowInformationProviderImpl.markImplicitReceiverOfSuspendLambda$dfs(ControlFlowInformationProviderImpl.kt:885)
    private fun profileDesigns() {
        createProfileDesign("defaultDark", true, Rarity.COMMON, LocalDate.of(0, 1, 1), availableToBuyViaSonhos = false)
        createProfileDesign("debug", true, Rarity.COMMON, LocalDate.of(0, 1, 1), availableToBuyViaSonhos = false)
        createProfileDesign("modernBlurple", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1))
        createProfileDesign("msn", true, Rarity.RARE, LocalDate.of(0, 1, 1))
        createProfileDesign("orkut", true, Rarity.RARE, LocalDate.of(0, 1, 1))
        createProfileDesign("plainWhite", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), createdBy = listOf("brigadeirim"))
        createProfileDesign("plainOrange", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), createdBy = listOf("brigadeirim"))
        createProfileDesign("plainPurple", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), createdBy = listOf("brigadeirim"))
        createProfileDesign("plainAqua", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), createdBy = listOf("brigadeirim"))
        createProfileDesign("plainGreen", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), createdBy = listOf("brigadeirim"))
        createProfileDesign("plainGreenHearts", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), createdBy = listOf("brigadeirim"))
        createProfileDesign("cowboy", true, Rarity.RARE, LocalDate.of(0, 1, 1), createdBy = listOf("brigadeirim"))
        createProfileDesign("nextGenDark", true, Rarity.RARE, LocalDate.of(0, 1, 1), createdBy = listOf("peterstark000"))
        createProfileDesign("monicaAta", true, Rarity.EPIC, LocalDate.of(0, 1, 1), createdBy = listOf("brigadeirim"))
        createProfileDesign("loriAta", true, Rarity.EPIC, LocalDate.of(0, 1, 1), createdBy = listOf("brigadeirim", "allouette"))
        createProfileDesign("undertaleBattle", true, Rarity.EPIC, LocalDate.of(0, 1, 1), createdBy = listOf("allouette"))
        createProfileDesign("halloween2019", true, Rarity.LEGENDARY, LocalDate.of(0, 1, 1), availableToBuyViaSonhos = false)
        createProfileDesign("christmas2019", true, Rarity.LEGENDARY, LocalDate.of(0, 1, 1), availableToBuyViaSonhos = false)
        createProfileDesign("lorittaChristmas2019", true, Rarity.LEGENDARY, LocalDate.of(0, 1, 1), availableToBuyViaSonhos = false)
        createProfileDesign("defaultBlurple", true, Rarity.COMMON, LocalDate.of(2020, 11, 22))
        createProfileDesign("defaultRed", true, Rarity.COMMON, LocalDate.of(2020, 11, 22))
        createProfileDesign("defaultBlue", true, Rarity.COMMON, LocalDate.of(2020, 11, 22))
        createProfileDesign("defaultGreen", true, Rarity.COMMON, LocalDate.of(2020, 11, 22))
        createProfileDesign("defaultPurple", true, Rarity.COMMON, LocalDate.of(2020, 11, 22))
        createProfileDesign("defaultPink", true, Rarity.COMMON, LocalDate.of(2020, 11, 22))
        createProfileDesign("defaultYellow", true, Rarity.COMMON, LocalDate.of(2020, 11, 22))
        createProfileDesign("defaultOrange", true, Rarity.COMMON, LocalDate.of(2020, 11, 22))
        createProfileDesign("defaultEaster2023", true, Rarity.LEGENDARY, LocalDate.of(2023, 4, 15), availableToBuyViaSonhos = false)
        createProfileDesign("loriCoolCardsStickerReceivedCommon", true, Rarity.LEGENDARY, LocalDate.of(2024, 4, 18), availableToBuyViaSonhos = false)
        createProfileDesign("loriCoolCardsStickerReceivedUncommon", true, Rarity.LEGENDARY, LocalDate.of(2024, 4, 18), availableToBuyViaSonhos = false)
        createProfileDesign("loriCoolCardsStickerReceivedRare", true, Rarity.LEGENDARY, LocalDate.of(2024, 4, 18), availableToBuyViaSonhos = false)
        createProfileDesign("loriCoolCardsStickerReceivedEpic", true, Rarity.LEGENDARY, LocalDate.of(2024, 4, 18), availableToBuyViaSonhos = false)
        createProfileDesign("loriCoolCardsStickerReceivedLegendary", true, Rarity.LEGENDARY, LocalDate.of(2024, 4, 18), availableToBuyViaSonhos = false)
        createProfileDesign("loriCoolCardsStickerReceivedMythic", true, Rarity.LEGENDARY, LocalDate.of(2024, 4, 18), availableToBuyViaSonhos = false)
        createProfileDesign("loriCoolCardsStickerReceivedPlainCommon", true, Rarity.LEGENDARY, LocalDate.of(2024, 4, 18), availableToBuyViaSonhos = false)
        createProfileDesign("loriCoolCardsStickerReceivedPlainUncommon", true, Rarity.LEGENDARY, LocalDate.of(2024, 4, 18), availableToBuyViaSonhos = false)
        createProfileDesign("loriCoolCardsStickerReceivedPlainRare", true, Rarity.LEGENDARY, LocalDate.of(2024, 4, 18), availableToBuyViaSonhos = false)
        createProfileDesign("loriCoolCardsStickerReceivedPlainEpic", true, Rarity.LEGENDARY, LocalDate.of(2024, 4, 18), availableToBuyViaSonhos = false)
        createProfileDesign("loriCoolCardsStickerReceivedPlainLegendary", true, Rarity.LEGENDARY, LocalDate.of(2024, 4, 18), availableToBuyViaSonhos = false)
        createProfileDesign("loriCoolCardsStickerReceivedPlainMythic", true, Rarity.LEGENDARY, LocalDate.of(2024, 4, 18), availableToBuyViaSonhos = false)
    }

    private fun backgrounds() {
        // ===[ INTERNAL BACKGROUNDS ]===
        // These do not require a default variant
        createBackground("random", true, Rarity.COMMON, LocalDate.of(0, 1, 1), availableToBuyViaSonhos = false)
        createBackground("custom", true, Rarity.COMMON, LocalDate.of(0, 1, 1), availableToBuyViaSonhos = false)

        // ===[ DEFAULT BACKGROUND STYLES ]===
        createBackground("defaultRed", true, Rarity.COMMON, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("default-red", ContentType.Image.PNG)
        }
        createBackground("defaultGreen", true, Rarity.COMMON, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("default-green", ContentType.Image.PNG)
        }
        createBackground("defaultOrange", true, Rarity.COMMON, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("default-orange", ContentType.Image.PNG)
        }
        createBackground("defaultPink", true, Rarity.COMMON, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("default-pink", ContentType.Image.PNG)
        }
        createBackground("defaultPurple", true, Rarity.COMMON, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("default-purple", ContentType.Image.PNG)
        }
        createBackground("defaultBlue", true, Rarity.COMMON, LocalDate.of(0, 1, 1), availableToBuyViaSonhos = false) {
            addDefaultVariant("default-blue", ContentType.Image.PNG)
        }

        createBackground("cute_black_cat", true, Rarity.UNCOMMON, LocalDate.of(2020, 7, 24)) {
            addDefaultVariant("cute-black-cat", ContentType.Image.JPEG)
        }
        createBackground("lorittaAndPantufa", true, Rarity.RARE, LocalDate.of(0, 1, 1), createdBy = listOf("allouette")) {
            addDefaultVariant("loritta-and-pantufa", ContentType.Image.PNG, Rectangle(190, 0, 400, 300))
        }
        createBackground("dogeWolf", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("doge-wolf", ContentType.Image.PNG, Rectangle(160, 0, 800, 600))
        }
        createBackground("dorimeSortros", true, Rarity.RARE, LocalDate.of(0, 1, 1), createdBy = listOf("sortrosphoresia")) {
            addDefaultVariant("dorime-sortros", ContentType.Image.PNG, Rectangle(200, 0, 1600, 1200))
        }
        createBackground("dogeAngry", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("doge-angry", ContentType.Image.PNG)
        }
        createBackground("umasou1", true, Rarity.RARE, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("umasou", ContentType.Image.JPEG, Rectangle(0, 0, 1440, 1080))
        }
        createBackground("archerIllya", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("archer-illya", ContentType.Image.PNG, Rectangle(0, 0, 1440, 1080))
        }
        createBackground("lumenParkour", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("lumen-parkour", ContentType.Image.PNG, Rectangle(112, 0, 800, 600))
        }
        createBackground("windowsXpBliss", true, Rarity.RARE, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("windows-xp-bliss", ContentType.Image.JPEG, Rectangle(240, 0, 1440, 1080))
        }
        createBackground("countryBalls", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("country-balls", ContentType.Image.PNG)
        }
        createBackground("ageOfEmpires2De", true, Rarity.EPIC, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("age-of-empires-2-de", ContentType.Image.JPEG, Rectangle(240, 0, 1440, 1080))
        }
        createBackground("ageOfEmpiresDe", true, Rarity.EPIC, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("age-of-empires-de", ContentType.Image.JPEG, Rectangle(240, 0, 1440, 1080))
        }
        createBackground("ehMole", true, Rarity.EPIC, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("eh-mole", ContentType.Image.PNG)
        }
        createBackground("emojo", true, Rarity.EPIC, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("emojo", ContentType.Image.JPEG, Rectangle(160, 0, 960, 720))
        }
        createBackground("cryingLonely", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), createdBy = listOf("delly1000")) {
            addDefaultVariant("crying-lonely", ContentType.Image.PNG, Rectangle(200, 0, 1200, 1000))
        }
        createBackground("sadCatTeddyBear", true, Rarity.RARE, LocalDate.of(0, 1, 1), set = "sadCats") {
            addDefaultVariant("sad-cat-teddy-bear", ContentType.Image.JPEG)
        }
        createBackground("sadCatPhone", true, Rarity.RARE, LocalDate.of(0, 1, 1), set = "sadCats") {
            addDefaultVariant("sad-cat-phone", ContentType.Image.JPEG)
        }
        createBackground("sadCat", true, Rarity.RARE, LocalDate.of(0, 1, 1), set = "sadCats") {
            addDefaultVariant("sad-cat", ContentType.Image.JPEG, Rectangle(125, 0, 600, 450))
        }
        createBackground("sadCatMirror", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), set = "sadCats") {
            addDefaultVariant("sad-cat-mirror", ContentType.Image.JPEG, Rectangle(0, 0, 540, 360))
        }
        createBackground("sadCatSleepy", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), set = "sadCats") {
            addDefaultVariant("sad-cat-sleepy", ContentType.Image.JPEG, Rectangle(0, 0, 880, 610))
        }
        createBackground("defaultBlueLori", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("default-blue-lori", ContentType.Image.PNG)
        }
        createBackground("defaultGreenLori", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("default-green-lori", ContentType.Image.PNG)
        }
        createBackground("defaultOrangeLori", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("default-orange-lori", ContentType.Image.PNG)
        }
        createBackground("defaultPinkLori", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("default-pink-lori", ContentType.Image.PNG)
        }
        createBackground("defaultPurpleLori", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("default-purple-lori", ContentType.Image.PNG)
        }
        createBackground("defaultRedLori", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("default-red-lori", ContentType.Image.PNG)
        }
        createBackground("wumpusSad", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), set = "discord") {
            addDefaultVariant("wumpus-sad", ContentType.Image.PNG)
        }
        createBackground("wumpusHoodieCool", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), set = "discord") {
            addDefaultVariant("wumpus-hoodie-cool", ContentType.Image.PNG, Rectangle(100, 0, 1000, 800))
        }
        createBackground("wumpusGame", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), set = "discord") {
            addDefaultVariant("wumpus-game", ContentType.Image.PNG)
        }
        createBackground("wumpusHeart", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), set = "discord") {
            addDefaultVariant("wumpus-heart", ContentType.Image.PNG, Rectangle(100, 0, 1000, 800))
        }
        createBackground("smudgeTheCat", true, Rarity.EPIC, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("smudge-the-cat", ContentType.Image.JPEG)
        }
        createBackground("sadCatShower", true, Rarity.RARE, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("sad-cat-shower", ContentType.Image.JPEG, Rectangle(0, 380, 1280, 980))
        }
        createBackground("sadCatPainting", true, Rarity.EPIC, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("sad-cat-painting", ContentType.Image.JPEG)
        }
        createBackground("wilsonFusca", true, Rarity.EPIC, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("wilson-fusca", ContentType.Image.JPEG, Rectangle(0, 0, 630, 460))
        }
        createBackground("tavares", true, Rarity.RARE, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("tavares", ContentType.Image.JPEG, Rectangle(0, 0, 920, 720))
        }
        createBackground("dogeSurprised", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("doge-surprised", ContentType.Image.JPEG)
        }
        createBackground("doge", true, Rarity.RARE, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("doge", ContentType.Image.PNG, Rectangle(200, 0, 900, 700))
        }
        createBackground("dogeCool", true, Rarity.EPIC, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("doge-cool", ContentType.Image.JPEG)
        }
        createBackground("dogeCabin", true, Rarity.RARE, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("doge-cabin", ContentType.Image.JPEG, Rectangle(0, 50, 600, 500))
        }
        createBackground("ashComputer", true, Rarity.EPIC, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("ash-computer", ContentType.Image.JPEG)
        }
        createBackground("katyKatAnime", true, Rarity.RARE, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("katy-kat-anime", ContentType.Image.PNG)
        }
        createBackground("shadowToilet", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("shadow-toilet", ContentType.Image.JPEG)
        }
        createBackground("dogIdk", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("dog-idk", ContentType.Image.JPEG, Rectangle(0, 0, 641, 577))
        }
        createBackground("kotlinChan", true, Rarity.EPIC, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("kotlin-chan", ContentType.Image.JPEG, Rectangle(0, 50, 500, 400))
        }
        createBackground("loriSextou", true, Rarity.RARE, LocalDate.of(0, 1, 1), createdBy = listOf("inksans")) {
            addDefaultVariant("lori-sextou", ContentType.Image.PNG, Rectangle(100, 0, 1000, 688))
        }
        createBackground("allouetteLoritta", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), createdBy = listOf("allouette")) {
            addDefaultVariant("allouette-loritta", ContentType.Image.PNG)
        }
        createBackground("coxinha", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), set = "cuteFoodFaces", createdBy = listOf("brigadeirim")) {
            addDefaultVariant("coxinha", ContentType.Image.PNG)
        }
        createBackground("kibe", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), set = "cuteFoodFaces", createdBy = listOf("brigadeirim")) {
            addDefaultVariant("kibe", ContentType.Image.PNG)
        }
        createBackground("greenApple", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), set = "cuteFoodFaces", createdBy = listOf("brigadeirim")) {
            addDefaultVariant("green-apple", ContentType.Image.PNG)
        }
        createBackground("hotDog", true, Rarity.RARE, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("hot-dog", ContentType.Image.JPEG, Rectangle(0, 10, 800, 600))
        }
        createBackground("discordLogin", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), set = "discord") {
            addDefaultVariant("discord-login", ContentType.Image.JPEG, Rectangle(240, 0, 1440, 1080))
        }
        createBackground("loud", true, Rarity.RARE, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("loud", ContentType.Image.JPEG, Rectangle(240, 0, 1440, 1080))
        }
        createBackground("neekoNeekoNii", true, Rarity.EPIC, LocalDate.of(0, 1, 1), createdBy = listOf("vta7991")) {
            addDefaultVariant("neeko-neeko-nii", ContentType.Image.PNG, Rectangle(225, 0, 1440, 1080))
        }
        createBackground("sortrosFranbow", true, Rarity.RARE, LocalDate.of(0, 1, 1), createdBy = listOf("sortrosphoresia")) {
            addDefaultVariant("sortros-franbow", ContentType.Image.PNG, Rectangle(100, 0, 2000, 1600))
        }
        createBackground("sortrosMisfortune", true, Rarity.RARE, LocalDate.of(0, 1, 1), createdBy = listOf("sortrosphoresia")) {
            addDefaultVariant("sortros-misfortune", ContentType.Image.PNG, Rectangle(500, 100, 1600, 1200))
        }
        createBackground("redApple", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), set = "cuteFoodFaces", createdBy = listOf("brigadeirim")) {
            addDefaultVariant("red-apple", ContentType.Image.PNG)
        }
        createBackground("redAppleEat", true, Rarity.RARE, LocalDate.of(0, 1, 1), set = "cuteFoodFaces", createdBy = listOf("brigadeirim")) {
            addDefaultVariant("red-apple-eat", ContentType.Image.PNG)
        }
        createBackground("dokyoSweater", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), createdBy = listOf("dokyotv")) {
            addDefaultVariant("dokyo-sweater", ContentType.Image.PNG, Rectangle(100, 0, 1360, 922))
        }
        createBackground("hampsterdance", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), set = "hamptonAndTheHampsters") {
            addDefaultVariant("hampton-and-the-hampsters", ContentType.Image.PNG)
        }
        createBackground("bmwM3Gtr2005", true, Rarity.RARE, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("bmw-m3-gtr-2005", ContentType.Image.JPEG, Rectangle(122, 0, 800, 600))
        }
        createBackground("pogChamp", true, Rarity.EPIC, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("pogchamp", ContentType.Image.JPEG)
        }
        createBackground("studiopolis", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("studiopolis", ContentType.Image.PNG)
        }
        createBackground("sakuraAdr", true, Rarity.EPIC, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("sakura-adr", ContentType.Image.PNG, Rectangle(0, 0, 800, 587))
        }
        createBackground("obraDinn", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("obra-dinn", ContentType.Image.PNG, Rectangle(240, 0, 1440, 1080))
        }
        createBackground("starMabel", true, Rarity.RARE, LocalDate.of(0, 1, 1), set = "gravityFalls", createdBy = listOf("469709885710270484")) {
            addDefaultVariant("star-mabel", ContentType.Image.PNG, Rectangle(0, 0, 2200, 1600))
        }
        createBackground("celesteTower", true, Rarity.RARE, LocalDate.of(0, 1, 1), set = "stevenUniverse") {
            addDefaultVariant("celeste-tower", ContentType.Image.JPEG, Rectangle(0, 0, 1440, 1080))
        }
        createBackground("crystalTemple", true, Rarity.RARE, LocalDate.of(0, 1, 1), set = "stevenUniverse") {
            addDefaultVariant("crystal-temple", ContentType.Image.JPEG, Rectangle(240, 0, 1440, 1080))
        }
        createBackground("fordMustang", true, Rarity.RARE, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("ford-mustang", ContentType.Image.JPEG, Rectangle(100, 0, 1080, 720))
        }
        createBackground("stevenPizza", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), set = "stevenUniverse") {
            addDefaultVariant("steven-pizza", ContentType.Image.PNG)
        }
        createBackground("rainbowDashPinkFloyd", true, Rarity.RARE, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("rainbow-dash-pink-floyd", ContentType.Image.PNG)
        }
        createBackground("lifeIsStrange", true, Rarity.EPIC, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("life-is-strange", ContentType.Image.JPEG, Rectangle(240, 0, 1440, 1080))
        }
        createBackground("kanedaBikeSkid", true, Rarity.EPIC, LocalDate.of(0, 1, 1), set = "akira") {
            addDefaultVariant("kaneda-bike-skid", ContentType.Image.JPEG, Rectangle(150, 0, 2000, 1352))
        }
        createBackground("kaneda", true, Rarity.EPIC, LocalDate.of(0, 1, 1), set = "akira") {
            addDefaultVariant("kaneda", ContentType.Image.JPEG, Rectangle(50, 0, 800, 600))
        }
        createBackground("ponnyBunny", true, Rarity.RARE, LocalDate.of(0, 1, 1), createdBy = listOf("s2inner")) {
            addDefaultVariant("ponny-bunny", ContentType.Image.PNG, Rectangle(0, 200, 1000, 800))
        }
        createBackground("mar", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), createdBy = listOf("s2inner")) {
            addDefaultVariant("mar", ContentType.Image.PNG, Rectangle(0, 150, 1000, 800))
        }
        createBackground("paola", true, Rarity.RARE, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("paola", ContentType.Image.PNG, Rectangle(170, 0, 700, 545))
        }
        createBackground("alanChefe", true, Rarity.EPIC, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("alan-chefe", ContentType.Image.JPEG, Rectangle(0, 0, 1100, 800))
        }
        createBackground("loriCorona", true, Rarity.EPIC, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("lori-corona", ContentType.Image.PNG, Rectangle(250, 0, 920, 720))
        }
        createBackground("lowPolyPlanet", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), set = "lowPoly", createdBy = listOf("brigadeirim")) {
            addDefaultVariant("low-poly-planet", ContentType.Image.PNG)
        }
        createBackground("jonatanMoon", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), createdBy = listOf("jonatanfelipe10")) {
            addDefaultVariant("jonatan-lua", ContentType.Image.PNG, Rectangle(0, 0, 920, 720))
        }
        createBackground("minecraftWindowFox", true, Rarity.RARE, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("minecraft-window-fox", ContentType.Image.PNG, Rectangle(220, 0, 966, 705))
        }
        createBackground("countryBalls2", true, Rarity.RARE, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("country-balls-2", ContentType.Image.PNG, Rectangle(100, 0, 1080, 720))
        }
        createBackground("bothWorlds", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("both-worlds", ContentType.Image.PNG, Rectangle(160, 0, 1024, 768))
        }
        createBackground("lowPolyRocket", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), set = "lowPoly", createdBy = listOf("brigadeirim")) {
            addDefaultVariant("low-poly-rocket", ContentType.Image.PNG)
        }
        createBackground("sunsetRiver", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), set = "landscapes") {
            addDefaultVariant("sunset-river", ContentType.Image.PNG)
        }
        createBackground("auroraBorealis", true, Rarity.RARE, LocalDate.of(0, 1, 1), set = "landscapes") {
            addDefaultVariant("aurora-borealis", ContentType.Image.PNG)
        }
        createBackground("starryNight", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), set = "landscapes") {
            addDefaultVariant("starry-night", ContentType.Image.PNG)
        }
        createBackground("constellations", true, Rarity.EPIC, LocalDate.of(0, 1, 1), set = "landscapes") {
            addDefaultVariant("constellations", ContentType.Image.PNG)
        }
        createBackground("grass", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), set = "landscapes") {
            addDefaultVariant("grass", ContentType.Image.PNG)
        }
        createBackground("mist", true, Rarity.RARE, LocalDate.of(0, 1, 1), set = "landscapes", createdBy = listOf("brigadeirim")) {
            addDefaultVariant("mist", ContentType.Image.PNG)
        }
        createBackground("sadCatButter", true, Rarity.RARE, LocalDate.of(0, 1, 1), set = "sadCats") {
            addDefaultVariant("sad-cat-butter", ContentType.Image.JPEG, Rectangle(0, 50, 500, 400))
        }
        createBackground("bunchOfHearts", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), createdBy = listOf("brigadeirim")) {
            addDefaultVariant("bunch-of-hearts", ContentType.Image.PNG)
        }
        createBackground("wumpusHackweekPeter", true, Rarity.RARE, LocalDate.of(0, 1, 1), set = "discord", createdBy = listOf("peterstark000")) {
            addDefaultVariant("wumpus-hackweek-peter", ContentType.Image.PNG)
        }
        createBackground("wumpusCool", true, Rarity.RARE, LocalDate.of(0, 1, 1), set = "discord") {
            addDefaultVariant("wumpus-cool", ContentType.Image.PNG, Rectangle(37, 0, 2025, 1500))
        }
        createBackground("wumpusLeaf", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), set = "discord") {
            addDefaultVariant("wumpus-leaf", ContentType.Image.PNG, Rectangle(165, 0, 900, 675))
        }
        createBackground("sadCatZoom", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), set = "sadCats") {
            addDefaultVariant("sad-cat-zoom", ContentType.Image.PNG, Rectangle(0, 115, 368, 268))
        }
        createBackground("chocoholic", true, Rarity.EPIC, LocalDate.of(0, 1, 1), createdBy = listOf("allouette")) {
            addDefaultVariant("chocoholic", ContentType.Image.PNG, Rectangle(100, 0, 760, 540))
        }
        createBackground("lorittaGrafiteira", true, Rarity.EPIC, LocalDate.of(0, 1, 1), createdBy = listOf("1vip")) {
            addDefaultVariant("loritta-grafiteira", ContentType.Image.PNG, Rectangle(300, 0, 1200, 844))
        }
        createBackground("londrinaRosa", true, Rarity.RARE, LocalDate.of(0, 1, 1), set = "landscapes") {
            addDefaultVariant("londrina-rosa", ContentType.Image.JPEG, Rectangle(200, 0, 800, 675))
        }
        createBackground("sadCatDrama", true, Rarity.RARE, LocalDate.of(0, 1, 1), set = "sadCats") {
            addDefaultVariant("sad-cat-drama", ContentType.Image.JPEG, Rectangle(0, 0, 1000, 800))
        }
        createBackground("birthday2020TeamGabriela", true, Rarity.LEGENDARY, LocalDate.of(0, 1, 1), set = "lorittaBirthday2020", createdBy = listOf("sortrosphoresia"), availableToBuyViaSonhos = false) {
            addDefaultVariant("birthday2020-gabriela-teamgabriela", ContentType.Image.PNG)
        }
        createBackground("birthday2020TeamPantufa", true, Rarity.LEGENDARY, LocalDate.of(0, 1, 1), set = "lorittaBirthday2020", createdBy = listOf("sortrosphoresia"), availableToBuyViaSonhos = false) {
            addDefaultVariant("birthday2020-pantufa-teampantufa", ContentType.Image.PNG)
        }
        createBackground("birthday2020Brabas", true, Rarity.LEGENDARY, LocalDate.of(0, 1, 1), set = "lorittaBirthday2020", createdBy = listOf("sortrosphoresia"), availableToBuyViaSonhos = false) {
            addDefaultVariant("birthday2020-brabas", ContentType.Image.PNG)
        }
        createBackground("birthday2020PantufaSonikaSan", true, Rarity.LEGENDARY, LocalDate.of(0, 1, 1), set = "lorittaBirthday2020", createdBy = listOf("508651783330070538"), availableToBuyViaSonhos = false) {
            addDefaultVariant("birthday2020-pantufa-sonikasan", ContentType.Image.PNG)
        }
        createBackground("birthday2020PantufaDelly", true, Rarity.LEGENDARY, LocalDate.of(0, 1, 1), set = "lorittaBirthday2020", createdBy = listOf("delly1000"), availableToBuyViaSonhos = false) {
            addDefaultVariant("birthday2020-pantufa-delly", ContentType.Image.PNG, Rectangle(0, 300, 2600, 2000))
        }
        createBackground("birthday2020PantufaAllouette", true, Rarity.LEGENDARY, LocalDate.of(0, 1, 1), set = "lorittaBirthday2020", createdBy = listOf("allouette"), availableToBuyViaSonhos = false) {
            addDefaultVariant("birthday2020-pantufa-allouette", ContentType.Image.PNG, Rectangle(100, 0, 2600, 2100))
        }
        createBackground("birthday2020GabrielaCoffee", true, Rarity.LEGENDARY, LocalDate.of(0, 1, 1), set = "lorittaBirthday2020", createdBy = listOf("inksans"), availableToBuyViaSonhos = false) {
            addDefaultVariant("birthday2020-gabriela-coffee", ContentType.Image.PNG)
        }
        createBackground("birthday2020GabrielaInnerDesu", true, Rarity.LEGENDARY, LocalDate.of(0, 1, 1), set = "lorittaBirthday2020", createdBy = listOf("s2inner"), availableToBuyViaSonhos = false) {
            addDefaultVariant("birthday2020-gabriela-innerdesu", ContentType.Image.PNG, Rectangle(0, 100, 1000, 800))
        }
        createBackground("birthday2020GabrielaStar", true, Rarity.LEGENDARY, LocalDate.of(0, 1, 1), set = "lorittaBirthday2020", createdBy = listOf("469709885710270484"), availableToBuyViaSonhos = false) {
            addDefaultVariant("birthday2020-gabriela-star", ContentType.Image.PNG, Rectangle(360, 0, 1240, 980))
        }
        createBackground("sadCatCoca", true, Rarity.RARE, LocalDate.of(0, 1, 1), set = "sadCats") {
            addDefaultVariant("sad-cat-coca", ContentType.Image.JPEG, Rectangle(0, 190, 680, 480))
        }
        createBackground("sadCatMoney", true, Rarity.EPIC, LocalDate.of(0, 1, 1), set = "sadCats") {
            addDefaultVariant("sad-cat-money", ContentType.Image.PNG, Rectangle(200, 0, 652, 480))
        }
        createBackground("sadCatFat", true, Rarity.EPIC, LocalDate.of(0, 1, 1), set = "sadCats") {
            addDefaultVariant("sad-cat-fat", ContentType.Image.PNG, Rectangle(0, 0, 900, 709))
        }
        createBackground("birthday2020PantufaHugoo", true, Rarity.LEGENDARY, LocalDate.of(0, 1, 1), set = "lorittaBirthday2020", createdBy = listOf("hugoo"), availableToBuyViaSonhos = false) {
            addDefaultVariant("birthday2020-pantufa-hugoo", ContentType.Image.PNG)
        }
        createBackground("sadCatSushi", true, Rarity.RARE, LocalDate.of(0, 1, 1), set = "sadCats") {
            addDefaultVariant("sad-cat-sushi", ContentType.Image.JPEG, Rectangle(0, 0, 580, 416))
        }
        createBackground("londrinaCyber", true, Rarity.RARE, LocalDate.of(0, 1, 1), set = "landscapes") {
            addDefaultVariant("londrina-cyber", ContentType.Image.JPEG, Rectangle(200, 0, 800, 675))
        }
        createBackground("londrinaAves", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("londrina-aves", ContentType.Image.JPEG, Rectangle(230, 0, 900, 675))
        }
        createBackground("birthday2020GabrielaItsGabi", true, Rarity.LEGENDARY, LocalDate.of(0, 1, 1), set = "lorittaBirthday2020", createdBy = listOf("its_gabi"), availableToBuyViaSonhos = false) {
            addDefaultVariant("birthday2020-gabriela-itsgabi", ContentType.Image.PNG)
        }
        createBackground("sonicSonika", true, Rarity.RARE, LocalDate.of(0, 1, 1), createdBy = listOf("508651783330070538")) {
            addDefaultVariant("sonic-sonika", ContentType.Image.PNG)
        }
        createBackground("loriCrowd", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), createdBy = listOf("brenoplays2")) {
            addDefaultVariant("lori-crowd", ContentType.Image.PNG, Rectangle(50, 0, 599, 460))
        }
        createBackground("starAlissa", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), createdBy = listOf("469709885710270484")) {
            addDefaultVariant("star-alissa", ContentType.Image.PNG)
        }
        createBackground("birthday2020PantufaLaurenha", true, Rarity.LEGENDARY, LocalDate.of(0, 1, 1), set = "lorittaBirthday2020", createdBy = listOf("awebnamoradadealguem"), availableToBuyViaSonhos = false) {
            addDefaultVariant("birthday2020-pantufa-laurenha", ContentType.Image.PNG, Rectangle(0, 0, 900, 700))
        }
        createBackground("birthday2020PantufaOusado", true, Rarity.LEGENDARY, LocalDate.of(0, 1, 1), set = "lorittaBirthday2020", createdBy = listOf("ousado"), availableToBuyViaSonhos = false) {
            addDefaultVariant("birthday2020-pantufa-ousado", ContentType.Image.PNG, Rectangle(0, 0, 1800, 1400))
        }
        createBackground("birthday2020PantufaDezato", true, Rarity.LEGENDARY, LocalDate.of(0, 1, 1), set = "lorittaBirthday2020", createdBy = listOf("yeonjun"), availableToBuyViaSonhos = false) {
            addDefaultVariant("birthday2020-pantufa-dezato", ContentType.Image.PNG, Rectangle(0, 0, 768, 568))
        }
        createBackground("birthday2020GabrielaPinotti", true, Rarity.LEGENDARY, LocalDate.of(0, 1, 1), set = "lorittaBirthday2020", createdBy = listOf("dnpinotti"), availableToBuyViaSonhos = false) {
            addDefaultVariant("birthday2020-gabriela-pinotti", ContentType.Image.PNG)
        }
        createBackground("loritta400k", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), createdBy = listOf("inksans")) {
            addDefaultVariant("loritta-400k", ContentType.Image.PNG)
        }
        createBackground("darkStars", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("dark-stars", ContentType.Image.JPEG)
        }
        createBackground("stevePlane", true, Rarity.EPIC, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("steve-plane", ContentType.Image.JPEG, Rectangle(0, 220, 1080, 759))
        }
        createBackground("cuteBlushCat", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("cute-blush-cat", ContentType.Image.PNG)
        }
        createBackground("birthday2020GabrielaCoffee2", true, Rarity.LEGENDARY, LocalDate.of(0, 1, 1), set = "lorittaBirthday2020", createdBy = listOf("inksans"), availableToBuyViaSonhos = false) {
            addDefaultVariant("birthday2020-gabriela-coffee2", ContentType.Image.PNG, Rectangle(200, 0, 1400, 1152))
        }
        createBackground("deathDance", true, Rarity.EPIC, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("death-dance", ContentType.Image.PNG)
        }
        createBackground("loriEhMole", true, Rarity.EPIC, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("lori-eh-mole", ContentType.Image.PNG)
        }
        createBackground("loboGuara", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("lobo-guara", ContentType.Image.PNG, Rectangle(0, 0, 1600, 1400))
        }
        createBackground("titanicMonarch", true, Rarity.RARE, LocalDate.of(0, 1, 1), set = "sonic") {
            addDefaultVariant("titanic-monarch", ContentType.Image.PNG)
        }
        createBackground("emeraldHill", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), set = "sonic") {
            addDefaultVariant("emerald-hill", ContentType.Image.PNG, Rectangle(250, 0, 1424, 1080))
        }
        createBackground("starAngryPigeon", true, Rarity.UNCOMMON, LocalDate.of(2020, 7, 24), createdBy = listOf("469709885710270484")) {
            addDefaultVariant("star-angry-pigeon", ContentType.Image.PNG)
        }
        createBackground("chemicalPlant", true, Rarity.RARE, LocalDate.of(0, 1, 1), set = "sonic") {
            addDefaultVariant("chemical-plant", ContentType.Image.PNG, Rectangle(0, 0, 1440, 1080))
        }
        createBackground("stardustSpeedwayAct2", true, Rarity.RARE, LocalDate.of(0, 1, 1), set = "sonic") {
            addDefaultVariant("stardust-speedway-act2", ContentType.Image.PNG)
        }
        createBackground("ultimateStonks", true, Rarity.LEGENDARY, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("ultimate-stonks", ContentType.Image.JPEG)
        }
        createBackground("coloredSquares", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("colored-squares", ContentType.Image.JPEG)
        }
        createBackground("furryFright", true, Rarity.EPIC, LocalDate.of(2020, 5, 9)) {
            addDefaultVariant("furry-fright", ContentType.Image.JPEG, Rectangle(0, 80, 900, 700))
        }
        createBackground("konoha", true, Rarity.RARE, LocalDate.of(2020, 5, 9)) {
            addDefaultVariant("konoha", ContentType.Image.PNG, Rectangle(88, 0, 300, 199))
        }
        createBackground("wumpusMovie", true, Rarity.RARE, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("wumpus-movie", ContentType.Image.PNG)
        }
        createBackground("easterStar", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), createdBy = listOf("469709885710270484")) {
            addDefaultVariant("easter-star", ContentType.Image.PNG)
        }
        createBackground("stevenUniverseBeach", true, Rarity.UNCOMMON, LocalDate.of(2020, 5, 9), set = "stevenUniverse") {
            addDefaultVariant("steven-universe-beach", ContentType.Image.JPEG, Rectangle(150, 0, 1440, 1080))
        }
        createBackground("stevenUniverseBigDonut", true, Rarity.RARE, LocalDate.of(2020, 5, 9), set = "stevenUniverse") {
            addDefaultVariant("steven-universe-big-donut", ContentType.Image.JPEG, Rectangle(150, 70, 1440, 1000))
        }
        createBackground("stevenUniverseOceanTemple", true, Rarity.UNCOMMON, LocalDate.of(2020, 5, 9), set = "stevenUniverse") {
            addDefaultVariant("steven-universe-ocean-temple", ContentType.Image.JPEG, Rectangle(255, 80, 1440, 1000))
        }
        createBackground("stevenUniverseLunarSeaTemple1", true, Rarity.UNCOMMON, LocalDate.of(2020, 5, 9), set = "stevenUniverse") {
            addDefaultVariant("steven-universe-lunar-sea-temple1", ContentType.Image.JPEG, Rectangle(255, 0, 1440, 1080))
        }
        createBackground("stevenUniverseLunarSeaTemple2", true, Rarity.RARE, LocalDate.of(2020, 5, 9), set = "stevenUniverse") {
            addDefaultVariant("steven-universe-lunar-sea-temple2", ContentType.Image.JPEG, Rectangle(255, 0, 1440, 1080))
        }
        createBackground("stevenUniverseLunarSeaTemple3", true, Rarity.RARE, LocalDate.of(2020, 5, 9), set = "stevenUniverse") {
            addDefaultVariant("steven-universe-lunar-sea-temple3", ContentType.Image.JPEG, Rectangle(0, 0, 1872, 1080))
        }
        createBackground("stevenUniverseMaskIsland", true, Rarity.UNCOMMON, LocalDate.of(2020, 5, 9), set = "stevenUniverse") {
            addDefaultVariant("steven-universe-mask-island", ContentType.Image.JPEG, Rectangle(170, 0, 1440, 1080))
        }
        createBackground("stevenUniverseSparkles", true, Rarity.RARE, LocalDate.of(2020, 5, 9), set = "stevenUniverse") {
            addDefaultVariant("steven-universe-sparkles", ContentType.Image.JPEG, Rectangle(270, 0, 1440, 1080))
        }
        createBackground("stevenUniverseFutureEnemies", true, Rarity.UNCOMMON, LocalDate.of(2020, 5, 9), set = "stevenUniverse") {
            addDefaultVariant("steven-universe-future-enemies", ContentType.Image.PNG, Rectangle(200, 0, 800, 600))
        }
        createBackground("dioBrando", true, Rarity.UNCOMMON, LocalDate.of(2020, 5, 9), set = "jojo") {
            addDefaultVariant("dio-brando", ContentType.Image.JPEG, Rectangle(0, 35, 506, 380))
        }
        createBackground("kesokaBeach", true, Rarity.UNCOMMON, LocalDate.of(0, 1, 1), createdBy = listOf("kenkuisan")) {
            addDefaultVariant("kesoka-beach", ContentType.Image.PNG)
        }
        createBackground("alanTriggered", true, Rarity.EPIC, LocalDate.of(2020, 6, 2)) {
            addDefaultVariant("alan-triggered", ContentType.Image.PNG, Rectangle(150, 0, 700, 475))
        }
        createBackground("oakmont", true, Rarity.RARE, LocalDate.of(2020, 6, 2)) {
            addDefaultVariant("oakmont", ContentType.Image.JPEG, Rectangle(0, 0, 1440, 1080))
        }
        createBackground("brandNAnimalMichiru", true, Rarity.RARE, LocalDate.of(2020, 6, 2)) {
            addDefaultVariant("bna-michiru", ContentType.Image.JPEG, Rectangle(330, 0, 1440, 1080))
        }
        createBackground("brandNAnimalCity", true, Rarity.EPIC, LocalDate.of(2020, 6, 2)) {
            addDefaultVariant("bna-animal-city", ContentType.Image.JPEG, Rectangle(250, 0, 1440, 1080))
        }
        createBackground("brandNAnimalBaseball", true, Rarity.UNCOMMON, LocalDate.of(2020, 6, 2)) {
            addDefaultVariant("bna-baseball", ContentType.Image.JPEG, Rectangle(235, 0, 1440, 1080))
        }
        createBackground("rainingForest", true, Rarity.EPIC, LocalDate.of(0, 1, 1), createdBy = listOf("kneezmz")) {
            addDefaultVariant("raining", ContentType.Image.PNG)
        }
        createBackground("laureneaBoom", true, Rarity.EPIC, LocalDate.of(2020, 6, 2), createdBy = listOf("awebnamoradadealguem")) {
            addDefaultVariant("laurenea-boom", ContentType.Image.PNG, Rectangle(0, 0, 760, 481))
        }
        createBackground("lickOfLove", true, Rarity.EPIC, LocalDate.of(2020, 6, 2), set = "jojo") {
            addDefaultVariant("lick-of-love", ContentType.Image.JPEG, Rectangle(0, 0, 600, 415))
        }
        createBackground("stoneMask", true, Rarity.UNCOMMON, LocalDate.of(2020, 6, 2), set = "jojo") {
            addDefaultVariant("stone-mask", ContentType.Image.JPEG, Rectangle(0, 0, 495, 276))
        }
        createBackground("defaultBluePantufa", true, Rarity.UNCOMMON, LocalDate.of(2020, 6, 2)) {
            addDefaultVariant("default-blue-pantufa", ContentType.Image.PNG)
        }
        createBackground("defaultGreenPantufa", true, Rarity.UNCOMMON, LocalDate.of(2020, 6, 2)) {
            addDefaultVariant("default-green-pantufa", ContentType.Image.PNG)
        }
        createBackground("defaultOrangePantufa", true, Rarity.UNCOMMON, LocalDate.of(2020, 6, 2)) {
            addDefaultVariant("default-orange-pantufa", ContentType.Image.PNG)
        }
        createBackground("defaultPinkPantufa", true, Rarity.UNCOMMON, LocalDate.of(2020, 6, 2)) {
            addDefaultVariant("default-pink-pantufa", ContentType.Image.PNG)
        }
        createBackground("defaultPurplePantufa", true, Rarity.UNCOMMON, LocalDate.of(2020, 6, 2)) {
            addDefaultVariant("default-purple-pantufa", ContentType.Image.PNG)
        }
        createBackground("defaultRedPantufa", true, Rarity.UNCOMMON, LocalDate.of(2020, 6, 2)) {
            addDefaultVariant("default-red-pantufa", ContentType.Image.PNG)
        }
        createBackground("transformiceClass", true, Rarity.RARE, LocalDate.of(2020, 7, 24)) {
            addDefaultVariant("transformice-class", ContentType.Image.JPEG, Rectangle(280, 0, 1500, 1100))
        }
        createBackground("stevenUniverseThePast", true, Rarity.RARE, LocalDate.of(2020, 7, 24)) {
            addDefaultVariant("steven-universe-the-past", ContentType.Image.PNG, Rectangle(90, 0, 950, 600))
        }
        createBackground("dollars", true, Rarity.LEGENDARY, LocalDate.of(2020, 7, 24)) {
            addDefaultVariant("dollars", ContentType.Image.JPEG, Rectangle(120, 17, 800, 600))
        }
        createBackground("reais", true, Rarity.EPIC, LocalDate.of(2020, 7, 24)) {
            addDefaultVariant("reais", ContentType.Image.JPEG, Rectangle(120, 17, 800, 600))
        }
        createBackground("loriDailyShop", true, Rarity.RARE, LocalDate.of(2020, 7, 24), createdBy = listOf("inksans")) {
            addDefaultVariant("lori-daily-shop", ContentType.Image.PNG, Rectangle(200, 0, 1520, 1080))
        }
        createBackground("wumpusSummer", true, Rarity.RARE, LocalDate.of(2020, 7, 24), set = "discord") {
            addDefaultVariant("wumpus-summer", ContentType.Image.PNG, Rectangle(160, 0, 1066, 768))
        }
        createBackground("paintToolSaiDisgraca", true, Rarity.UNCOMMON, LocalDate.of(2020, 7, 24)) {
            addDefaultVariant("paint-tool-sai-disgraca", ContentType.Image.JPEG, Rectangle(20, 0, 980, 624))
        }
        createBackground("ednaldoPereira", true, Rarity.EPIC, LocalDate.of(2020, 7, 24)) {
            addDefaultVariant("ednaldo-pereira", ContentType.Image.PNG, Rectangle(0, 50, 512, 412))
        }
        createBackground("lofiGirlDay", true, Rarity.EPIC, LocalDate.of(2020, 7, 25)) {
            addDefaultVariant("lofi-girl-day", ContentType.Image.JPEG, Rectangle(0, 123, 813, 576))
        }
        createBackground("winkingDoggo", true, Rarity.UNCOMMON, LocalDate.of(2020, 7, 25), set = "shibas") {
            addDefaultVariant("winking-doggo", ContentType.Image.JPEG, Rectangle(200, 0, 400, 300))
        }
        createBackground("brawlStarsJessieNani", true, Rarity.RARE, LocalDate.of(2020, 7, 26), set = "brawlStars") {
            addDefaultVariant("brawl-stars-jessie-nani", ContentType.Image.JPEG, Rectangle(600, 0, 2962, 2222))
        }
        createBackground("hkDarkAbyss", true, Rarity.RARE, LocalDate.of(2020, 7, 26), set = "hollowKnight") {
            addDefaultVariant("hk-dark-abyss", ContentType.Image.PNG, Rectangle(225, 0, 1381, 1036))
        }
        createBackground("stevenUniverseStarryNight", true, Rarity.UNCOMMON, LocalDate.of(2020, 7, 27), set = "stevenUniverse") {
            addDefaultVariant("steven-universe-starry-night", ContentType.Image.PNG, Rectangle(399, 0, 1320, 1018))
        }
        createBackground("transformiceCarnival", true, Rarity.RARE, LocalDate.of(2020, 7, 28), set = "transformice") {
            addDefaultVariant("transformice-carnival", ContentType.Image.JPEG, Rectangle(211, 105, 1300, 1020))
        }
        createBackground("minecraftAlbinoFox", true, Rarity.RARE, LocalDate.of(2020, 7, 30)) {
            addDefaultVariant("minecraft-albino-fox", ContentType.Image.PNG, Rectangle(228, 0, 1320, 1030))
        }
        createBackground("nyanCat", true, Rarity.EPIC, LocalDate.of(2020, 7, 30)) {
            addDefaultVariant("nyan-cat", ContentType.Image.PNG, Rectangle(0, 0, 937, 671))
        }
        createBackground("brawlStarsIsland", true, Rarity.UNCOMMON, LocalDate.of(2020, 7, 30), set = "brawlStars") {
            addDefaultVariant("brawl-stars-island", ContentType.Image.JPEG, Rectangle(164, 0, 1440, 1080))
        }
        createBackground("desertStreet", true, Rarity.UNCOMMON, LocalDate.of(2020, 7, 29)) {
            addDefaultVariant("desert-street", ContentType.Image.JPEG)
        }
        createBackground("devilMayCryNero", true, Rarity.RARE, LocalDate.of(2020, 8, 1), set = "devilMayCry") {
            addDefaultVariant("devil-may-cry-nero", ContentType.Image.JPEG, Rectangle(172, 0, 2750, 2160))
        }
        createBackground("workingTable", true, Rarity.UNCOMMON, LocalDate.of(2020, 8, 2)) {
            addDefaultVariant("working-table", ContentType.Image.PNG, Rectangle(688, 100, 1585, 1412))
        }
        createBackground("stevenUniverseEmpireCity", true, Rarity.RARE, LocalDate.of(2020, 8, 2), set = "stevenUniverse") {
            addDefaultVariant("steven-universe-empire-city", ContentType.Image.JPEG, Rectangle(165, 0, 1440, 1080))
        }
        createBackground("fluffyPudding", true, Rarity.UNCOMMON, LocalDate.of(2020, 8, 3), createdBy = listOf("allouette")) {
            addDefaultVariant("fluffy-pudding", ContentType.Image.PNG)
        }
        createBackground("stickLights", true, Rarity.UNCOMMON, LocalDate.of(2020, 8, 4)) {
            addDefaultVariant("stick-lights", ContentType.Image.PNG, Rectangle(0, 0, 560, 398))
        }
        createBackground("adventureTimeDonutsValley", true, Rarity.RARE, LocalDate.of(2020, 8, 4), set = "adventureTime") {
            addDefaultVariant("adventure-time-donuts-valley", ContentType.Image.PNG, Rectangle(0, 0, 1600, 1200))
        }
        createBackground("meAndTheBoys", true, Rarity.EPIC, LocalDate.of(2020, 8, 7)) {
            addDefaultVariant("me-and-the-boys", ContentType.Image.PNG, Rectangle(0, 0, 1600, 1200))
        }
        createBackground("gatinhoDeTouca", true, Rarity.UNCOMMON, LocalDate.of(2020, 8, 13), createdBy = listOf("469709885710270484")) {
            addDefaultVariant("gatinho-de-touca", ContentType.Image.PNG, Rectangle(0, 0, 541, 406))
        }
        createBackground("sunVaporwave", true, Rarity.RARE, LocalDate.of(2020, 8, 15)) {
            addDefaultVariant("sun-vaporwave", ContentType.Image.JPEG, Rectangle(242, 0, 1200, 1050))
        }
        createBackground("cafezinho", true, Rarity.UNCOMMON, LocalDate.of(2020, 8, 15), createdBy = listOf("polar8bits")) {
            addDefaultVariant("cafezinho", ContentType.Image.PNG)
        }
        createBackground("sorvetinho", true, Rarity.UNCOMMON, LocalDate.of(2020, 8, 15), createdBy = listOf("polar8bits")) {
            addDefaultVariant("sorvetinho", ContentType.Image.PNG)
        }
        createBackground("docinho", true, Rarity.UNCOMMON, LocalDate.of(2020, 8, 16), createdBy = listOf("polar8bits")) {
            addDefaultVariant("docinho", ContentType.Image.PNG)
        }
        createBackground("melancia", true, Rarity.UNCOMMON, LocalDate.of(2020, 8, 16), createdBy = listOf("polar8bits")) {
            addDefaultVariant("melancia", ContentType.Image.PNG)
        }
        createBackground("leagueOfLegendsPopStars", true, Rarity.RARE, LocalDate.of(2020, 8, 17), set = "leagueOfLegends") {
            addDefaultVariant("league-of-legends-pop-stars", ContentType.Image.PNG, Rectangle(0, 0, 1600, 1200))
        }
        createBackground("pizza", true, Rarity.UNCOMMON, LocalDate.of(2020, 8, 18), createdBy = listOf("polar8bits")) {
            addDefaultVariant("pizza", ContentType.Image.PNG)
        }
        createBackground("kurama", true, Rarity.RARE, LocalDate.of(2020, 8, 19), createdBy = listOf("allouette")) {
            addDefaultVariant("kurama", ContentType.Image.PNG, Rectangle(0, 0, 800, 599))
        }
        createBackground("laranjaAzeda", true, Rarity.UNCOMMON, LocalDate.of(2020, 8, 19), createdBy = listOf("polar8bits")) {
            addDefaultVariant("laranja-azeda", ContentType.Image.PNG)
        }
        createBackground("leagueOfLegendsStarGuardians", true, Rarity.RARE, LocalDate.of(2020, 8, 20), set = "leagueOfLegends") {
            addDefaultVariant("league-of-legends-star-guardians", ContentType.Image.PNG, Rectangle(0, 0, 1600, 1200))
        }
        createBackground("mcLarenSenna", true, Rarity.RARE, LocalDate.of(2020, 8, 22)) {
            addDefaultVariant("mclaren-senna", ContentType.Image.JPEG)
        }
        createBackground("justMarshmallows", true, Rarity.UNCOMMON, LocalDate.of(2020, 9, 5), createdBy = listOf("brigadeirim")) {
            addDefaultVariant("just-marshmallows", ContentType.Image.PNG, Rectangle(0, 0, 561, 421))
        }
        createBackground("bandanaNinja", true, Rarity.RARE, LocalDate.of(2020, 9, 8), createdBy = listOf("polar8bits")) {
            addDefaultVariant("bandana-ninja", ContentType.Image.PNG)
        }
        createBackground("brawlStarsVirus", true, Rarity.EPIC, LocalDate.of(2020, 9, 13), set = "brawlStars") {
            addDefaultVariant("brawl-stars-virus", ContentType.Image.PNG, Rectangle(0, 0, 1600, 1200))
        }
        createBackground("bandageHeart", true, Rarity.UNCOMMON, LocalDate.of(2020, 10, 7), createdBy = listOf("polar8bits")) {
            addDefaultVariant("bandage-heart", ContentType.Image.PNG)
        }
        createBackground("lostSouls", true, Rarity.RARE, LocalDate.of(2020, 10, 8), createdBy = listOf("zellbit")) {
            addDefaultVariant("lost-souls", ContentType.Image.PNG)
        }
        createBackground("amongUsSkyBridge", true, Rarity.RARE, LocalDate.of(2020, 10, 9), set = "amongUs") {
            addDefaultVariant("among-us-sky-bridge", ContentType.Image.PNG, Rectangle(0, 0, 1600, 1200))
        }
        createBackground("halloweenIsHere", true, Rarity.UNCOMMON, LocalDate.of(2020, 10, 17), createdBy = listOf("polar8bits")) {
            addDefaultVariant("halloween-is-here", ContentType.Image.PNG, Rectangle(0, 0, 1600, 1200))
        }
        createBackground("discordSpookyHalloween", true, Rarity.RARE, LocalDate.of(2020, 10, 31), set = "discord") {
            addDefaultVariant("discord-spooky-halloween", ContentType.Image.PNG, Rectangle(0, 0, 1600, 1200))
        }
        createBackground("porsche911Carrera", true, Rarity.RARE, LocalDate.of(0, 1, 1)) {
            addDefaultVariant("porsche-911-carrera", ContentType.Image.JPEG, Rectangle(30, 0, 636, 490))
        }
        createBackground("elisahTransformice", true, Rarity.RARE, LocalDate.of(2020, 7, 24), set = "transformice") {
            addDefaultVariant("elisah-transformice", ContentType.Image.JPEG, Rectangle(101, 0, 1440, 1080))
        }
        createBackground("lofiWarmAfternoon", true, Rarity.RARE, LocalDate.of(2020, 8, 4)) {
            addDefaultVariant("lofi-warm-afternoon", ContentType.Image.PNG, Rectangle(0, 0, 1600, 1200))
        }
        createBackground("snowBeetle", true, Rarity.RARE, LocalDate.of(2020, 8, 17)) {
            addDefaultVariant("snow-beetle", ContentType.Image.JPEG)
        }
        createBackground("nostalgicMemories", true, Rarity.RARE, LocalDate.of(2021, 11, 11)) {
            addDefaultVariant("nostalgic-memories", ContentType.Image.PNG)
            addProfileDesignGroupVariant(CENTER_TOP_FOCUS_DESIGN, "nostalgic-memories-center-top", ContentType.Image.PNG)
        }
        createBackground("imitandoEmojis", true, Rarity.UNCOMMON, LocalDate.of(2021, 11, 11)) {
            addDefaultVariant("imitando-emojis", ContentType.Image.JPEG)
        }
        createBackground("iHateLoveLoriLight", true, Rarity.UNCOMMON, LocalDate.of(2021, 11, 11)) {
            addDefaultVariant("i-hate-love-lori-light", ContentType.Image.PNG)
        }
        createBackground("iHateLoveLoriDark", true, Rarity.UNCOMMON, LocalDate.of(2021, 11, 11)) {
            addDefaultVariant("i-hate-love-lori-dark", ContentType.Image.PNG)
        }
        createBackground("powerBannerDiscordAd", true, Rarity.UNCOMMON, LocalDate.of(2021, 11, 11)) {
            addDefaultVariant("powerbanner-discord-ad", ContentType.Image.PNG)
        }
        createBackground("powerBannerPalmtreePanic", true, Rarity.RARE, LocalDate.of(2021, 11, 11)) {
            addDefaultVariant("powerbanner-palmtree-panic", ContentType.Image.PNG)
        }
        createBackground("powerBannerTheSims", true, Rarity.UNCOMMON, LocalDate.of(2021, 11, 11)) {
            addDefaultVariant("powerbanner-the-sims", ContentType.Image.PNG)
        }
        createBackground("powerBannerInsideComputer", true, Rarity.RARE, LocalDate.of(2021, 11, 11)) {
            addDefaultVariant("powerbanner-inside-computer", ContentType.Image.PNG)
        }
        createBackground("powerBannerInsideComputer2", true, Rarity.RARE, LocalDate.of(2021, 11, 11)) {
            addDefaultVariant("powerbanner-inside-computer2", ContentType.Image.PNG)
        }
        createBackground("badAppleReimuApple", true, Rarity.EPIC, LocalDate.of(2021, 11, 11)) {
            addDefaultVariant("bad-apple-reimu-apple", ContentType.Image.PNG)
        }
        createBackground("badAppleMarisaApple", true, Rarity.EPIC, LocalDate.of(2021, 11, 11)) {
            addDefaultVariant("bad-apple-marisa-apple", ContentType.Image.PNG)
            addProfileDesignGroupVariant(CENTER_TOP_FOCUS_DESIGN, "bad-apple-marisa-apple-center-top", ContentType.Image.PNG)
        }
        createBackground("metaforandoNamoradoDelaAqui", true, Rarity.EPIC, LocalDate.of(2021, 11, 11)) {
            addDefaultVariant("metaforando-namorado-dela-aqui", ContentType.Image.JPEG)
            addProfileDesignGroupVariant(CENTER_TOP_FOCUS_DESIGN, "metaforando-namorado-dela-aqui-center-top", ContentType.Image.JPEG)
        }
        createBackground("abacat", true, Rarity.UNCOMMON, LocalDate.of(2021, 11, 11)) {
            addDefaultVariant("abacat", ContentType.Image.PNG)
            addProfileDesignGroupVariant(CENTER_TOP_FOCUS_DESIGN, "abacat-center-top", ContentType.Image.JPEG)
        }
        createBackground("moPaz", true, Rarity.RARE, LocalDate.of(2021, 11, 11)) {
            addDefaultVariant("mo-paz", ContentType.Image.JPEG)
            addProfileDesignGroupVariant(CENTER_TOP_FOCUS_DESIGN, "mo-paz-center-top", ContentType.Image.JPEG)
        }
        createBackground("bolsolula", true, Rarity.LEGENDARY, LocalDate.of(2021, 11, 11)) {
            addDefaultVariant("bolsolula", ContentType.Image.PNG)
            addProfileDesignGroupVariant(CENTER_TOP_FOCUS_DESIGN, "bolsolula-center-top", ContentType.Image.PNG)
        }
        createBackground("locodolYukariWhat", true, Rarity.RARE, LocalDate.of(2021, 11, 11)) {
            addDefaultVariant("locodol-yukari-what", ContentType.Image.PNG)
        }
        createBackground("loralsei", true, Rarity.EPIC, LocalDate.of(2021, 11, 11), createdBy = listOf("inksans")) {
            addDefaultVariant("loralsei", ContentType.Image.PNG)
        }
        createBackground("viniccius13CasaAutomatica", true, Rarity.RARE, LocalDate.of(2021, 11, 11)) {
            addDefaultVariant("viniccius13-casa-automatica", ContentType.Image.PNG)
        }
        createBackground("lorittaCodeDark", true, Rarity.UNCOMMON, LocalDate.of(2021, 11, 11)) {
            addDefaultVariant("loritta-code-dark", ContentType.Image.PNG)
        }
        createBackground("loriSketchSprites", true, Rarity.UNCOMMON, LocalDate.of(2021, 11, 11), createdBy = listOf("its_gabi")) {
            addDefaultVariant("lori-sketch-sprites", ContentType.Image.PNG)
        }
        createBackground("gamblingLifeLight", true, Rarity.RARE, LocalDate.of(2021, 11, 11)) {
            addDefaultVariant("gambling-life-light", ContentType.Image.PNG)
            addProfileDesignGroupVariant(CENTER_TOP_FOCUS_DESIGN, "gambling-life-light-center-top", ContentType.Image.PNG)
        }
        createBackground("gamblingLifeDark", true, Rarity.RARE, LocalDate.of(2021, 11, 11)) {
            addDefaultVariant("gambling-life-dark", ContentType.Image.PNG)
            addProfileDesignGroupVariant(CENTER_TOP_FOCUS_DESIGN, "gambling-life-dark-center-top", ContentType.Image.PNG)
        }
        createBackground("wumpusBuffDark", true, Rarity.RARE, LocalDate.of(2021, 11, 11), createdBy = listOf("peterstark000"), set = "discord") {
            addDefaultVariant("wumpus-buff-dark", ContentType.Image.PNG)
            addProfileDesignGroupVariant(CENTER_TOP_FOCUS_DESIGN, "wumpus-buff-dark-center-top", ContentType.Image.PNG)
        }
        createBackground("wumpusBuffBlurple", true, Rarity.RARE, LocalDate.of(2021, 11, 11), createdBy = listOf("peterstark000"), set = "discord") {
            addDefaultVariant("wumpus-buff-blurple", ContentType.Image.PNG)
            addProfileDesignGroupVariant(CENTER_TOP_FOCUS_DESIGN, "wumpus-buff-blurple-center-top", ContentType.Image.PNG)
        }
        createBackground("wumpusMania", true, Rarity.EPIC, LocalDate.of(2021, 11, 11), set = "discord") {
            addDefaultVariant("wumpus-mania", ContentType.Image.PNG)
        }
        createBackground("fridayLoriFunkin", true, Rarity.EPIC, LocalDate.of(2021, 12, 8)) {
            addDefaultVariant("friday-lori-funkin", ContentType.Image.PNG)
            addProfileDesignGroupVariant(CENTER_TOP_FOCUS_DESIGN, "friday-lori-funkin-center-top", ContentType.Image.PNG)
        }
        createBackground("circuitBoardGreen", true, Rarity.UNCOMMON, LocalDate.of(2021, 12, 8)) {
            addDefaultVariant("circuit-board-green", ContentType.Image.PNG)
        }
        createBackground("redditRplace1", true, Rarity.EPIC, LocalDate.of(2021, 12, 8)) {
            addDefaultVariant("reddit-rplace-1", ContentType.Image.PNG)
        }
        createBackground("redditRplace2", true, Rarity.EPIC, LocalDate.of(2021, 12, 8)) {
            addDefaultVariant("reddit-rplace-2", ContentType.Image.PNG)
        }
        createBackground("sonicMovie2Poster", true, Rarity.EPIC, LocalDate.of(2021, 12, 8), set = "sonic") {
            addDefaultVariant("sonic-movie-2-poster", ContentType.Image.PNG)
        }
        createBackground("ehMoleAnime", true, Rarity.EPIC, LocalDate.of(2022, 12, 19)) {
            addDefaultVariant("eh-mole-anime", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("soritta", true, Rarity.EPIC, LocalDate.of(2022, 12, 19)) {
            addDefaultVariant("soritta", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("happyCatClown", true, Rarity.RARE, LocalDate.of(2022, 12, 19)) {
            addDefaultVariant("happy-cat-clown", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("sparklyNightPurple", true, Rarity.RARE, LocalDate.of(2022, 12, 19)) {
            addDefaultVariant("sparkly-night-purple", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("puddingAnime", true, Rarity.RARE, LocalDate.of(2022, 12, 19)) {
            addDefaultVariant("pudding-anime", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("feijoadaAnime", true, Rarity.RARE, LocalDate.of(2022, 12, 19)) {
            addDefaultVariant("feijoada-anime", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("loriDbz", true, Rarity.EPIC, LocalDate.of(2022, 12, 19)) {
            addDefaultVariant("lori-dbz", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("arthCute", true, Rarity.UNCOMMON, LocalDate.of(2022, 12, 19)) {
            addDefaultVariant("arth-cute", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("lorittaTheDog", true, Rarity.EPIC, LocalDate.of(2022, 12, 20)) {
            addDefaultVariant("loritta-the-dog", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("starrySpace", true, Rarity.UNCOMMON, LocalDate.of(2022, 12, 22)) {
            addDefaultVariant("starry-space", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("threeCuteCats", true, Rarity.UNCOMMON, LocalDate.of(2022, 12, 22)) {
            addDefaultVariant("three-cute-cats", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("astronautANewDay", true, Rarity.EPIC, LocalDate.of(2022, 12, 22)) {
            addDefaultVariant("astronaut-a-new-day", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("lorittaDollMicrophone", true, Rarity.RARE, LocalDate.of(2022, 12, 22)) {
            addDefaultVariant("loritta-doll-microphone", ContentType.Image.JPEG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("lorittaDollWumpus", true, Rarity.RARE, LocalDate.of(2022, 12, 22)) {
            addDefaultVariant("loritta-doll-wumpus", ContentType.Image.JPEG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("bahiaBeachCahy", true, Rarity.RARE, LocalDate.of(2022, 12, 22)) {
            addDefaultVariant("beach-bahia-cahy", ContentType.Image.JPEG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("bahiaBeachJaparaMirim", true, Rarity.RARE, LocalDate.of(2022, 12, 22)) {
            addDefaultVariant("beach-bahia-japara-mirim", ContentType.Image.JPEG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("bahiaDuck", true, Rarity.UNCOMMON, LocalDate.of(2022, 12, 22)) {
            addDefaultVariant("bahia-duck", ContentType.Image.JPEG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("bahiaYardGrass", true, Rarity.UNCOMMON, LocalDate.of(2022, 12, 22)) {
            addDefaultVariant("bahia-yard-grass", ContentType.Image.JPEG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("bahiaYardLake", true, Rarity.RARE, LocalDate.of(2022, 12, 22)) {
            addDefaultVariant("bahia-yard-lake", ContentType.Image.JPEG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("lorittaPantufaJojo", true, Rarity.EPIC, LocalDate.of(2022, 12, 22), createdBy = listOf("sortrosphoresia")) {
            addDefaultVariant("loritta-pantufa-jojo", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
            addProfileDesignGroupVariant(CENTER_TOP_FOCUS_DESIGN, "loritta-pantufa-jojo-center-top", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("lorittaTearCode", true, Rarity.EPIC, LocalDate.of(2022, 12, 22), createdBy = listOf("sortrosphoresia")) {
            addDefaultVariant("loritta-tear-code", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
            addProfileDesignGroupVariant(CENTER_TOP_FOCUS_DESIGN, "loritta-tear-code-center-top", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("catLaptopCrayon", true, Rarity.RARE, LocalDate.of(2022, 12, 22)) {
            addDefaultVariant("cat-laptop-crayon", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
            addProfileDesignGroupVariant(CENTER_TOP_FOCUS_DESIGN, "cat-laptop-crayon-center-top", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("bedroomNights", true, Rarity.RARE, LocalDate.of(2022, 12, 22)) {
            addDefaultVariant("bedroom-nights", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("jumpingRabbitCute", true, Rarity.UNCOMMON, LocalDate.of(2022, 12, 22)) {
            addDefaultVariant("jumping-rabbit-cute", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("bpLovesickGirls", true, Rarity.EPIC, LocalDate.of(2022, 12, 22)) {
            addDefaultVariant("bp-lovesick-girls", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("bpLovesickGirls", true, Rarity.EPIC, LocalDate.of(2022, 12, 22)) {
            addDefaultVariant("bp-lovesick-girls", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("bpDduDduDdu", true, Rarity.EPIC, LocalDate.of(2022, 12, 22)) {
            addDefaultVariant("bp-ddu-ddu-ddu", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("bpShutDown", true, Rarity.EPIC, LocalDate.of(2022, 12, 22)) {
            addDefaultVariant("bp-shut-down", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("chuuLoona", true, Rarity.EPIC, LocalDate.of(2022, 12, 22)) {
            addDefaultVariant("chuu-loona", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("caracalHamburgerCute", true, Rarity.RARE, LocalDate.of(2022, 12, 22)) {
            addDefaultVariant("caracal-hamburger-cute", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("easter2023Loritta", true, Rarity.LEGENDARY, LocalDate.of(2023, 4, 15), availableToBuyViaSonhos = false) {
            addDefaultVariant("easter2023-loritta", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("lorittaAndTheDreamers", true, Rarity.EPIC, LocalDate.of(2024, 3, 13)) {
            addDefaultVariant("loritta-and-the-dreamers", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
            addProfileDesignGroupVariant(CENTER_TOP_FOCUS_DESIGN, "loritta-and-the-dreamers-center-top", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("lorittaAndTheDreamersTiled", true, Rarity.EPIC, LocalDate.of(2024, 3, 13)) {
            addDefaultVariant("loritta-and-the-dreamers-tiled", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("lorittaBliss", true, Rarity.EPIC, LocalDate.of(2024, 3, 13)) {
            addDefaultVariant("loritta-bliss", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("lorittaMoonBeach", true, Rarity.EPIC, LocalDate.of(2024, 3, 13)) {
            addDefaultVariant("loritta-moon-beach", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
            addProfileDesignGroupVariant(CENTER_TOP_FOCUS_DESIGN, "loritta-moon-beach-center-top", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("lorittaYouBringLightIn", true, Rarity.EPIC, LocalDate.of(2024, 3, 13)) {
            addDefaultVariant("loritta-you-bring-light-in", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
            addProfileDesignGroupVariant(CENTER_TOP_FOCUS_DESIGN, "loritta-you-bring-light-in-center-top", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("lorittaCoolPose", true, Rarity.EPIC, LocalDate.of(2024, 3, 13)) {
            addDefaultVariant("loritta-cool-pose", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("powerRandomSketches", true, Rarity.UNCOMMON, LocalDate.of(2024, 3, 13)) {
            addDefaultVariant("power-random-sketches", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("captainBoykisser", true, Rarity.EPIC, LocalDate.of(2024, 3, 13)) {
            addDefaultVariant("captain-boykisser", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
            addProfileDesignGroupVariant(CENTER_TOP_FOCUS_DESIGN, "captain-boykisser-center-top", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("lorittaHeyHeyMyMyYoYo", true, Rarity.EPIC, LocalDate.of(2024, 3, 20)) {
            addDefaultVariant("loritta-hey-hey-my-my-yo-yo", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
            addProfileDesignGroupVariant(CENTER_TOP_FOCUS_DESIGN, "loritta-hey-hey-my-my-yo-yo-center-top", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("lorittaHalloweenBecheshire", true, Rarity.EPIC, LocalDate.of(2024, 11, 7), createdBy = listOf("begames")) {
            addDefaultVariant("loritta-halloween-becheshire", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("lorittaHalloweenMonsterHighPandastica", true, Rarity.EPIC, LocalDate.of(2024, 11, 7), createdBy = listOf("597259977803169835")) {
            addDefaultVariant("loritta-halloween-monster-high-pandastica", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("lorittaHalloweenQualquerCoisa", true, Rarity.EPIC, LocalDate.of(2024, 11, 7), createdBy = listOf("kouhay")) {
            addDefaultVariant("loritta-halloween-qualquercoisa", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("lorittaHalloweenBibbosz", true, Rarity.RARE, LocalDate.of(2024, 11, 7), createdBy = listOf("aressz")) {
            addDefaultVariant("loritta-halloween-bibbosz", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("sonicVsShadowSonicMovie3", true, Rarity.EPIC, LocalDate.of(2024, 11, 7), set = "sonic") {
            addDefaultVariant("sonic-vs-shadow-sonic-movie-3", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("shadowBikeSkidSonicMovie3", true, Rarity.EPIC, LocalDate.of(2024, 11, 7), set = "sonic") {
            addDefaultVariant("shadow-bike-skid-sonic-movie-3", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("mariaRobotnikDarkBeginnings", true, Rarity.EPIC, LocalDate.of(2024, 11, 7), set = "sonic") {
            addDefaultVariant("maria-robotnik-dark-beginnings", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("shadowAndMariaDarkBeginnings", true, Rarity.EPIC, LocalDate.of(2024, 11, 7), set = "sonic") {
            addDefaultVariant("shadow-and-maria-dark-beginnings", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("lorittaHalloweenYafyr", true, Rarity.EPIC, LocalDate.of(2024, 11, 7), createdBy = listOf("yafyr")) {
            addDefaultVariant("loritta-halloween-yafyr", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("crocodileClothingBlue", true, Rarity.EPIC, LocalDate.of(2024, 11, 7)) {
            addDefaultVariant("crocodile-clothing-blue", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
            addProfileDesignGroupVariant(CENTER_TOP_FOCUS_DESIGN, "crocodile-clothing-blue-center-top", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("crocodileClothingRed", true, Rarity.EPIC, LocalDate.of(2024, 11, 7)) {
            addDefaultVariant("crocodile-clothing-red", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
            addProfileDesignGroupVariant(CENTER_TOP_FOCUS_DESIGN, "crocodile-clothing-red-center-top", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("crocodileClothingGreen", true, Rarity.EPIC, LocalDate.of(2024, 11, 7)) {
            addDefaultVariant("crocodile-clothing-green", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
            addProfileDesignGroupVariant(CENTER_TOP_FOCUS_DESIGN, "crocodile-clothing-green-center-top", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("crocodileClothingOrange", true, Rarity.EPIC, LocalDate.of(2024, 11, 7)) {
            addDefaultVariant("crocodile-clothing-orange", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
            addProfileDesignGroupVariant(CENTER_TOP_FOCUS_DESIGN, "crocodile-clothing-orange-center-top", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("crocodileClothingPink", true, Rarity.EPIC, LocalDate.of(2024, 11, 7)) {
            addDefaultVariant("crocodile-clothing-pink", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
            addProfileDesignGroupVariant(CENTER_TOP_FOCUS_DESIGN, "crocodile-clothing-pink-center-top", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("crocodileClothingPurple", true, Rarity.EPIC, LocalDate.of(2024, 11, 7)) {
            addDefaultVariant("crocodile-clothing-purple", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
            addProfileDesignGroupVariant(CENTER_TOP_FOCUS_DESIGN, "crocodile-clothing-purple-center-top", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("myDeerFriendNokotanYouDeer", true, Rarity.EPIC, LocalDate.of(2024, 11, 7)) {
            addDefaultVariant("my-deer-friend-nokotan-you-deer", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("luckyStarChocolateCornet", true, Rarity.EPIC, LocalDate.of(2024, 11, 7), set = "luckyStar") {
            addDefaultVariant("lucky-star-chocolate-cornet", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("luckyStarGirls", true, Rarity.EPIC, LocalDate.of(2024, 11, 7), set = "luckyStar") {
            addDefaultVariant("lucky-star-girls", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("luckyStarClothes", true, Rarity.RARE, LocalDate.of(2024, 11, 7), set = "luckyStar") {
            addDefaultVariant("lucky-star-clothes", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
        createBackground("lorittaHalloweenBoloDeCaco", true, Rarity.EPIC, LocalDate.of(2024, 11, 7), createdBy = listOf("1234564295115935817")) {
            addDefaultVariant("loritta-halloween-bolodecaco", ContentType.Image.PNG, storageType = BackgroundStorageType.ETHEREAL_GAMBI)
        }
    }

    private fun createProfileDesign(
        id: String,
        enabled: Boolean,
        rarity: Rarity,
        addedAt: LocalDate,
        set: String? = null,
        createdBy: List<String> = listOf(),
        availableToBuyViaSonhos: Boolean = true,
        availableToBuyViaMoney: Boolean = false
    ) {
        ProfileDesigns.upsert(ProfileDesigns.id) {
            it[ProfileDesigns.id] = id
            it[ProfileDesigns.enabled] = enabled
            it[ProfileDesigns.rarity] = rarity
            if (set != null)
                it[ProfileDesigns.set] = EntityID(set, Sets)
            it[ProfileDesigns.createdBy] = createdBy
            it[ProfileDesigns.availableToBuyViaDreams] = availableToBuyViaSonhos
            it[ProfileDesigns.availableToBuyViaMoney] = availableToBuyViaMoney
            it[ProfileDesigns.addedAt] = addedAt.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        }
    }

    private fun createBackground(
        id: String,
        enabled: Boolean,
        rarity: Rarity,
        addedAt: LocalDate,
        set: String? = null,
        createdBy: List<String> = listOf(),
        availableToBuyViaSonhos: Boolean = true,
        availableToBuyViaMoney: Boolean = false,
        variantBuilder: VariantBuilder.() -> (Unit) = {}
    ) {
        Backgrounds.upsert(Backgrounds.id) {
            it[Backgrounds.id] = id
            it[Backgrounds.enabled] = enabled
            it[Backgrounds.rarity] = rarity
            if (set != null)
                it[Backgrounds.set] = EntityID(set, Sets)
            it[Backgrounds.createdBy] = createdBy
            it[Backgrounds.availableToBuyViaDreams] = availableToBuyViaSonhos
            it[Backgrounds.availableToBuyViaMoney] = availableToBuyViaMoney
            it[Backgrounds.addedAt] = addedAt.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        }

        variantBuilder.invoke(VariantBuilder(id))
    }

    class VariantBuilder(private val backgroundInternalName: String) {
        // Upsert sadly does not work for us here, because upsert does not work with a column that has null values
        // So we will need to check ourselves
        fun addDefaultVariant(file: String, preferredMediaType: ContentType, crop: Rectangle? = null, storageType: BackgroundStorageType = BackgroundStorageType.DREAM_STORAGE_SERVICE) {
            val firstResult = BackgroundVariations.selectFirstOrNull { BackgroundVariations.background eq backgroundInternalName and BackgroundVariations.profileDesignGroup.isNull() }
            if (firstResult != null) {
                BackgroundVariations.update({ BackgroundVariations.id eq firstResult[BackgroundVariations.id] }) {
                    it[BackgroundVariations.file] = file
                    it[BackgroundVariations.preferredMediaType] = preferredMediaType.toString()
                    if (crop != null)
                        it[BackgroundVariations.crop] = Json.encodeToString(crop)
                    it[BackgroundVariations.storageType] = storageType
                }
            } else {
                BackgroundVariations.insert {
                    it[BackgroundVariations.background] = backgroundInternalName
                    it[BackgroundVariations.file] = file
                    it[BackgroundVariations.preferredMediaType] = preferredMediaType.toString()
                    if (crop != null)
                        it[BackgroundVariations.crop] = Json.encodeToString(crop)
                    it[BackgroundVariations.storageType] = storageType
                }
            }
        }

        fun addProfileDesignGroupVariant(profileGroupId: UUID, file: String, preferredMediaType: ContentType, crop: Rectangle? = null, storageType: BackgroundStorageType = BackgroundStorageType.DREAM_STORAGE_SERVICE) {
            val firstResult = BackgroundVariations.selectFirstOrNull { BackgroundVariations.background eq backgroundInternalName and (BackgroundVariations.profileDesignGroup eq profileGroupId) }
            if (firstResult != null) {
                BackgroundVariations.update({ BackgroundVariations.id eq firstResult[BackgroundVariations.id] }) {
                    it[BackgroundVariations.file] = file
                    it[BackgroundVariations.preferredMediaType] = preferredMediaType.toString()
                    if (crop != null)
                        it[BackgroundVariations.crop] = Json.encodeToString(crop)
                    it[BackgroundVariations.storageType] = storageType
                }
            } else {
                BackgroundVariations.insert {
                    it[BackgroundVariations.background] = backgroundInternalName
                    it[BackgroundVariations.file] = file
                    it[BackgroundVariations.preferredMediaType] = preferredMediaType.toString()
                    it[BackgroundVariations.profileDesignGroup] = EntityID(profileGroupId, ProfileDesignGroups)
                    if (crop != null)
                        it[BackgroundVariations.crop] = Json.encodeToString(crop)
                    it[BackgroundVariations.storageType] = storageType
                }
            }
        }
    }
}