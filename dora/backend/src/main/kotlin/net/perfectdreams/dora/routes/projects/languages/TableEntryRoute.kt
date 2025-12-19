package net.perfectdreams.dora.routes.projects.languages

import io.ktor.server.application.ApplicationCall
import io.ktor.server.util.getOrFail
import net.perfectdreams.dora.DoraBackend
import net.perfectdreams.dora.DoraUserSession
import net.perfectdreams.dora.Project
import net.perfectdreams.dora.ProjectPermissionLevel
import net.perfectdreams.dora.Translator
import net.perfectdreams.dora.components.batchEntry
import net.perfectdreams.dora.routes.RequiresProjectAuthDashboardRoute
import net.perfectdreams.dora.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.dora.tables.CachedDiscordUserIdentifications
import net.perfectdreams.dora.tables.LanguageTargets
import net.perfectdreams.dora.tables.MachineTranslatedStrings
import net.perfectdreams.dora.tables.SourceStrings
import net.perfectdreams.dora.tables.TranslationsStrings
import net.perfectdreams.dora.tables.Users
import net.perfectdreams.dora.utils.respondHtmlFragment
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.selectAll

class TableEntryRoute(val dora: DoraBackend) : RequiresProjectAuthDashboardRoute(dora, "/languages/{languageSlug}/table/{stringId}/{uniqueId}") {
    override suspend fun onAuthenticatedProjectRequest(call: ApplicationCall, session: DoraUserSession, project: Project, projectPermissionLevel: ProjectPermissionLevel) {
        val uniqueId = call.parameters.getOrFail("uniqueId")
        val languageSlug = call.parameters.getOrFail("languageSlug")
        val stringId = call.parameters.getOrFail("stringId")

        val sourceStrings = dora.pudding.transaction {
            val language = LanguageTargets.selectAll()
                .where {
                    LanguageTargets.languageId eq languageSlug and (LanguageTargets.project eq project.id)
                }
                .first()

            SourceStrings
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
                .where {
                    SourceStrings.key eq stringId and (SourceStrings.project eq project.id)
                }
                .orderBy(SourceStrings.id)
                .first()
        }

        call.respondHtmlFragment {
            batchEntry(
                project,
                uniqueId,
                languageSlug,
                stringId,
                sourceStrings[SourceStrings.context],
                sourceStrings[SourceStrings.text],
                sourceStrings.getOrNull(MachineTranslatedStrings.text),
                sourceStrings.getOrNull(TranslationsStrings.text),
                sourceStrings.getOrNull(TranslationsStrings.text) != null,
                if (sourceStrings.getOrNull(TranslationsStrings.id) != null) {
                    Translator(
                        sourceStrings[Users.id].value,
                        sourceStrings.getOrNull(CachedDiscordUserIdentifications.id)?.value ?: 0L,
                        sourceStrings.getOrNull(CachedDiscordUserIdentifications.username) ?: "Unknown",
                        sourceStrings[CachedDiscordUserIdentifications.avatarId]
                    )
                } else null,
                false
            )
        }
    }
}