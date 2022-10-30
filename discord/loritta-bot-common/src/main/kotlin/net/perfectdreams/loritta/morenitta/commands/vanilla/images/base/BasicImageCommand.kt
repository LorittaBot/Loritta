package net.perfectdreams.loritta.morenitta.commands.vanilla.images.base

import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.api.commands.CommandBuilder
import net.perfectdreams.loritta.morenitta.api.commands.CommandContext

open class BasicImageCommand(
    loritta: LorittaBot,
    labels: List<String>,
    val descriptionKey: String,
    val sourceTemplatePath: String,
    val builder: CommandBuilder<CommandContext>.() -> (Unit),
    val slashCommandName: String? = null
) : ImageAbstractCommandBase(
    loritta,
    labels
) {
    override fun command() = create {
        localizedDescription(descriptionKey)

        usage {
            argument(ArgumentType.IMAGE) {}
        }

        needsToUploadFiles = true

        builder.invoke(this)
    }
}