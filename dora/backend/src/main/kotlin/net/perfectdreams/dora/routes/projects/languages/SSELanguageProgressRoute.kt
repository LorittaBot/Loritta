package net.perfectdreams.dora.routes.projects.languages

import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.server.application.*
import io.ktor.server.response.cacheControl
import io.ktor.server.response.header
import io.ktor.server.response.respondBytesWriter
import io.ktor.server.util.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import kotlinx.html.style
import kotlinx.serialization.json.Json
import net.perfectdreams.dora.*
import net.perfectdreams.dora.components.*
import net.perfectdreams.dora.routes.RequiresProjectAuthDashboardRoute
import net.perfectdreams.dora.tables.*
import net.perfectdreams.dora.utils.SseEvent
import net.perfectdreams.dora.utils.TranslationProgress
import net.perfectdreams.dora.utils.respondHtml
import net.perfectdreams.dora.utils.writeSseEvent
import net.perfectdreams.luna.bliss.SSEBliss
import net.perfectdreams.luna.bliss.SSEBlissSwap
import net.perfectdreams.luna.toasts.EmbeddedToast
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.selectAll
import java.util.UUID

class SSELanguageProgressRoute(val dora: DoraBackend) : RequiresProjectAuthDashboardRoute(dora, "/languages/{languageSlug}/language-progress") {
    override suspend fun onAuthenticatedProjectRequest(call: ApplicationCall, session: DoraUserSession, project: Project, projectPermissionLevel: ProjectPermissionLevel) {
        // Makes SSE work behind nginx
        // https://stackoverflow.com/a/33414096/7271796
        call.response.header("X-Accel-Buffering", "no")
        call.response.cacheControl(CacheControl.NoCache(null))

        val languageSlug = call.parameters.getOrFail("languageSlug")

        val flow = dora.languageFlows.getOrPut(project.slug + "-" + languageSlug) {
            val (translatedStrings, sourceStrings) = dora.pudding.transaction {
                val languageTarget = LanguageTargets.selectAll()
                    .where {
                        LanguageTargets.languageId eq languageSlug and (LanguageTargets.project eq project.id)
                    }
                    .first()

                val translatedStrings = TranslationsStrings.selectAll()
                    .where {
                        TranslationsStrings.language eq languageTarget[LanguageTargets.id]
                    }
                    .toList()

                val sourceStrings = SourceStrings.selectAll()
                    .where {
                        SourceStrings.project eq project.id
                    }
                    .toList()

                Pair(translatedStrings, sourceStrings)
            }

            MutableStateFlow(TranslationProgress(translatedStrings.size, sourceStrings.size))
        }

        call.respondBytesWriter(contentType = ContentType.Text.EventStream) {
            call.launch {
                while (!isClosedForWrite) {
                    // We need to send a heartbeat event every once in a while to avoid the connection getting stuck in "open" state!
                    // Because the proxy only knows that it has been closed if sending an event fails.
                    writeSseEvent(
                        SseEvent(
                            data = System.currentTimeMillis().toString(),
                            event = "heartbeat"
                        )
                    )
                    flush()
                    delay(15_000)
                }
                // This is required to cancel any coroutines launched by this route (especially flow collections!)
                call.cancel()
            }

            flow.collect {
                writeSseEvent(
                    SseEvent(
                        data = Json.encodeToString<SSEBliss>(
                            SSEBlissSwap(
                                createHTML()
                                    .div {
                                        languageProgressBar(
                                            it.translatedCount,
                                            it.totalCount
                                        )
                                    },
                                "body (innerHTML) -> #left-sidebar-language-progress (innerHTML)"
                            )
                        )
                    )
                )
                flush()
            }
        }
    }
}