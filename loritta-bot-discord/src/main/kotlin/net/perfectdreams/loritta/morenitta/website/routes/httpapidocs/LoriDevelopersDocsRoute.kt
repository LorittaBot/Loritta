package net.perfectdreams.loritta.morenitta.website.routes.httpapidocs

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import io.ktor.server.application.*
import kotlinx.coroutines.flow.first
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserWebsiteSettings
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.LocalizedRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.httpapidocs.LoriDevelopersDocsView
import net.perfectdreams.loritta.morenitta.website.views.httpapidocs.LoriEndpointDevelopersDocsView
import net.perfectdreams.loritta.morenitta.website.views.httpapidocs.mainframeTerminalLorifetch
import net.perfectdreams.loritta.publichttpapi.LoriPublicHttpApiEndpoints
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jsoup.Jsoup
import java.io.File
import kotlin.collections.set

class LoriDevelopersDocsRoute(loritta: LorittaBot) : LocalizedRoute(loritta, "/developers/docs/{apiDocsPath...}") {
    override val isMainClusterOnlyRoute = true

    override suspend fun onLocalizedRequest(
        call: ApplicationCall,
        locale: BaseLocale,
        i18nContext: I18nContext
    ) {
        val pathThatWillBeRendered = call.parameters["apiDocsPath"] ?: "index"
        val devDocs = Yaml.default.decodeFromStream<DevDocs>(File(loritta.config.loritta.folders.content, "dev-docs.yml").inputStream())

        // Load all pages to know what we can actually access
        val sidebarCategories = devDocs.sidebar.map {
            LoriDevelopersDocsView.SidebarCategory(
                it.name,
                it.pages.map {
                    when (it) {
                        is DevDocs.SidebarCategory.SidebarDocsPageEntry -> {
                            // The first --- should always be empty
                            val contentInMarkdownWithFrontmatter = getPostRawContent(i18nContext, it.file)
                                .split("---")
                                .drop(1)

                            val frontmatterYaml = contentInMarkdownWithFrontmatter[0]
                            val contentMetadata = Yaml.default.decodeFromString<DocsContentMetadata>(frontmatterYaml)

                            when (contentMetadata) {
                                is DocsContentMetadata.TextDocsContentMetadata -> {
                                    LoriDevelopersDocsView.SidebarEntry.SidebarPageEntry(
                                        contentMetadata.title,
                                        it.file,
                                        it.icon
                                    )
                                }
                            }
                        }
                        is DevDocs.SidebarCategory.SidebarEndpointPageEntry -> {
                            LoriDevelopersDocsView.SidebarEntry.SidebarEndpointEntry(
                                i18nContext.get(MagicEndpoints.getEndpointI18nTitle(it.endpointId)),
                                it.endpointId.lowercase().replace("_", "-"), // A bit of an hack :P
                                it.endpointId,
                                LoriPublicHttpApiEndpoints.getEndpointById(it.endpointId)
                            )
                        }
                    }
                }
            )
        }

        val pageToAccess = sidebarCategories.flatMap { it.entries }
            .firstOrNull {
                when (it) {
                    is LoriDevelopersDocsView.SidebarEntry.SidebarEndpointEntry -> {
                        pathThatWillBeRendered == it.path
                    }
                    is LoriDevelopersDocsView.SidebarEntry.SidebarPageEntry -> {
                        pathThatWillBeRendered == it.path
                    }
                }
            }

        // Unknown page, bail out!
        if (pageToAccess == null)
            return

        // TODO: Fix this! Currently we always default to the light theme
        val session = loritta.dashboardWebServer.getSession(call)
        val dashboardColorThemePreference = ColorTheme.LIGHT

        val lorifetchStats = loritta.newWebsite!!.lorifetch.statsFlow.first()

        if (pageToAccess is LoriDevelopersDocsView.SidebarEntry.SidebarEndpointEntry) {
            call.respondHtml(
                LoriEndpointDevelopersDocsView(
                    loritta.newWebsite!!,
                    i18nContext,
                    locale,
                    getPathWithoutLocale(call),
                    loritta.getLegacyLocaleById(locale.id),
                    session,
                    if (session != null) {
                        UserPremiumPlans.getPlanFromValue(loritta.getActiveMoneyFromDonations(session.userId))
                    } else UserPremiumPlans.Free,
                    dashboardColorThemePreference,
                    sidebarCategories,
                    pageToAccess.endpointId,
                    pageToAccess.endpoint,
                    MagicEndpoints.endpointTesterOptions[pageToAccess.endpoint] ?: error("Whoops"),
                    lorifetchStats.guildCount,
                    lorifetchStats.executedCommands,
                    lorifetchStats.uniqueUsersExecutedCommands,
                    lorifetchStats.currentSong
                ).generateHtml()
            )

            return
        }

        val options = MutableDataSet()
            .set(Parser.EXTENSIONS, listOf(TablesExtension.create(), StrikethroughExtension.create()))
            .set(HtmlRenderer.GENERATE_HEADER_ID, true)
            .set(HtmlRenderer.RENDER_HEADER_ID, true)
        val parser = Parser.builder(options).build()
        val renderer = HtmlRenderer.builder(options).build()

        // The first --- should always be empty
        val rawContent = getPostRawContent(i18nContext, pathThatWillBeRendered)

        val contentInMarkdownWithFrontmatter = rawContent.split("---")
            .drop(1)

        val frontmatterYaml = contentInMarkdownWithFrontmatter[0]
        val contentInMarkdown = renderer.render(parser.parse(contentInMarkdownWithFrontmatter[1]))

        val contentMetadata = Yaml.default.decodeFromString<DocsContentMetadata>(frontmatterYaml)
        val soup = Jsoup.parse(contentInMarkdown)

        val playlistInfo = Yaml.default.decodeFromStream<SongPlaylist>(File(loritta.config.loritta.folders.content, "playlist.yml").inputStream())

        soup.select("transactiontypes-table")
            .forEach {
                it.html(
                    createHTML()
                        .table {
                            tr {
                                th {
                                    text("Enum")
                                }

                                th {
                                    text("Nome")
                                }

                                th {
                                    text("Descrição")
                                }
                            }

                            for (transactionType in TransactionType.entries) {
                                tr {
                                    td {
                                        code {
                                            text(transactionType.name)
                                        }
                                    }

                                    td {
                                        text(i18nContext.get(transactionType.title))
                                    }

                                    td {
                                        text(i18nContext.get(transactionType.description))
                                    }
                                }
                            }
                        }
                )
            }

        soup.select("loritta-devs-soundtrack")
            .forEach {
                it.html(
                    createHTML()
                        .ul {
                            for (song in playlistInfo.songs.sortedBy { it.title }) {
                                li {
                                    a(href = "https://youtu.be/${song.youtubeId}") {
                                        text(song.title)
                                    }
                                }
                            }
                        }
                )
            }

        soup.select("loritta-mainframe-lorifetch")
            .forEach {
                it.html(
                createHTML()
                    .div {
                        mainframeTerminalLorifetch(
                            loritta,
                            i18nContext,
                            session,
                            "Terminal",
                            null,
                            lorifetchStats.guildCount,
                            lorifetchStats.executedCommands.toInt(),
                            lorifetchStats.uniqueUsersExecutedCommands,
                            lorifetchStats.currentSong
                        )
                    }
                )
            }

        // Make it fancy
        soup.select("details")
            .forEach {
                it.addClass("fancy-details")
            }

        soup.select("summary")
            .forEach {
                it.append(
                    createHTML().div(classes = "chevron-icon") {
                        i(classes = "fa-solid fa-chevron-down") {}
                    }
                )
            }

        call.respondHtml(
            LoriDevelopersDocsView(
                loritta.newWebsite!!,
                i18nContext,
                locale,
                getPathWithoutLocale(call),
                loritta.getLegacyLocaleById(locale.id),
                session,
                if (session != null) {
                    UserPremiumPlans.getPlanFromValue(loritta.getActiveMoneyFromDonations(session.userId))
                } else UserPremiumPlans.Free,
                dashboardColorThemePreference,
                sidebarCategories,
                contentMetadata,
                soup.html()
            ).generateHtml()
        )
    }

    private fun getPostRawContent(
        i18nContext: I18nContext,
        pathThatWillBeRendered: String
    ): String {
        val targetFile: File
        var targetI18nContext: I18nContext = i18nContext
        while (true) {
            val i18nContextId = loritta.languageManager.getIdByI18nContext(targetI18nContext)
            val maybeTargetFile = File(loritta.config.loritta.folders.content, "dev-docs/${pathThatWillBeRendered}.$i18nContextId.md")
            if (maybeTargetFile.exists()) {
                targetFile = maybeTargetFile
                break
            }
            // If not, let's check the inherited
            val inheritsFrom = targetI18nContext.language.info.inheritsFrom ?: error("Couldn't find a valid post for $pathThatWillBeRendered") // Bail out!
            targetI18nContext = loritta.languageManager.getI18nContextById(inheritsFrom) // Also bail out!
        }
        return targetFile.readText(Charsets.UTF_8)
    }

    companion object {
        fun FlowContent.createObjectTemplateButton(
            i18nContext: I18nContext,
            endpointId: String,
            option: MagicEndpoints.APIParameter,
            clazzName: String,
            text: String
        ) {
            button(type = ButtonType.submit, classes = "discord-button primary") {
                attributes["hx-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/developers/docs/create-object-template"
                attributes["hx-target"] = "this"
                attributes["hx-disabled-elt"] = "this"
                attributes["hx-swap"] = "outerHTML"
                attributes["hx-vals"] = buildJsonObject {
                    put("endpointId", endpointId)
                    put("clazzName", clazzName)
                    put("prefix", option.name)
                }.toString()

                text(text)
            }
        }
    }
}