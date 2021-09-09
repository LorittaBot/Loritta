package net.perfectdreams.loritta.cinnamon.commands.images.declarations

import net.perfectdreams.loritta.cinnamon.commands.images.KnuxThrowExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.ManiaTitleCardExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.StudiopolisTvExecutor
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

object SonicCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Sonic

    override fun declaration() = command(listOf("sonic"), CommandCategory.IMAGES, TodoFixThisData) {
        subcommand(
            listOf("knuxthrow", "knucklesthrow", "throwknux", "throwknuckles", "knucklesjogar", "knuxjogar", "jogarknuckles", "jogarknux"),
            I18N_PREFIX.Knuxthrow
                .Description
        ) {
            executor = KnuxThrowExecutor
        }

        subcommand(listOf("maniatitlecard"), I18N_PREFIX.Maniatitlecard.Description) {
            executor = ManiaTitleCardExecutor
        }

        subcommand(listOf("studiopolistv"), I18N_PREFIX.Studiopolistv.Description) {
            executor = StudiopolisTvExecutor
        }
    }
}