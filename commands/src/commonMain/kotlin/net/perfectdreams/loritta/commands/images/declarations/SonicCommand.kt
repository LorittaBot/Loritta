package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.i18nhelper.core.keys.StringI18nKey
import net.perfectdreams.loritta.commands.images.KnuxThrowExecutor
import net.perfectdreams.loritta.commands.images.ManiaTitleCardExecutor
import net.perfectdreams.loritta.commands.images.StudiopolisTvExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.i18n.I18nKeysData

object SonicCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Sonic

    override fun declaration() = command(listOf("sonic"), CommandCategory.IMAGES, StringI18nData(StringI18nKey("TODO_FIX_THIS"), emptyMap())
    ) {
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