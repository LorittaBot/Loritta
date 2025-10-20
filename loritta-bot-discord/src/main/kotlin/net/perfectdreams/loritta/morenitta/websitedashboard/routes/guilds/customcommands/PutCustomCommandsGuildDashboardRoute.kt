package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.customcommands

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receiveText
import io.ktor.server.util.getOrFail
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.CustomGuildCommands
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.configSaved
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.CustomCommandCodeType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.update

class PutCustomCommandsGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/custom-commands/{entryId}") {
    @Serializable
    data class CreateTextCommandRequest(
        val label: String,
        val message: String
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme, guild: Guild) {
        val entryId = call.parameters.getOrFail("entryId").toLong()

        val request = Json.decodeFromString<CreateTextCommandRequest>(call.receiveText())

        val result = website.loritta.transaction {
            val updated = CustomGuildCommands.update({ CustomGuildCommands.guild eq guild.idLong and (CustomGuildCommands.id eq entryId) }) {
                it[CustomGuildCommands.enabled] = true
                it[CustomGuildCommands.guild] = guild.idLong
                it[CustomGuildCommands.label] = request.label
                    .replace(" ", "")
                    .lowercase()
                it[CustomGuildCommands.codeType] = CustomCommandCodeType.SIMPLE_TEXT
                it[CustomGuildCommands.code] = request.message
            }

            if (updated == 0)
                return@transaction Result.CommandNotFound

            return@transaction Result.Success
        }

        when (result) {
            Result.Success -> {
                call.respondHtml(
                    createHTML(false)
                        .body {
                            configSaved(i18nContext)
                        }
                )
            }
            Result.CommandNotFound -> {
                call.respondHtml(
                    createHTML(false)
                        .body {
                            blissShowToast(
                                createEmbeddedToast(
                                    EmbeddedToast.Type.WARN,
                                    "Você não pode editar um comando que não existe!"
                                )
                            )
                        },
                    status = HttpStatusCode.NotFound
                )
            }
        }
    }

    private sealed class Result {
        data object Success : Result()
        data object CommandNotFound : Result()
    }
}