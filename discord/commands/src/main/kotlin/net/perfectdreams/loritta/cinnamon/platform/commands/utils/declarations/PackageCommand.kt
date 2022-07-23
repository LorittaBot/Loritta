package net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations

import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.packtracker.PackageListExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.packtracker.TrackPackageExecutor

object PackageCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Package

    override fun declaration() = slashCommand(listOf("package"), CommandCategory.UTILS, TodoFixThisData) {
        subcommand(listOf("track"), I18N_PREFIX.Track.Description) {
            executor = TrackPackageExecutor
        }

        subcommand(listOf("list"), I18N_PREFIX.List.Description) {
            executor = PackageListExecutor
        }
    }
}