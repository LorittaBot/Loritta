package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.customcommands

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.util.getOrFail
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.CustomGuildCommands
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.customGuildCommands
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissCloseModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll

class DeleteCustomCommandsGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/custom-commands/{entryId}") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        val entryId = call.parameters.getOrFail("entryId").toLong()

        val result = website.loritta.transaction {
            val deletedCount = CustomGuildCommands.deleteWhere {
                CustomGuildCommands.guild eq guild.idLong and (CustomGuildCommands.id eq entryId)
            }

            if (deletedCount == 0)
                return@transaction Result.CommandNotFound

            val guildCommands = CustomGuildCommands.selectAll()
                .where {
                    CustomGuildCommands.guild eq guild.idLong
                }
                .toList()

            return@transaction Result.Success(guildCommands)
        }

        when (result) {
            is Result.Success -> {
                call.respondHtml(
                    createHTML()
                        .body {
                            customGuildCommands(i18nContext, guild, result.guildCommands)

                            blissCloseModal()

                            blissShowToast(
                                createEmbeddedToast(
                                    EmbeddedToast.Type.SUCCESS,
                                    "Comando deletado!"
                                )
                            )
                        },
                    status = HttpStatusCode.OK
                )
            }
            Result.CommandNotFound -> {
                call.respondHtml(
                    createHTML(false)
                        .body {
                            blissShowToast(
                                createEmbeddedToast(
                                    EmbeddedToast.Type.WARN,
                                    "Você não pode deletar um comando que não existe!"
                                )
                            )
                        },
                    status = HttpStatusCode.NotFound
                )
            }
        }
    }

    private sealed class Result {
        data class Success(val guildCommands: List<ResultRow>) : Result()
        data object CommandNotFound : Result()
    }
}