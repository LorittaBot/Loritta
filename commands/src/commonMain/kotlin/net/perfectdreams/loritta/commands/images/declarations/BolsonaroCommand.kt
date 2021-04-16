package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.BolsoFrameExecutor
import net.perfectdreams.loritta.commands.images.Bolsonaro2Executor
import net.perfectdreams.loritta.commands.images.BolsonaroExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object BolsonaroCommand : CommandDeclaration {
    override fun declaration() = command(listOf("bolsonaro")) {
        description = LocaleKeyData("TODO_FIX_THIS")

        subcommand(listOf("tv")) {
            description = LocaleKeyData("commands.command.bolsonaro.description")
            executor = BolsonaroExecutor
        }

        subcommand(listOf("tv2")) {
            description = LocaleKeyData("commands.command.bolsonaro.description")
            executor = Bolsonaro2Executor
        }

        subcommand(listOf("frame")) {
            description = LocaleKeyData("commands.command.bolsoframe.description")
            executor = BolsoFrameExecutor
        }
    }
}