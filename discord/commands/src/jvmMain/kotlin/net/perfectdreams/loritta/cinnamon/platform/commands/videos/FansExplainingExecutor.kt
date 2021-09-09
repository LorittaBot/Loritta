package net.perfectdreams.loritta.cinnamon.platform.commands.videos

import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import net.perfectdreams.loritta.cinnamon.platform.commands.images.gabrielaimageserver.executeAndHandleExceptions
import net.perfectdreams.loritta.cinnamon.platform.commands.videos.declarations.FansExplainingCommand
import net.perfectdreams.loritta.cinnamon.common.utils.gabrielaimageserver.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions

class FansExplainingExecutor(val client: GabrielaImageServerClient) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(FansExplainingExecutor::class) {
        object Options : CommandOptions() {
            val section1Line1 = string("section1_line1", FansExplainingCommand.I18N_PREFIX.Options.Section1Line1)
                .register()
            val section1Line2 = string("section1_line2", FansExplainingCommand.I18N_PREFIX.Options.Section1Line2)
                .register()

            val section2Line1 = string("section2_line1", FansExplainingCommand.I18N_PREFIX.Options.Section2Line1)
                .register()
            val section2Line2 = string("section2_line2", FansExplainingCommand.I18N_PREFIX.Options.Section2Line2)
                .register()

            val section3Line1 = string("section3_line1", FansExplainingCommand.I18N_PREFIX.Options.Section3Line1)
                .register()
            val section3Line2 = string("section3_line2", FansExplainingCommand.I18N_PREFIX.Options.Section3Line2)
                .register()

            val section4Line1 = string("section4_line1", FansExplainingCommand.I18N_PREFIX.Options.Section4Line1)
                .register()
            val section4Line2 = string("section4_line2", FansExplainingCommand.I18N_PREFIX.Options.Section4Line2)
                .register()

            val section5Line1 = string("section5_line1", FansExplainingCommand.I18N_PREFIX.Options.Section5Line1)
                .register()
            val section5Line2 = string("section5_line2", FansExplainingCommand.I18N_PREFIX.Options.Section5Line2)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        context.deferChannelMessage() // Defer message because image manipulation is kinda heavy

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

        val result = client.executeAndHandleExceptions(
            context,
            "/api/v1/videos/fans-explaining",
            buildJsonObject {
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
            }
        )

        context.sendMessage {
            addFile("fans_explaining.mp4", result.inputStream())
        }
    }
}