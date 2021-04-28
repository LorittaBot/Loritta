package net.perfectdreams.loritta.commands.videos

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import net.perfectdreams.loritta.commands.images.declarations.ManiaTitleCardCommand
import net.perfectdreams.loritta.commands.videos.declarations.FansExplainingCommand
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.locale.LocaleKeyData

class FansExplainingExecutor(val http: HttpClient) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(FansExplainingExecutor::class) {
        object Options : CommandOptions() {
            val section1Line1 = string("section1_line1", LocaleKeyData("${FansExplainingCommand.LOCALE_PREFIX}.selectSection1Line1"))
                .register()
            val section1Line2 = string("section1_line2", LocaleKeyData("${FansExplainingCommand.LOCALE_PREFIX}.selectSection1Line2"))
                .register()

            val section2Line1 = string("section2_line1", LocaleKeyData("${FansExplainingCommand.LOCALE_PREFIX}.selectSection2Line1"))
                .register()
            val section2Line2 = string("section2_line2", LocaleKeyData("${FansExplainingCommand.LOCALE_PREFIX}.selectSection2Line2"))
                .register()

            val section3Line1 = string("section3_line1", LocaleKeyData("${FansExplainingCommand.LOCALE_PREFIX}.selectSection3Line1"))
                .register()
            val section3Line2 = string("section3_line2", LocaleKeyData("${FansExplainingCommand.LOCALE_PREFIX}.selectSection3Line2"))
                .register()

            val section4Line1 = string("section4_line1", LocaleKeyData("${FansExplainingCommand.LOCALE_PREFIX}.selectSection4Line1"))
                .register()
            val section4Line2 = string("section4_line2", LocaleKeyData("${FansExplainingCommand.LOCALE_PREFIX}.selectSection4Line2"))
                .register()

            val section5Line1 = string("section5_line1", LocaleKeyData("${FansExplainingCommand.LOCALE_PREFIX}.selectSection5Line1"))
                .register()
            val section5Line2 = string("section5_line2", LocaleKeyData("${FansExplainingCommand.LOCALE_PREFIX}.selectSection5Line2"))
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val section1Line1 = args[options.section1Line1]
        val section1Line2 = args[options.section1Line2]

        val section2Line1 = args[options.section2Line1]
        val section2Line2 = args[options.section2Line2]

        val section3Line1 = args[options.section3Line1]
        val section3Line2 = args[options.section3Line2]

        val section4Line1 = args[options.section4Line1]
        val section4Line2 = args[options.section4Line2]

        val section5Line1 = args[options.section5Line1]
        val section5Line2 = args[options.section5Line2]

        val response = http.post<HttpResponse>("https://gabriela.loritta.website/api/v1/videos/fans-explaining") {
            body = buildJsonObject {
                putJsonArray("strings") {
                    addJsonObject {
                        put("string", section1Line1)
                    }
                    addJsonObject {
                        put("string", section1Line2)
                    }
                    addJsonObject {
                        put("string", section2Line1)
                    }
                    addJsonObject {
                        put("string", section2Line2)
                    }
                    addJsonObject {
                        put("string", section3Line1)
                    }
                    addJsonObject {
                        put("string", section3Line2)
                    }
                    addJsonObject {
                        put("string", section4Line1)
                    }
                    addJsonObject {
                        put("string", section4Line2)
                    }
                    addJsonObject {
                        put("string", section5Line1)
                    }
                    addJsonObject {
                        put("string", section5Line2)
                    }
                }
            }.toString()
        }

        println(response.status)

        val result = response.receive<ByteArray>()
        context.sendMessage {
            addFile("fans_explaining.mp4", result)
        }
    }
}