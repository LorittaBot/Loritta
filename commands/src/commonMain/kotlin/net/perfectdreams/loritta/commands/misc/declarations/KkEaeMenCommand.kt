package net.perfectdreams.loritta.commands.misc.declarations

import net.perfectdreams.loritta.commands.misc.KkEaeMenExecutor
import net.perfectdreams.loritta.commands.misc.PingAyayaExecutor
import net.perfectdreams.loritta.commands.misc.PingExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object KkEaeMenCommand : CommandDeclaration {
    override fun declaration() = command(listOf("kk")) {
        description = LocaleKeyData("TODO_FIX_THIS")

        subcommandGroup(listOf("eae")) {
            description = LocaleKeyData("TODO_FIX_THIS")

            subcommand(listOf("men")) {
                description = LocaleKeyData("TODO_FIX_THIS")
                executor = KkEaeMenExecutor
            }

            subcommand(listOf("girl")) {
                description = LocaleKeyData("TODO_FIX_THIS")
                executor = KkEaeMenExecutor
            }
        }
    }
}