package net.perfectdreams.loritta.commands.misc.declarations

import net.perfectdreams.loritta.commands.misc.PingAyayaExecutor
import net.perfectdreams.loritta.commands.misc.PingExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object PingCommand : CommandDeclaration {
    override fun declaration() = command(listOf("ping"), CommandCategory.MISC, LocaleKeyData("commands.command.ping.description").toI18nHelper()) {
        executor = PingExecutor

        subcommand(listOf("ayaya"), LocaleKeyData("commands.command.ping.description").toI18nHelper()) {

            executor = PingAyayaExecutor
        }
    }
}