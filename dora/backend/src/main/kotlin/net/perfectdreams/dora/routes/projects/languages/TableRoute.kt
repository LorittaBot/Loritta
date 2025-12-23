package net.perfectdreams.dora.routes.projects.languages

import io.ktor.server.application.*
import io.ktor.server.util.*
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.style
import kotlinx.html.style
import net.perfectdreams.dora.*
import net.perfectdreams.dora.components.*
import net.perfectdreams.dora.routes.RequiresProjectAuthDashboardRoute
import net.perfectdreams.dora.tables.*
import net.perfectdreams.dora.utils.PathBuilder
import net.perfectdreams.dora.utils.SVGIcons
import net.perfectdreams.dora.utils.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.components.svgIcon
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.selectAll
import java.util.UUID
import kotlin.collections.plus
import kotlin.math.ceil

class TableRoute(val dora: DoraBackend) : RequiresProjectAuthDashboardRoute(dora, "/languages/{languageSlug}/table") {
    override suspend fun onAuthenticatedProjectRequest(call: ApplicationCall, session: DoraUserSession, project: Project, projectPermissionLevel: ProjectPermissionLevel) {
        val projectSlug = call.parameters.getOrFail("projectSlug")
        val languageSlug = call.parameters.getOrFail("languageSlug")

        val filtersParam = call.request.queryParameters.getAll("filters") ?: call.request.queryParameters["filters"]?.let { listOf(it) } ?: emptyList()
        val untranslatedOnly = filtersParam.any { it.equals("UNTRANSLATED", ignoreCase = true) }
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val pageZeroIndexed = page - 1
        val maxEntries = call.request.queryParameters["maxEntries"]?.toIntOrNull() ?: 100
        val sort = call.request.queryParameters["sort"] ?: "keyasc"

        val result = dora.pudding.transaction {
            val language = LanguageTargets.selectAll()
                .where {
                    LanguageTargets.languageId eq languageSlug and (LanguageTargets.project eq project.id)
                }
                .first()

            fun sourceStringQuery(): Query {
                // The "query" is mutable, so we NEED to always create a new one
                // Which is why this is wrapped inside a function
                return SourceStrings
                    .leftJoin(TranslationsStrings, { SourceStrings.id }, { TranslationsStrings.sourceString })
                    {
                        TranslationsStrings.language eq language[LanguageTargets.id]
                    }
                    .leftJoin(Users, { TranslationsStrings.translatedBy }, { Users.id })
                    .leftJoin(CachedDiscordUserIdentifications, { Users.id }, { CachedDiscordUserIdentifications.id })
                    .selectAll()
            }

            val sourceStrings = sourceStringQuery()
                .where {
                    SourceStrings.project eq project.id and (if (untranslatedOnly) TranslationsStrings.text.isNull() else Op.TRUE)
                }
                .apply {
                    when (sort) {
                        "keyasc" -> orderBy(SourceStrings.key, SortOrder.ASC)
                        "keydesc" -> orderBy(SourceStrings.key, SortOrder.DESC)
                        "addedasc" -> orderBy(SourceStrings.addedAt, SortOrder.ASC)
                        "addeddesc" -> orderBy(SourceStrings.addedAt, SortOrder.DESC)
                    }
                }
                .orderBy(SourceStrings.key, SortOrder.ASC)
                .limit(maxEntries)
                .offset((pageZeroIndexed * maxEntries).toLong())
                .toList()

            val totalCount = sourceStringQuery().where { SourceStrings.project eq project.id }.count()
            val translatedCount = sourceStringQuery().where { SourceStrings.project eq project.id and (TranslationsStrings.text).isNotNull() }.count()
            val totalCountFiltered = sourceStringQuery().where { SourceStrings.project eq project.id and (if (untranslatedOnly) TranslationsStrings.text.isNull() else Op.TRUE) }.count()

            Result.Success(
                language,
                sourceStrings,
                totalCount,
                translatedCount,
                totalCountFiltered
            )
        }

        fun buildPaginationPath(newPage: Int): String {
            return PathBuilder("/projects/${project.slug}/languages/$languageSlug/table") {
                this.append("page", newPage.toString())
                if (untranslatedOnly) {
                    this.append("filters", "UNTRANSLATED")
                }
                if (maxEntries != 100)
                    this.append("maxEntries", maxEntries.toString())
            }
        }

        val totalCount = result.totalCount.toInt()
        val translatedCount = result.translatedCount.toInt()
        val totalPages = ceil(result.totalCountFiltered / maxEntries.toDouble()).toInt()

        call.respondHtml {
            dashboardBase(
                "Dora",
                {
                    languageLeftSidebarEntries(
                        project,
                        result.language,
                        if (untranslatedOnly) {
                            LanguageDashboardSection.UNTRANSLATED_STRINGS
                        } else {
                            LanguageDashboardSection.STRINGS
                        },
                        translatedCount,
                        totalCount
                    )
                }
            ) {
                cardsWithHeader {
                    cardHeader {
                        cardHeaderInfo {
                            cardHeaderTitle {
                                text(if (untranslatedOnly) "Strings (Não Traduzidas)" else "Strings")
                            }

                            cardHeaderDescription {
                                text("${result.totalCountFiltered} strings")
                            }
                        }
                    }

                    div {
                        style = "background-color: var(--card-border-color);\n" +
                                "  display: flex;\n" +
                                "  flex-direction: column;\n" +
                                "  gap: 1px;\n" +
                                "  padding: 1px;"

                        for (string in result.sourceStrings) {
                            val uniqueId = UUID.randomUUID().toString()

                            batchEntry(
                                project,
                                uniqueId,
                                languageSlug,
                                string[SourceStrings.key],
                                string[SourceStrings.context],
                                string[SourceStrings.transformers],
                                string[SourceStrings.text],
                                string.getOrNull(TranslationsStrings.text),
                                string.getOrNull(TranslationsStrings.text) != null,
                                if (string.getOrNull(TranslationsStrings.translatedBy) != null) {
                                    Translator(
                                        string[Users.id].value,
                                        string.getOrNull(CachedDiscordUserIdentifications.id)?.value ?: 0L,
                                        string.getOrNull(CachedDiscordUserIdentifications.username) ?: "Unknown",
                                        string[CachedDiscordUserIdentifications.avatarId]
                                    )
                                } else null,
                                false
                            )
                        }
                    }

                    div {
                        style = "display: flex; gap: 16px; justify-content: space-between;"

                        discordButtonLink(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT, href = if (page == 1) null else buildPaginationPath(page - 1)) {
                            classes += "text-with-icon"

                            if (page == 1) {
                                attributes["aria-disabled"] = "true"
                            } else {
                                attributes["bliss-get"] = "[href]"
                                attributes["bliss-swap:200"] = "#right-sidebar-contents (innerHTML) -> #right-sidebar-contents (innerHTML)"
                                attributes["bliss-push-url:200"] = "true"
                                attributes["bliss-scroll:200"] = "window:top"
                            }

                            svgIcon(SVGIcons.CaretLeft)

                            text("Voltar")
                        }

                        discordButtonLink(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT, href = if (page == totalPages) null else buildPaginationPath(page + 1)) {
                            classes += "text-with-icon"

                            if (page == totalPages) {
                                attributes["aria-disabled"] = "true"
                            } else {
                                attributes["bliss-get"] = "[href]"
                                attributes["bliss-swap:200"] = "#right-sidebar-contents (innerHTML) -> #right-sidebar-contents (innerHTML)"
                                attributes["bliss-push-url:200"] = "true"
                                attributes["bliss-scroll:200"] = "window:top"
                            }

                            svgIcon(SVGIcons.CaretRight)

                            text("Próximo")
                        }
                    }
                }
            }
        }
    }

    sealed class Result {
        data class Success(
            val language: ResultRow,
            val sourceStrings: List<ResultRow>,
            val totalCount: Long,
            val translatedCount: Long,
            val totalCountFiltered: Long
        ) : Result()
    }
}