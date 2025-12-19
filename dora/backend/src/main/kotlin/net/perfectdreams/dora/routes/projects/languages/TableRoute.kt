package net.perfectdreams.dora.routes.projects.languages

import io.ktor.server.application.*
import io.ktor.server.util.*
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.style
import kotlinx.html.style
import net.perfectdreams.dora.*
import net.perfectdreams.dora.components.*
import net.perfectdreams.dora.routes.RequiresProjectAuthDashboardRoute
import net.perfectdreams.dora.tables.*
import net.perfectdreams.dora.utils.respondHtml
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.selectAll
import java.util.UUID

class TableRoute(val dora: DoraBackend) : RequiresProjectAuthDashboardRoute(dora, "/languages/{languageSlug}/table") {
    override suspend fun onAuthenticatedProjectRequest(call: ApplicationCall, session: DoraUserSession, project: Project, projectPermissionLevel: ProjectPermissionLevel) {
        val projectSlug = call.parameters.getOrFail("projectSlug")
        val languageSlug = call.parameters.getOrFail("languageSlug")

        val filtersParam = call.request.queryParameters.getAll("filters") ?: call.request.queryParameters["filters"]?.let { listOf(it) } ?: emptyList()
        val untranslatedOnly = filtersParam.any { it.equals("UNTRANSLATED", ignoreCase = true) }

        val (language, sourceStrings) = dora.pudding.transaction {
            val language = LanguageTargets.selectAll()
                .where {
                    LanguageTargets.languageId eq languageSlug and (LanguageTargets.project eq project.id)
                }
                .first()

            val sourceStrings = SourceStrings
                .leftJoin(TranslationsStrings, { SourceStrings.id }, { TranslationsStrings.sourceString })
                {
                    TranslationsStrings.language eq language[LanguageTargets.id]
                }
                .leftJoin(Users, { TranslationsStrings.translatedBy }, { Users.id })
                .leftJoin(CachedDiscordUserIdentifications, { Users.id }, { CachedDiscordUserIdentifications.id })
                .leftJoin(MachineTranslatedStrings, { SourceStrings.id }, { MachineTranslatedStrings.sourceString })
                {
                    MachineTranslatedStrings.language eq language[LanguageTargets.id]
                }
                .selectAll()
                .where { SourceStrings.project eq project.id }
                .orderBy(SourceStrings.key, SortOrder.ASC)
                .toList()

            Pair(language, sourceStrings)
        }

        val filteredSourceStrings = if (untranslatedOnly) {
            sourceStrings.filter { it.getOrNull(TranslationsStrings.text) == null }
        } else {
            sourceStrings
        }

        val totalCount = sourceStrings.size
        val translatedCount = sourceStrings.count { it.getOrNull(TranslationsStrings.text) != null }

        call.respondHtml {
            dashboardBase(
                "Dora",
                {
                    languageLeftSidebarEntries(
                        project,
                        language,
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
                                text("${filteredSourceStrings.size} strings")
                            }
                        }
                    }

                    div {
                        style = "background-color: var(--card-border-color);\n" +
                                "  display: flex;\n" +
                                "  flex-direction: column;\n" +
                                "  gap: 1px;\n" +
                                "  padding: 1px;"

                        for (string in filteredSourceStrings) {
                            val uniqueId = UUID.randomUUID().toString()

                            batchEntry(
                                project,
                                uniqueId,
                                languageSlug,
                                string[SourceStrings.key],
                                string[SourceStrings.context],
                                string[SourceStrings.text],
                                string.getOrNull(MachineTranslatedStrings.text),
                                string.getOrNull(TranslationsStrings.text),
                                string.getOrNull(TranslationsStrings.text) != null,
                                if (string.getOrNull(TranslationsStrings.id) != null) {
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
                }
            }
        }
    }
}