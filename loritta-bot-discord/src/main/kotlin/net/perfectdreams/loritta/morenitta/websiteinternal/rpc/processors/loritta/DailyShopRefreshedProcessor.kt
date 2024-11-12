package net.perfectdreams.loritta.morenitta.websiteinternal.rpc.processors.loritta

import dev.minn.jda.ktx.messages.MessageCreate
import io.ktor.server.application.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.TimeFormat
import net.perfectdreams.galleryofdreams.common.data.DiscordSocialConnection
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.services.fromRow
import net.perfectdreams.loritta.cinnamon.pudding.tables.Backgrounds
import net.perfectdreams.loritta.cinnamon.pudding.tables.DailyProfileShopItems
import net.perfectdreams.loritta.cinnamon.pudding.tables.DailyShopItems
import net.perfectdreams.loritta.cinnamon.pudding.tables.ProfileDesigns
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.LorittaDailyShopNotificationsConfigs
import net.perfectdreams.loritta.common.utils.Color
import net.perfectdreams.loritta.common.utils.Rarity
import net.perfectdreams.loritta.common.utils.placeholders.DailyShopTrinketsNotificationMessagePlaceholders
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.profile.ProfileDesignManager
import net.perfectdreams.loritta.morenitta.utils.ImageFormat
import net.perfectdreams.loritta.morenitta.utils.MessageUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.utils.extensions.getIconUrl
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.morenitta.websiteinternal.rpc.processors.LorittaInternalRpcProcessor
import net.perfectdreams.loritta.serializable.*
import net.perfectdreams.loritta.serializable.internal.requests.LorittaInternalRPCRequest
import net.perfectdreams.loritta.serializable.internal.responses.LorittaInternalRPCResponse
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class DailyShopRefreshedProcessor(val loritta: LorittaBot) : LorittaInternalRpcProcessor<LorittaInternalRPCRequest.DailyShopRefreshedRequest, LorittaInternalRPCResponse.DailyShopRefreshedResponse> {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun process(
        call: ApplicationCall,
        request: LorittaInternalRPCRequest.DailyShopRefreshedRequest
    ): LorittaInternalRPCResponse.DailyShopRefreshedResponse {
        val dailyShopId = request.dailyShopId
        logger.info { "Received information that the daily shop was refreshed! dailyShopId: $dailyShopId" }

        val result = loritta.transaction {
            val backgrounds = DailyShopItems
                .innerJoin(Backgrounds)
                .selectAll()
                .where {
                    DailyShopItems.shop eq dailyShopId
                }
                .toList()
                .map {
                    DailyShopBackgroundEntry(
                        BackgroundWithVariations(
                            Background.fromRow(it),
                            loritta.pudding.backgrounds.getBackgroundVariations(it[Backgrounds.internalName])
                        ),
                        it[DailyShopItems.tag]
                    )
                }

            val profileDesigns = DailyProfileShopItems
                .innerJoin(ProfileDesigns)
                .selectAll()
                .where {
                    DailyProfileShopItems.shop eq dailyShopId
                }
                .toList()
                .map {
                    WebsiteUtils.fromProfileDesignToSerializable(loritta, it).also { profile ->
                        profile.tag = it[DailyProfileShopItems.tag]
                    }
                }

            val configs = LorittaDailyShopNotificationsConfigs.innerJoin(ServerConfigs, { ServerConfigs.id }, { LorittaDailyShopNotificationsConfigs.id })
                .selectAll()
                .where {
                    LorittaDailyShopNotificationsConfigs.notifyNewTrinkets eq true or (LorittaDailyShopNotificationsConfigs.notifyShopTrinkets eq true)
                }
                .toList()

            Result(backgrounds, profileDesigns, configs)
        }

        for (config in result.configs) {
            val guild = loritta.lorittaShards.getGuildById(config[ServerConfigs.id].value) ?: continue
            try {
                val shopTrinketsChannelId = config[LorittaDailyShopNotificationsConfigs.shopTrinketsChannelId]
                val newTrinketsChannelId = config[LorittaDailyShopNotificationsConfigs.newTrinketsChannelId]

                val i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(config[ServerConfigs.localeId])
                val legacyBaseLocale = loritta.localeManager.getLocaleById(config[ServerConfigs.localeId])
                val selfMemberAsProfileUserInfoData = loritta.profileDesignManager.transformUserToProfileUserInfoData(guild.selfMember.user)

                suspend fun sendDailyShopTrinketsNotification(
                    channel: GuildMessageChannel,
                    message: String?,
                    shopItems: List<ShopItemWrapper>,
                    medium: String,
                ) {
                    channel.sendMessage(
                        MessageUtils.generateMessageOrFallbackIfInvalid(
                            i18nContext,
                            message ?: "",
                            guild,
                            DailyShopTrinketsNotificationMessagePlaceholders,
                            {
                                when (it) {
                                    DailyShopTrinketsNotificationMessagePlaceholders.DailyShopDateShortPlaceholder -> TimeFormat.DATE_SHORT.format(
                                        Instant.now()
                                    )

                                    DailyShopTrinketsNotificationMessagePlaceholders.GuildNamePlaceholder -> guild.name
                                    DailyShopTrinketsNotificationMessagePlaceholders.GuildSizePlaceholder -> guild.memberCount.toString()
                                    DailyShopTrinketsNotificationMessagePlaceholders.GuildIconUrlPlaceholder -> guild.getIconUrl(
                                        512,
                                        ImageFormat.PNG
                                    ) ?: ""
                                }
                            },
                            I18nKeysData.InvalidMessages.DailyShopTrinketsNotification,
                        )
                    ).await()

                    val chunkedItems = shopItems
                        .sortedWith(compareByDescending(ShopItemWrapper::rarity).thenBy(ShopItemWrapper::internalName))
                        .chunked(Message.MAX_EMBED_COUNT)

                    for ((index, itemChunk) in chunkedItems.withIndex()) {
                        val isLast = index == chunkedItems.size - 1

                        channel.sendMessage(
                            MessageCreate {
                                for (item in itemChunk) {
                                    embed {
                                        val tag = item.tag
                                        title = buildString {
                                            if (tag != null) {
                                                append("[${legacyBaseLocale[tag].uppercase()}] ")
                                            }

                                            append(legacyBaseLocale[item.nameKey])
                                        }
                                        description = legacyBaseLocale[item.descriptionKey]
                                        field(
                                            i18nContext.get(I18nKeysData.DailyShopTrinketsRelayer.Rarity),
                                            when (item.rarity) {
                                                Rarity.COMMON -> i18nContext.get(I18nKeysData.DailyShopTrinketsRelayer.RaritiesWithSonhos.Common(item.price))
                                                Rarity.UNCOMMON -> i18nContext.get(I18nKeysData.DailyShopTrinketsRelayer.RaritiesWithSonhos.Uncommon(item.price))
                                                Rarity.RARE -> i18nContext.get(I18nKeysData.DailyShopTrinketsRelayer.RaritiesWithSonhos.Rare(item.price))
                                                Rarity.EPIC -> i18nContext.get(I18nKeysData.DailyShopTrinketsRelayer.RaritiesWithSonhos.Epic(item.price))
                                                Rarity.LEGENDARY -> i18nContext.get(I18nKeysData.DailyShopTrinketsRelayer.RaritiesWithSonhos.Legendary(item.price))
                                            }
                                        )
                                        if (item.set != null) {
                                            field(
                                                i18nContext.get(I18nKeysData.DailyShopTrinketsRelayer.Set),
                                                legacyBaseLocale["sets.${item.set}"]
                                            )
                                        }

                                        val createdBy = item.createdBy
                                        if (createdBy.isNotEmpty()) {
                                            val artists = loritta.cachedGalleryOfDreamsDataResponse!!.artists
                                                .filter { it.slug in createdBy }

                                            if (artists.isNotEmpty()) {
                                                field(
                                                    i18nContext.get(I18nKeysData.DailyShopTrinketsRelayer.Creator),
                                                    buildString {
                                                        var isFirst = true
                                                        for (artist in artists) {
                                                            if (!isFirst)
                                                                append(", ")
                                                            append(artist.name)
                                                            val discord =
                                                                artist.socialConnections.filterIsInstance<DiscordSocialConnection>()
                                                                    .firstOrNull()
                                                            if (discord != null) {
                                                                append(" (`${discord.id}`)")
                                                            }
                                                            isFirst = false
                                                        }
                                                    }
                                                )
                                            }
                                        }

                                        color = when (item.rarity) {
                                            Rarity.COMMON -> Color.fromHex("#e7e7e7").rgb
                                            Rarity.UNCOMMON -> Color.fromHex("#2cff00").rgb
                                            Rarity.RARE -> Color.fromHex("#009fff").rgb
                                            Rarity.EPIC -> Color.fromHex("#b03cff").rgb
                                            Rarity.LEGENDARY -> Color.fromHex("#fadf4b").rgb
                                        }

                                        image = when (item) {
                                            is BackgroundItemWrapper -> item.backgroundUrl
                                            is ProfileDesignItemWrapper -> "attachment://profile-${item.internalName}.${item.profileResult.imageFormat.extension}"
                                        }
                                    }

                                    if (item is ProfileDesignItemWrapper) {
                                        files += FileUpload.fromData(
                                            item.profileResult.image,
                                            "profile-${item.internalName}.${item.profileResult.imageFormat.extension}"
                                        )
                                    }
                                }

                                if (isLast) {
                                    actionRow(
                                        Button.of(
                                            ButtonStyle.LINK,
                                            "${loritta.config.loritta.website.url}dashboard/daily-shop?utm_source=discord&utm_medium=$medium&utm_campaign=daily-item-shop&utm_content=guild-${guild.idLong}",
                                            i18nContext.get(I18nKeysData.Commands.Command.Profileview.LorittaDailyItemShop),
                                            Emotes.ShoppingBags.toJDA()
                                        )
                                    )
                                }
                            }
                        ).await()
                    }
                }

                if (shopTrinketsChannelId != null) {
                    val channel = guild.getGuildMessageChannelById(shopTrinketsChannelId)

                    if (channel != null && channel.canTalk()) {
                        GlobalScope.launch {
                            try {
                                // This has "guild-specific" items (like the profile designs)
                                val shopItems = mutableListOf<ShopItemWrapper>()

                                for (item in result.profileDesigns) {
                                    val profileResult = loritta.profileDesignManager.createProfile(
                                        loritta,
                                        i18nContext,
                                        legacyBaseLocale,
                                        selfMemberAsProfileUserInfoData,
                                        selfMemberAsProfileUserInfoData,
                                        guild.let { loritta.profileDesignManager.transformGuildToProfileGuildInfoData(it) },
                                        loritta.profileDesignManager.designs.first {
                                            it.internalName == item.internalName
                                        }
                                    )

                                    shopItems.add(ProfileDesignItemWrapper(item, profileResult))
                                }

                                for (item in result.backgrounds) {
                                    val dssNamespace = loritta.dreamStorageService.getCachedNamespaceOrRetrieve()
                                    val backgroundWithVariations = item.backgroundWithVariations

                                    val variation =
                                        backgroundWithVariations.variations.filterIsInstance<DefaultBackgroundVariation>()
                                            .first()

                                    val backgroundUrl = when (variation.storageType) {
                                        BackgroundStorageType.DREAM_STORAGE_SERVICE -> loritta.profileDesignManager.getDreamStorageServiceBackgroundUrlWithCropParameters(
                                            loritta.config.loritta.dreamStorageService.url,
                                            dssNamespace,
                                            variation
                                        )

                                        BackgroundStorageType.ETHEREAL_GAMBI -> loritta.profileDesignManager.getEtherealGambiBackgroundUrl(
                                            variation
                                        )
                                    }

                                    shopItems.add(BackgroundItemWrapper(item, backgroundUrl))
                                }

                                if (shopItems.isNotEmpty()) {
                                    sendDailyShopTrinketsNotification(
                                        channel,
                                        config[LorittaDailyShopNotificationsConfigs.shopTrinketsMessage],
                                        shopItems,
                                        "daily-shop-refresh"
                                    )
                                }
                            } catch (e: Exception) {
                                logger.warn(e) { "Something went wrong while trying to send daily shop trinkets notifications to ${guild.idLong}!" }
                            }
                        }
                    }
                }

                if (newTrinketsChannelId != null) {
                    val channel = guild.getGuildMessageChannelById(newTrinketsChannelId)

                    if (channel != null && channel.canTalk()) {
                        GlobalScope.launch {
                            try {
                                // This has "guild-specific" items (like the profile designs)
                                val shopItems = mutableListOf<ShopItemWrapper>()

                                for (item in result.profileDesigns.filter { it.tag == "website.dailyShop.new" }) {
                                    val profileResult = loritta.profileDesignManager.createProfile(
                                        loritta,
                                        i18nContext,
                                        legacyBaseLocale,
                                        selfMemberAsProfileUserInfoData,
                                        selfMemberAsProfileUserInfoData,
                                        guild.let {
                                            loritta.profileDesignManager.transformGuildToProfileGuildInfoData(
                                                it
                                            )
                                        },
                                        loritta.profileDesignManager.designs.first {
                                            it.internalName == item.internalName
                                        }
                                    )

                                    shopItems.add(ProfileDesignItemWrapper(item, profileResult))
                                }

                                for (item in result.backgrounds.filter { it.tag == "website.dailyShop.new" }) {
                                    val dssNamespace = loritta.dreamStorageService.getCachedNamespaceOrRetrieve()
                                    val backgroundWithVariations = item.backgroundWithVariations

                                    val variation =
                                        backgroundWithVariations.variations.filterIsInstance<DefaultBackgroundVariation>()
                                            .first()

                                    val backgroundUrl = when (variation.storageType) {
                                        BackgroundStorageType.DREAM_STORAGE_SERVICE -> loritta.profileDesignManager.getDreamStorageServiceBackgroundUrlWithCropParameters(
                                            loritta.config.loritta.dreamStorageService.url,
                                            dssNamespace,
                                            variation
                                        )

                                        BackgroundStorageType.ETHEREAL_GAMBI -> loritta.profileDesignManager.getEtherealGambiBackgroundUrl(
                                            variation
                                        )
                                    }

                                    shopItems.add(BackgroundItemWrapper(item, backgroundUrl))
                                }

                                if (shopItems.isNotEmpty())
                                    sendDailyShopTrinketsNotification(
                                        channel,
                                        config[LorittaDailyShopNotificationsConfigs.newTrinketsMessage],
                                        shopItems,
                                        "new-trinkets"
                                    )
                            } catch (e: Exception) {
                                logger.warn(e) { "Something went wrong while trying to send daily shop trinkets notifications to ${guild.idLong}!" }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                logger.warn(e) { "Something went wrong while trying to process daily shop trinkets notifications to ${guild.idLong}!" }
            }
        }

        return LorittaInternalRPCResponse.DailyShopRefreshedResponse
    }


    private data class Result(
        val backgrounds: List<DailyShopBackgroundEntry>,
        val profileDesigns: List<ProfileDesign>,
        val configs: List<ResultRow>,
    )

    private sealed class ShopItemWrapper {
        abstract val internalName: String
        abstract val rarity: Rarity
        abstract val tag: String?
        abstract val localePrefix: String?
        abstract val price: Int
        abstract val set: String?
        abstract val createdBy: List<String>
        abstract val nameKey: String
        abstract val descriptionKey: String
    }

    private class BackgroundItemWrapper(backgroundEntry: DailyShopBackgroundEntry, val backgroundUrl: String) : ShopItemWrapper() {
        val background = backgroundEntry.backgroundWithVariations.background
        val variations = backgroundEntry.backgroundWithVariations.variations
        override val internalName = background.id
        override val rarity = background.rarity
        override val tag = backgroundEntry.tag
        override val localePrefix = "backgrounds"
        override val price = rarity.getBackgroundPrice()
        override val set = backgroundEntry.backgroundWithVariations.background.set
        override val createdBy = backgroundEntry.backgroundWithVariations.background.createdBy

        override val nameKey = "backgrounds.${internalName}.title"
        override val descriptionKey = "backgrounds.${internalName}.description"
    }

    private class ProfileDesignItemWrapper(val profileDesign: ProfileDesign, val profileResult: ProfileDesignManager.ProfileCreationResult) : ShopItemWrapper() {
        override val internalName = profileDesign.internalName
        override val rarity = profileDesign.rarity
        override val tag = profileDesign.tag
        override val localePrefix = "profileDesigns"
        override val price = rarity.getProfilePrice()
        override val set = profileDesign.set
        override val createdBy = profileDesign.createdBy

        override val nameKey = "profileDesigns.${internalName}.title"
        override val descriptionKey = "profileDesigns.${internalName}.description"
    }
}