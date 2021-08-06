package net.perfectdreams.loritta.commands.misc.declarations

import net.perfectdreams.loritta.commands.misc.KkEaeMenExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object KkEaeMenCommand : CommandDeclaration {
    override fun declaration() = command(listOf("kk"), CommandCategory.MISC, LocaleKeyData("commands.command.kkeaemen.description").toI18nHelper()) {

        subcommandGroup(listOf("eae"), LocaleKeyData("commands.command.kkeaemen.description").toI18nHelper()) {

            subcommand(listOf("men"), LocaleKeyData("commands.command.kkeaemen.description").toI18nHelper()) {
                executor = KkEaeMenExecutor
            }

            subcommand(listOf("girl"), LocaleKeyData("commands.command.kkeaemen.description").toI18nHelper()) {
                executor = KkEaeMenExecutor
            }
        }
    }
}