package net.perfectdreams.loritta.morenitta.websitedashboard.routes.dailyshop

import net.perfectdreams.loritta.cinnamon.pudding.services.fromRow
import net.perfectdreams.loritta.cinnamon.pudding.tables.BackgroundPayments
import net.perfectdreams.loritta.cinnamon.pudding.tables.BackgroundVariations
import net.perfectdreams.loritta.cinnamon.pudding.tables.Backgrounds
import net.perfectdreams.loritta.cinnamon.pudding.tables.DailyProfileShopItems
import net.perfectdreams.loritta.cinnamon.pudding.tables.DailyShopItems
import net.perfectdreams.loritta.cinnamon.pudding.tables.DailyShops
import net.perfectdreams.loritta.cinnamon.pudding.tables.ProfileDesigns
import net.perfectdreams.loritta.cinnamon.pudding.tables.ProfileDesignsPayments
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ProfileDesign
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.serializable.Background
import net.perfectdreams.loritta.serializable.BackgroundVariation
import net.perfectdreams.loritta.serializable.BackgroundWithVariations
import net.perfectdreams.loritta.serializable.DailyShopBackgroundEntry
import net.perfectdreams.loritta.serializable.ProfileSectionsResponse
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

object DashboardDailyShopUtils {
    suspend fun queryDailyShopResult(
        loritta: LorittaBot,
        userId: Long,
        dreamStorageServiceNamespace: String
    ): DailyShopResult {
        val shop = DailyShops.selectAll().orderBy(DailyShops.generatedAt, SortOrder.DESC).limit(1).first()

        val profile = loritta.getLorittaProfile(userId)
        val activeProfileDesignId = profile?.settings?.activeProfileDesignInternalName?.value ?: ProfileDesign.DEFAULT_PROFILE_DESIGN_ID
        val activeBackgroundId = profile?.settings?.activeBackgroundInternalName?.value ?: Background.DEFAULT_BACKGROUND_ID

        val backgroundsInShopResults = (DailyShopItems innerJoin Backgrounds)
            .selectAll()
            .where {
                DailyShopItems.shop eq shop[DailyShops.id]
            }
            .toList()

        val backgroundsInShop = backgroundsInShopResults.map {
            DailyShopBackgroundEntry(
                BackgroundWithVariations(
                    Background.fromRow(it),
                    loritta.pudding.backgrounds.getBackgroundVariations(it[Backgrounds.internalName])
                ),
                it[DailyShopItems.tag]
            )
        }

        val profileDesignsInShop = (DailyProfileShopItems innerJoin ProfileDesigns)
            .selectAll()
            .where {
                DailyProfileShopItems.shop eq shop[DailyShops.id]
            }
            .map {
                WebsiteUtils.fromProfileDesignToSerializable(loritta, it).also { profile ->
                    profile.tag = it[DailyProfileShopItems.tag]
                }
            }
            .toList()

        val boughtBackgroundsResults = Backgrounds.selectAll().where {
            Backgrounds.internalName inList BackgroundPayments.selectAll().where {
                BackgroundPayments.userId eq userId
            }.map { it[BackgroundPayments.background].value }
        }.map {
            Background.fromRow(it)
        } + loritta.pudding.backgrounds.getBackground(net.perfectdreams.loritta.serializable.Background.DEFAULT_BACKGROUND_ID)!!.data // The default background should always exist

        // OPTIMIZATION: Bulk get user bought background variations to avoid multiple selects
        val queriedBackgroundVariations = BackgroundVariations
            .selectAll()
            .where { BackgroundVariations.background inList boughtBackgroundsResults.map { it.id } }
            .toList()

        val backgroundsWrapper = ProfileSectionsResponse.BackgroundsWrapper(
            loritta.dreamStorageService.baseUrl,
            dreamStorageServiceNamespace,
            loritta.config.loritta.etherealGambiService.url,
            boughtBackgroundsResults.map {
                BackgroundWithVariations(
                    it,
                    queriedBackgroundVariations.filter { variation -> variation[BackgroundVariations.background].value == it.id }
                        .map { BackgroundVariation.fromRow(it) }
                )
            }
        )

        val boughtProfileDesignsResults = ProfileDesigns.selectAll().where {
            ProfileDesigns.internalName inList ProfileDesignsPayments.selectAll().where {
                ProfileDesignsPayments.userId eq userId
            }.map { it[ProfileDesignsPayments.profile].value }
        }

        val boughtProfileDesigns = boughtProfileDesignsResults.map {
            WebsiteUtils.toSerializable(
                loritta,
                ProfileDesign.wrapRow(it)
            )
        } + WebsiteUtils.toSerializable(
            loritta,
            ProfileDesign.findById(
                ProfileDesign.DEFAULT_PROFILE_DESIGN_ID
            )!!
        )

        val dailyShop = DailyShopResult(
            profile,
            activeProfileDesignId,
            activeBackgroundId,
            backgroundsInShop,
            profileDesignsInShop,
            backgroundsWrapper,
            boughtProfileDesigns,
            shop[DailyShops.generatedAt]
        )

        return dailyShop
    }

    /**
     * Queries when the current daily shop was generated
     */
    fun queryCurrentDailyShopGeneratedAt(): Long {
        val generatedAt = DailyShops.select(DailyShops.generatedAt)
            .orderBy(DailyShops.generatedAt, SortOrder.DESC)
            .limit(1)
            .first()[DailyShops.generatedAt]

        return generatedAt
    }

    /**
     * Gets when Loritta's Item Shop resets, in epoch millis
     */
    fun getShopResetsEpochMilli(): Long {
        val midnight = LocalTime.MIDNIGHT
        val today = LocalDate.now(ZoneOffset.UTC)
        val todayMidnight = LocalDateTime.of(today, midnight)
        val tomorrowMidnight = todayMidnight.plusDays(1)
        return tomorrowMidnight.toInstant(ZoneOffset.UTC).toEpochMilli()
    }
}