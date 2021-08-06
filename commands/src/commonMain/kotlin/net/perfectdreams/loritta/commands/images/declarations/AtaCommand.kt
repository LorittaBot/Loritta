package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.ChicoAtaExecutor
import net.perfectdreams.loritta.commands.images.GessyAtaExecutor
import net.perfectdreams.loritta.commands.images.LoriAtaExecutor
import net.perfectdreams.loritta.commands.images.MonicaAtaExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object AtaCommand : CommandDeclaration {
    override fun declaration() = command(listOf("ata"), CommandCategory.IMAGES, LocaleKeyData("TODO_FIX_THIS").toI18nHelper()) {

        subcommand(listOf("monica"), LocaleKeyData("commands.command.ata.description").toI18nHelper()) {
            executor = MonicaAtaExecutor
        }

        subcommand(listOf("chico"), LocaleKeyData("commands.command.chicoata.description").toI18nHelper()) {
            executor = ChicoAtaExecutor
        }

        subcommand(listOf("lori"), LocaleKeyData("commands.command.loriata.description").toI18nHelper()) {
            executor = LoriAtaExecutor
        }

        subcommand(listOf("gessy"), LocaleKeyData("commands.command.gessyata.description").toI18nHelper()) {
            executor = GessyAtaExecutor
        }
    }
}