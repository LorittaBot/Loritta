package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.BolsoFrameExecutor
import net.perfectdreams.loritta.commands.images.Bolsonaro2Executor
import net.perfectdreams.loritta.commands.images.BolsonaroExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object BolsonaroCommand : CommandDeclaration {
    override fun declaration() = command(listOf("bolsonaro"), CommandCategory.IMAGES, LocaleKeyData("TODO_FIX_THIS").toI18nHelper()) {

        subcommand(listOf("tv"), LocaleKeyData("commands.command.bolsonaro.description").toI18nHelper()) {
            executor = BolsonaroExecutor
        }

        subcommand(listOf("tv2"), LocaleKeyData("commands.command.bolsonaro.description").toI18nHelper()) {
            executor = Bolsonaro2Executor
        }

        subcommand(listOf("frame"), LocaleKeyData("commands.command.bolsoframe.description").toI18nHelper()) {
            executor = BolsoFrameExecutor
        }
    }
}