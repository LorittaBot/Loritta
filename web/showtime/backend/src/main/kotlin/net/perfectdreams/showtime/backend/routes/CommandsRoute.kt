package net.perfectdreams.showtime.backend.routes

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.typesafe.config.ConfigFactory
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import kotlinx.serialization.json.Json
import net.perfectdreams.dokyo.RoutePath
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.CommandInfo
import net.perfectdreams.showtime.backend.ShowtimeBackend
import net.perfectdreams.showtime.backend.utils.SimpleImageInfo
import net.perfectdreams.showtime.backend.utils.commands.AdditionalCommandInfoConfigs
import net.perfectdreams.showtime.backend.utils.userTheme
import net.perfectdreams.showtime.backend.views.CommandsView

class CommandsRoute(val showtime: ShowtimeBackend) : LocalizedRoute(showtime, RoutePath.COMMANDS) {
    val commands: List<CommandInfo> by lazy {
        Json.decodeFromString<List<CommandInfo>>(
            ShowtimeBackend::class.java.getResourceAsStream("/commands/default.json")!!
                .readAllBytes()
                .toString(Charsets.UTF_8)
        )
    }

    override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
        try {
            val additionalCommandInfo = ConfigFactory.parseString(
                // Workaround because HOCON can't deserialize root lists (sad)
                "additionalCommandInfos=" + ShowtimeBackend::class.java.getResourceAsStream("/commands/commands-info.conf")!!
                    .readAllBytes()
                    .toString(Charsets.UTF_8)
            ).resolve()

            // Workaround because HOCON can't deserialize root lists (sad)
            val config = Hocon.decodeFromConfig<AdditionalCommandInfoConfigs>(additionalCommandInfo)

            val imageSizes = mutableMapOf<String, Pair<Int, Int>>()

            for (imageUrl in config.additionalCommandInfos.flatMap { it.imageUrls ?: listOf() }) {
                println("/static$imageUrl")
                val info = SimpleImageInfo(CommandsRoute::class.java.getResourceAsStream("/static$imageUrl"))
                imageSizes[imageUrl] = Pair(info.width, info.height)
            }

            call.respondText(
                CommandsView(
                    call.request.userTheme,
                    showtime.svgIconManager,
                    showtime.hashManager,
                    locale,
                    "/commands",
                    commands,
                    call.parameters["category"]?.toUpperCase()?.let {
                        try {
                            CommandCategory.valueOf(it)
                        } catch (e: Throwable) {
                            null
                        }
                    },
                    config.additionalCommandInfos,
                    imageSizes
                ).generateHtml(),
                ContentType.Text.Html
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}