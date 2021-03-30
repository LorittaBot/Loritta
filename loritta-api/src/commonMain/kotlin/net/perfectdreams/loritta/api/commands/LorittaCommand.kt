package net.perfectdreams.loritta.api.commands

import net.perfectdreams.loritta.api.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.utils.locale.LocaleKeyData

/**
 * This is the class that should be inherited if you
 * want to create a Slash Command.
 *
 * It's recommended that the [declaration] parameter be
 * the class' companion object that extends [SlashCommandDeclaration].
 */
abstract class LorittaCommand<T>(
    val declaration: CommandDeclaration,
    val rootDeclaration: CommandDeclaration = declaration
) {
    companion object {
        val MISSING_DESCRIPTION_KEY = LocaleKeyData("commands.missingDescription")
        val SINGLE_IMAGE_EXAMPLES_KEY = LocaleKeyData("commands.category.images.singleImageExamples")
        val TWO_IMAGES_EXAMPLES_KEY = LocaleKeyData("commands.category.images.twoImagesExamples")
    }

    /**
     * This is the method that'll be called when this command
     * is executed, even if some of the options're not matched (you should handle this kind of error)
     *
     * @param context The context including the command executor, the channel, guild, etc...
     */
    abstract suspend fun executes(context: T)
}