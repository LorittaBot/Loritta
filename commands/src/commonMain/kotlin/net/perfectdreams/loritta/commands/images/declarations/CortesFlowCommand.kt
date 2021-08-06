package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.CortesFlowExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object CortesFlowCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.cortesflow"
    val lists = listOf(
        "arthur-benozzati-smile",
        "douglas-laughing",
        "douglas-pointing",
        "douglas-pray",
        "gaules-sad",
        "igor-angry",
        "igor-naked",
        "igor-pointing",
        "julio-cocielo-eyes",
        "lucas-inutilismo-exalted",
        "metaforando-badge",
        "metaforando-surprised",
        "mitico-succ",
        "monark-discussion",
        "monark-smoking",
        "monark-stop",
        "peter-jordan-action-figure",
        "poladoful-discussion",
        "rato-borrachudo-disappointed",
        "rato-borrachudo-no-glasses"
    )

    override fun declaration() = command(listOf("cortesflow"), CommandCategory.IMAGES, LocaleKeyData("${LOCALE_PREFIX}.description").toI18nHelper()) {
        executor = CortesFlowExecutor
    }
}