package net.perfectdreams.showtime.backend.routes

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.typesafe.config.ConfigFactory
import io.ktor.server.application.*
import io.ktor.server.html.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import kotlinx.serialization.json.Json
import net.perfectdreams.dokyo.RoutePath
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.CommandInfo
import net.perfectdreams.showtime.backend.ShowtimeBackend
import net.perfectdreams.showtime.backend.utils.commands.AdditionalCommandInfoConfigs
import net.perfectdreams.showtime.backend.utils.userTheme
import net.perfectdreams.showtime.backend.views.LegacyCommandsView

class LegacyCommandsRoute(val showtime: ShowtimeBackend) : LocalizedRoute(showtime, RoutePath.LEGACY_COMMANDS) {
    val commands: List<CommandInfo> by lazy {
        Json.decodeFromString<List<CommandInfo>>(
            ShowtimeBackend::class.java.getResourceAsStream("/commands/default.json")!!
                .readAllBytes()
                .toString(Charsets.UTF_8)
        )
    }

    override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext) {
        try {
            val additionalCommandInfo = ConfigFactory.parseString(
                // Workaround because HOCON can't deserialize root lists (sad)
                "additionalCommandInfos=" + ShowtimeBackend::class.java.getResourceAsStream("/commands/commands-info.conf")!!
                    .readAllBytes()
                    .toString(Charsets.UTF_8)
            ).resolve()

            // Workaround because HOCON can't deserialize root lists (sad)
            val config = Hocon.decodeFromConfig<AdditionalCommandInfoConfigs>(additionalCommandInfo)

            call.respondHtml(
                block = LegacyCommandsView(
                    showtime,
                    call.request.userTheme,
                    locale,
                    i18nContext,
                    "/commands/legacy",
                    commands,
                    call.parameters["category"]?.toUpperCase()?.let {
                        try {
                            CommandCategory.valueOf(it)
                        } catch (e: Throwable) {
                            null
                        }
                    },
                    config.additionalCommandInfos
                ).generateHtml()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}