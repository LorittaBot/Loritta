package net.perfectdreams.loritta.commands.images

import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import net.perfectdreams.loritta.commands.images.declarations.SAMCommand
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.gabrielaimageserver.GabrielaImageServerClient
import net.perfectdreams.loritta.common.utils.gabrielaimageserver.executeAndHandleExceptions

class SAMExecutor(val emotes: Emotes, val client: GabrielaImageServerClient) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(SAMExecutor::class) {
        object Options : CommandOptions() {
            val type = string("type", LocaleKeyData("${SAMCommand.LOCALE_PREFIX}.selectLogo"))
                .choice("1", LocaleKeyData("${SAMCommand.LOCALE_PREFIX}.sam1"))
                .choice("2", LocaleKeyData("${SAMCommand.LOCALE_PREFIX}.sam2"))
                .choice("3", LocaleKeyData("${SAMCommand.LOCALE_PREFIX}.sam3"))
                .register()

            val imageReference = imageReference("image", LocaleKeyData("TODO_FIX_THIS"))
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val type = args[options.type]
        val imageReference = args[options.imageReference]

        val result = client.executeAndHandleExceptions(
            context,
            emotes,
            "/api/v1/images/sam/$type",
            buildJsonObject {
                putJsonArray("images") {
                    addJsonObject {
                        put("type", "url")
                        put("content", imageReference.url)
                    }
                }
            }
        )

        context.sendMessage {
            addFile("sam_logo.png", result)
        }
    }
}