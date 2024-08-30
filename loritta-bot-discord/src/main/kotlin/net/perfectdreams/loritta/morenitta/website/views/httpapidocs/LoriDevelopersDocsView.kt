package net.perfectdreams.loritta.morenitta.website.views.httpapidocs

import kotlinx.html.DIV
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.unsafe
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.routes.httpapidocs.DocsContentMetadata
import net.perfectdreams.loritta.publichttpapi.LoriPublicHttpApiEndpoint
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession

class LoriDevelopersDocsView(
    lorittaWebsite: LorittaWebsite,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String,
    legacyBaseLocale: LegacyBaseLocale,
    userIdentification: LorittaJsonWebSession.UserIdentification?,
    userPremiumPlan: UserPremiumPlans,
    colorTheme: ColorTheme,
    sidebarCategories: List<SidebarCategory>,
    private val metadata: DocsContentMetadata,
    private val content: String
) : LoriDevelopersDocsDashboardView(
    lorittaWebsite,
    i18nContext,
    locale,
    path,
    legacyBaseLocale,
    userIdentification,
    userPremiumPlan,
    colorTheme,
    sidebarCategories
) {
    override fun DIV.generateRightSidebarContents() {
        div(classes = "developer-docs") {
            h1 {
                text(metadata.title)
            }

            unsafe {
                raw(content)
            }
        }
    }

    override fun getTitle() = metadata.title

    class SidebarCategory(
        val name: String?,
        val entries: List<SidebarEntry>
    )

    sealed class SidebarEntry {
        abstract val name: String
        abstract val path: String

        class SidebarEndpointEntry(
            override val name: String,
            override val path: String,
            val endpointId: String,
            val endpoint: LoriPublicHttpApiEndpoint
        ) : SidebarEntry()

        class SidebarPageEntry(
            override val name: String,
            override val path: String,
            val icon: String
        ) : SidebarEntry()
    }
}