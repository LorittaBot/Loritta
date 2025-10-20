package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.customcommands

import io.ktor.server.application.*
import io.ktor.server.request.receiveText
import io.ktor.server.response.header
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.CustomGuildCommands
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.customGuildCommandTextEditor
import net.perfectdreams.loritta.morenitta.websitedashboard.components.saveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.configSaved
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.CustomCommandCodeType
import org.jetbrains.exposed.sql.insertAndGetId

class PostCustomCommandsGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/custom-commands") {
    @Serializable
    data class CreateTextCommandRequest(
        val label: String,
        val message: String
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme, guild: Guild) {
        val request = Json.decodeFromString<CreateTextCommandRequest>(call.receiveText())

        val commandId = website.loritta.transaction {
            CustomGuildCommands.insertAndGetId {
                it[CustomGuildCommands.enabled] = true
                it[CustomGuildCommands.guild] = guild.idLong
                it[CustomGuildCommands.label] = request.label
                    .replace(" ", "")
                    .lowercase()
                it[CustomGuildCommands.codeType] = CustomCommandCodeType.SIMPLE_TEXT
                it[CustomGuildCommands.code] = request.message
            }
        }

        call.response.header("Bliss-Push-Url", "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/custom-commands/${commandId.value}")
        call.respondHtml(
            createHTML(false)
                .body {
                    configSaved(i18nContext)

                    div {
                        id = "section-config"

                        customGuildCommandTextEditor(
                            i18nContext,
                            guild,
                            request.label,
                            request.message,
                        )
                    }

                    hr {}

                    saveBar(
                        i18nContext,
                        false,
                        {
                            attributes["bliss-get"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/custom-commands/${commandId}"
                            attributes["bliss-swap:200"] = "#section-config (innerHTML) -> #section-config (innerHTML)"
                            attributes["bliss-headers"] = buildJsonObject {
                                put("Loritta-Configuration-Reset", "true")
                            }.toString()
                        }
                    ) {
                        attributes["bliss-put"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/custom-commands/${commandId}"
                        attributes["bliss-include-json"] = "#section-config"
                    }
                }
        )
    }
}