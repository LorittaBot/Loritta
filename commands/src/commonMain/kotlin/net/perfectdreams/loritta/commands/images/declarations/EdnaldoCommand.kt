package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.EdnaldoBandeiraExecutor
import net.perfectdreams.loritta.commands.images.EdnaldoTvExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object EdnaldoCommand : CommandDeclaration {
    override fun declaration() = command(listOf("ednaldo"), CommandCategory.IMAGES, LocaleKeyData("TODO_FIX_THIS").toI18nHelper()) {

        subcommand(listOf("bandeira", "flag"), LocaleKeyData("commands.command.ednaldobandeira.description").toI18nHelper()) {
            executor = EdnaldoBandeiraExecutor
        }

        subcommand(listOf("tv"), LocaleKeyData("commands.command.ednaldotv.description").toI18nHelper()) {
            executor = EdnaldoTvExecutor
        }
    }
}