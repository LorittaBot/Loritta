package net.perfectdreams.loritta.commands.misc.declarations

import net.perfectdreams.loritta.commands.misc.KkEaeMenExecutor
import net.perfectdreams.loritta.commands.misc.PingAyayaExecutor
import net.perfectdreams.loritta.commands.misc.PingExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object KkEaeMenCommand : CommandDeclaration {
    override fun declaration() = command(listOf("kk"), CommandCategory.MISC) {
        description = LocaleKeyData("commands.command.kkeaemen.description")

        subcommandGroup(listOf("eae")) {
            description = LocaleKeyData("commands.command.kkeaemen.description")

            subcommand(listOf("men")) {
                description = LocaleKeyData("commands.command.kkeaemen.description")
                executor = KkEaeMenExecutor
            }

            subcommand(listOf("girl")) {
                description = LocaleKeyData("commands.command.kkeaemen.description")
                executor = KkEaeMenExecutor
            }
        }
    }
}