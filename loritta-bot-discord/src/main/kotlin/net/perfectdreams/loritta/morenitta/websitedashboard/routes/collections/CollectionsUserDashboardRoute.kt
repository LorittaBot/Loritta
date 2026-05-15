package net.perfectdreams.loritta.morenitta.websitedashboard.routes.collections

import io.ktor.server.application.*
import kotlinx.html.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.BackgroundPayments
import net.perfectdreams.loritta.cinnamon.pudding.tables.CollectionItems
import net.perfectdreams.loritta.cinnamon.pudding.tables.CollectionRewardBackgrounds
import net.perfectdreams.loritta.cinnamon.pudding.tables.CollectionRewardProfileDesigns
import net.perfectdreams.loritta.cinnamon.pudding.tables.Collections
import net.perfectdreams.loritta.cinnamon.pudding.tables.CompletedCollections
import net.perfectdreams.loritta.cinnamon.pudding.tables.ProfileDesignsPayments
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleManager
import net.perfectdreams.loritta.common.utils.UserPremiumPlan
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.profile.badges.CollectionBadge
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.UserDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.userDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import org.jetbrains.exposed.sql.selectAll
import java.util.UUID

class CollectionsUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/collections") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlan, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        // Hacky!
        val locale = website.loritta.localeManager.getLocaleById(LocaleManager.DEFAULT_LOCALE_ID)

        // Badges are code-defined (not in the database), so we look them up here and match them to collections by their ID
        val collectionBadgeIdsByCollection = website.loritta.profileDesignManager.badges
            .filterIsInstance<CollectionBadge>()
            .associate { it.collectionId to it.id }

        val collections = website.loritta.transaction {
            val ownedBackgrounds = BackgroundPayments.select(BackgroundPayments.background)
                .where { BackgroundPayments.userId eq session.userId }
                .map { it[BackgroundPayments.background].value }
                .toSet()

            val ownedProfileDesigns = ProfileDesignsPayments.select(ProfileDesignsPayments.profile)
                .where { ProfileDesignsPayments.userId eq session.userId }
                .map { it[ProfileDesignsPayments.profile].value }
                .toSet()

            val completedCollectionIds = CompletedCollections.select(CompletedCollections.collection)
                .where { CompletedCollections.userId eq session.userId }
                .map { it[CompletedCollections.collection].value }
                .toSet()

            Collections.selectAll()
                .where { Collections.enabled eq true }
                .toList()
                .sortedBy { it[Collections.addedAt] }
                .map { collectionRow ->
                    val collectionId = collectionRow[Collections.id].value

                    val items = CollectionItems.selectAll()
                        .where { CollectionItems.collection eq collectionId }
                        .map { item ->
                            val background = item[CollectionItems.background]?.value
                            val profileDesign = item[CollectionItems.profileDesign]?.value

                            if (background != null) {
                                CollectionItem(background, ItemType.BACKGROUND, background in ownedBackgrounds)
                            } else {
                                CollectionItem(profileDesign!!, ItemType.PROFILE_DESIGN, profileDesign in ownedProfileDesigns)
                            }
                        }

                    val rewardBackgrounds = CollectionRewardBackgrounds.select(CollectionRewardBackgrounds.background)
                        .where { CollectionRewardBackgrounds.collection eq collectionId }
                        .map { CollectionItem(it[CollectionRewardBackgrounds.background].value, ItemType.BACKGROUND, false) }

                    val rewardProfileDesigns = CollectionRewardProfileDesigns.select(CollectionRewardProfileDesigns.profileDesign)
                        .where { CollectionRewardProfileDesigns.collection eq collectionId }
                        .map { CollectionItem(it[CollectionRewardProfileDesigns.profileDesign].value, ItemType.PROFILE_DESIGN, false) }

                    Collection(
                        collectionId,
                        items,
                        collectionRow[Collections.rewardSonhos],
                        rewardBackgrounds + rewardProfileDesigns,
                        collectionBadgeIdsByCollection[collectionId],
                        collectionId in completedCollectionIds
                    )
                }
        }

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.Collections.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    userDashLeftSidebarEntries(website.loritta, i18nContext, userPremiumPlan, UserDashboardSection.COLLECTIONS)
                },
                {
                    div {
                        div(classes = "hero-wrapper") {
                            div(classes = "hero-text") {
                                h1 {
                                    text(i18nContext.get(DashboardI18nKeysData.Collections.Title))
                                }

                                for (line in i18nContext.get(DashboardI18nKeysData.Collections.Description)) {
                                    p {
                                        text(line)
                                    }
                                }
                            }
                        }

                        if (collections.isEmpty()) {
                            p {
                                text(i18nContext.get(DashboardI18nKeysData.Collections.Empty))
                            }
                        }

                        for (collection in collections) {
                            renderCollection(i18nContext, locale, collection)
                        }
                    }
                }
            )
        }
    }

    private fun FlowContent.renderCollection(i18nContext: I18nContext, locale: BaseLocale, collection: Collection) {
        val ownedCount = collection.items.count { it.owned }
        val totalCount = collection.items.size
        val progressPercentage = if (totalCount == 0) 0 else (ownedCount * 100) / totalCount

        div(classes = "collection-entry") {
            h2 {
                text(i18nContext.get("collections.${collection.internalName}.title"))
            }

            p {
                text(i18nContext.get("collections.${collection.internalName}.description"))
            }

            // Progress bar
            div {
                style = "width: 100%; height: 1.5em; background-color: var(--inset-background-color); border-radius: 999px; overflow: hidden;"

                div {
                    style = "width: ${progressPercentage}%; height: 100%; background-color: var(--primary-500); border-radius: 999px; transition: width 0.2s;"
                }
            }

            p {
                style = "margin-top: 0.5em;"

                if (collection.completed) {
                    text(i18nContext.get(DashboardI18nKeysData.Collections.Completed))
                } else {
                    text(
                        i18nContext.get(
                            DashboardI18nKeysData.Collections.Progress(
                                owned = ownedCount,
                                total = totalCount
                            )
                        )
                    )
                }
            }

            // Items that need to be collected
            div(classes = "collection-items-container") {
                div(classes = "loritta-items-wrapper") {
                    for (item in collection.items) {
                        renderItemTile(i18nContext, locale, item)
                    }
                }
            }

            if (collection.rewardSonhos > 0L || collection.rewardItems.isNotEmpty() || collection.rewardBadgeId != null) {
                h3 {
                    text(i18nContext.get(DashboardI18nKeysData.Collections.RewardsTitle))
                }

                if (collection.rewardSonhos > 0L) {
                    p {
                        text(
                            i18nContext.get(
                                DashboardI18nKeysData.Collections.RewardSonhos(sonhos = collection.rewardSonhos)
                            )
                        )
                    }
                }

                if (collection.rewardItems.isNotEmpty() || collection.rewardBadgeId != null) {
                    div(classes = "collection-items-container") {
                        div(classes = "loritta-items-wrapper") {
                            for (item in collection.rewardItems) {
                                renderItemTile(i18nContext, locale, item)
                            }

                            if (collection.rewardBadgeId != null) {
                                renderBadgeTile(i18nContext, collection.rewardBadgeId, collection.completed)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun FlowContent.renderBadgeTile(i18nContext: I18nContext, badgeId: UUID, owned: Boolean) {
        val classes = buildString {
            append("shop-item-entry rarity-common")
            if (!owned) append(" locked")
        }

        val localePathId = i18nContext.get(I18nKeysData.Website.LocalePathId)

        div(classes = classes) {
            div {
                style = "display: flex; justify-content: center; align-items: center; aspect-ratio: 1/1; padding: 1em;"

                img {
                    src = "/$localePathId/badge-image/$badgeId"
                    style = "max-width: 100%; max-height: 100%; object-fit: contain;"
                }
            }
        }
    }

    private fun FlowContent.renderItemTile(i18nContext: I18nContext, locale: BaseLocale, item: CollectionItem) {
        val classes = buildString {
            append("shop-item-entry rarity-common")
            if (!item.owned) append(" locked")
        }

        val localePathId = i18nContext.get(I18nKeysData.Website.LocalePathId)

        val (imageSrc, title) = when (item.type) {
            ItemType.BACKGROUND -> "/$localePathId/background-preview/${item.internalName}" to locale["backgrounds.${item.internalName}.title"]
            ItemType.PROFILE_DESIGN -> "/$localePathId/profile-preview?type=${item.internalName}" to locale["profiles.${item.internalName}.title"]
        }

        div(classes = classes) {
            div {
                style = "overflow: hidden; line-height: 0;"

                img {
                    src = imageSrc
                    attributes["title"] = title
                    attributes["alt"] = title
                    style = "width: 100%; height: auto; aspect-ratio: 4/3;"
                }
            }
        }
    }

    private enum class ItemType {
        BACKGROUND,
        PROFILE_DESIGN
    }

    private data class CollectionItem(
        val internalName: String,
        val type: ItemType,
        val owned: Boolean
    )

    private data class Collection(
        val internalName: String,
        val items: List<CollectionItem>,
        val rewardSonhos: Long,
        val rewardItems: List<CollectionItem>,
        val rewardBadgeId: UUID?,
        val completed: Boolean
    )
}
