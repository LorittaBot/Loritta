package net.perfectdreams.loritta.cinnamon.platform.commands.fortnite.declarations

import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.fortnite.FortniteNewsExecutor

object FortniteCommand : CommandDeclaration {
    override fun declaration() = command(listOf("fortnite"), CommandCategory.FORTNITE, TodoFixThisData) {
        subcommand(listOf("news"), TodoFixThisData) {
            executor = FortniteNewsExecutor
        }
    }
}