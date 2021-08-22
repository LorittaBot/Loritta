package net.perfectdreams.loritta.commands.videos.declarations

import net.perfectdreams.loritta.commands.videos.AttackOnHeartExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.utils.TodoFixThisData

object AttackOnHeartCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.attackonheart"

    override fun declaration() = command(listOf("attackonheart"), CommandCategory.VIDEOS, TodoFixThisData) {
        executor = AttackOnHeartExecutor
    }
}