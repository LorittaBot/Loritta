package net.perfectdreams.loritta.cinnamon.platform.webserver.webserver.routes.api.v1.cinnamon

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.webserver.LorittaCinnamonWebServer
import net.perfectdreams.loritta.cinnamon.platform.webserver.utils.InteractionCommand
import net.perfectdreams.loritta.cinnamon.platform.webserver.utils.InteractionCommandGroup
import net.perfectdreams.sequins.ktor.BaseRoute

class GetCommandsRoute(val m: LorittaCinnamonWebServer) : BaseRoute("/api/v1/cinnamon/commands") {
    override suspend fun onRequest(call: ApplicationCall) {
        call.respondText(
            Json.encodeToString(
                m.commandManager.commandManager.declarationWrappers.filterIsInstance<SlashCommandDeclarationWrapper>().map { it.declaration() }.map {
                    InteractionCommand(
                        listOf(it.name),
                        it.description,
                        it.category,
                        it.executor?.parent?.let { m.commandManager.commandManager.executors.first { executor -> executor::class == it }::class.simpleName }?.toString(),
                        it.subcommandGroups.map {
                            InteractionCommandGroup(
                                listOf(it.name),
                                it.subcommands.map {
                                    InteractionCommand(
                                        listOf(it.name),
                                        it.description,
                                        it.category,
                                        it.executor?.parent?.let { m.commandManager.commandManager.executors.first { executor -> executor::class == it }::class.simpleName }?.toString(),
                                        listOf(),
                                        listOf()
                                    )
                                }
                            )
                        },
                        it.subcommands.map {
                            InteractionCommand(
                                listOf(it.name),
                                it.description,
                                it.category,
                                it.executor?.parent?.let { m.commandManager.commandManager.executors.first { executor -> executor::class == it }::class.simpleName }?.toString(),
                                listOf(),
                                listOf()
                            )
                        }
                    )
                }
            ),
            ContentType.Application.Json
        )
    }
}