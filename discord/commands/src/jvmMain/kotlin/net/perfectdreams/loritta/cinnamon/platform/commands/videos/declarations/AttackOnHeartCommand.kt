package net.perfectdreams.loritta.cinnamon.platform.commands.videos.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.videos.AttackOnHeartExecutor

object AttackOnHeartCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Attackonheart

    override fun declaration() = slashCommand(listOf("attackonheart"), CommandCategory.VIDEOS, I18N_PREFIX.Description) {
        executor = AttackOnHeartExecutor
    }
}