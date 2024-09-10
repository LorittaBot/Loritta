package net.perfectdreams.loritta.morenitta.interactions.vanilla.undertale

import dev.minn.jda.ktx.interactions.components.SelectOption
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.TobyTextBoxRequest
import net.perfectdreams.gabrielaimageserver.data.URLImageData
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.components.ComponentContext
import net.perfectdreams.loritta.morenitta.interactions.vanilla.undertale.textbox.*
import net.perfectdreams.loritta.morenitta.interactions.vanilla.undertale.textbox.characters.CharacterType
import net.perfectdreams.loritta.morenitta.interactions.vanilla.undertale.textbox.characters.UniverseType
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import java.io.InputStream
import java.util.*

class UndertaleCommand(val loritta: LorittaBot, val gabrielaImageServerClient: GabrielaImageServerClient) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Undertale
        val I18N_TEXTBOX_PREFIX = I18nKeysData.Commands.Command.Undertale.Textbox

        suspend fun createMessage(
            loritta: LorittaBot,
            gabrielaImageServerClient: GabrielaImageServerClient,
            user: User,
            i18nContext: I18nContext,
            textBoxOptionsData: TextBoxOptionsData
        ): InlineMessage<*>.() -> (Unit) {
            // I suppose that, if someday, DELTARUNE has save points outside of the Dark World, they won't be blue-ish
            // So let's do it based on the Dialog Box Style, not on the Universe Type!
            val savePointBasedOnTheDialogBoxStyleType = when (textBoxOptionsData.dialogBoxType) {
                DialogBoxType.ORIGINAL -> Emotes.UndertaleSavePoint
                DialogBoxType.DARK_WORLD -> Emotes.DeltaruneSavePoint
            }

            return {
                styled(i18nContext.get(I18N_TEXTBOX_PREFIX.CustomizeYourMessage), savePointBasedOnTheDialogBoxStyleType)

                // ===[ UNIVERSE ]===
                if (textBoxOptionsData is TextBoxWithUniverseOptionsData) {
                    actionRow(
                        loritta.interactivityManager.stringSelectMenuForUser(
                            user,
                            {
                                for (universe in UniverseType.entries) {
                                    addOptions(SelectOption(i18nContext.get(universe.universeName), universe.name, null, universe.emote?.toJDA(), textBoxOptionsData.universeType == universe))
                                }
                            }
                        ) { context, values ->
                            // We will already defer to avoid issues
                            // Also because we want to edit the message with a file... later!
                            val deferredEdit = context.deferEdit()

                            val newUniverse = UniverseType.valueOf(values.first())
                            val newData = if (newUniverse == UniverseType.NONE) {
                                TextBoxWithNoPortraitOptionsData(
                                    textBoxOptionsData.text,
                                    textBoxOptionsData.dialogBoxType
                                )
                            } else {
                                TextBoxWithGamePortraitOptionsData(
                                    textBoxOptionsData.text,
                                    textBoxOptionsData.dialogBoxType,
                                    newUniverse,
                                    character = when (newUniverse) {
                                        UniverseType.DELTARUNE -> CharacterType.DELTARUNE_RALSEI
                                        UniverseType.UNDERTALE -> CharacterType.UNDERTALE_TORIEL
                                        else -> error("Trying to select UniverseType.NONE while we are using TextBoxWithGamePortraitOptionsData!")
                                    },
                                    portrait = when (newUniverse) {
                                        UniverseType.DELTARUNE -> "deltarune/ralsei/neutral"
                                        // This is a workaround, if the UniverseType is "None", the character is not used at all
                                        UniverseType.UNDERTALE -> "undertale/toriel/neutral"
                                        else -> error("Trying to select UniverseType.NONE while we are using TextBoxWithGamePortraitOptionsData!")
                                    },
                                )
                            }

                            val builtMessage = createMessage(
                                context.loritta,
                                gabrielaImageServerClient,
                                context.user,
                                context.i18nContext,
                                newData
                            )

                            val dialogBox = gabrielaImageServerClient.handleExceptions(context) { createDialogBox(gabrielaImageServerClient, newData) }

                            deferredEdit.editOriginal(
                                MessageEdit {
                                    files += FileUpload.fromData(dialogBox, "undertale_box.gif")
                                    apply(builtMessage)
                                }
                            ).await()
                        }
                    )
                }

                // ===[ CHANGE CHARACTER ]===
                // Those two options should only be available if the Universe is not "NONE"
                if (textBoxOptionsData is TextBoxWithGamePortraitOptionsData) {
                    actionRow(
                        loritta.interactivityManager.stringSelectMenuForUser(
                            user,
                            {
                                CharacterType.entries
                                    .filter { it.universe == textBoxOptionsData.universeType }
                                    .sortedBy { it.name }
                                    .forEach {
                                        addOptions(SelectOption(i18nContext.get(it.charName), it.name, null, it.emote?.toJDA(), it == textBoxOptionsData.character))
                                    }
                            }
                        ) { context, values ->
                            // We will already defer to avoid issues
                            // Also because we want to edit the message with a file... later!
                            val deferredEdit = context.deferEdit()

                            val character = CharacterType.valueOf(values.first())
                            val newData = textBoxOptionsData.copy(
                                character = character,
                                portrait = character.defaultKeyName
                            )

                            val builtMessage = createMessage(
                                context.loritta,
                                gabrielaImageServerClient,
                                context.user,
                                context.i18nContext,
                                newData
                            )

                            val dialogBox = gabrielaImageServerClient.handleExceptions(context) { createDialogBox(gabrielaImageServerClient, newData) }

                            deferredEdit.editOriginal(
                                MessageEdit {
                                    files += FileUpload.fromData(dialogBox, "undertale_box.gif")
                                    apply(builtMessage)
                                }
                            ).await()
                        }
                    )

                    // ===[ CHANGE PORTRAIT ]===
                    actionRow(
                        loritta.interactivityManager.stringSelectMenuForUser(
                            user,
                            {
                                textBoxOptionsData.character.data.menuOptions(i18nContext, textBoxOptionsData.portrait, this)
                            }
                        ) { context, values ->
                            // We will already defer to avoid issues
                            // Also because we want to edit the message with a file... later!
                            val deferredEdit = context.deferEdit()

                            val newData = textBoxOptionsData.copy(portrait = values.first())

                            val builtMessage = createMessage(
                                context.loritta,
                                gabrielaImageServerClient,
                                context.user,
                                context.i18nContext,
                                newData
                            )

                            val dialogBox = gabrielaImageServerClient.handleExceptions(context) { createDialogBox(gabrielaImageServerClient, newData) }

                            deferredEdit.editOriginal(
                                MessageEdit {
                                    files += FileUpload.fromData(dialogBox, "undertale_box.gif")
                                    apply(builtMessage)
                                }
                            ).await()
                        }
                    )
                }

                // ===[ BUTTONS ]===
                val changeDialogBoxBlock: suspend (context: ComponentContext, dialogBoxType: DialogBoxType) -> (Unit) = { context, dialogBoxType ->
                    // We will already defer to avoid issues
                    // Also because we want to edit the message with a file... later!
                    val deferredEdit = context.deferEdit()

                    val newData = when (textBoxOptionsData) {
                        is TextBoxWithCustomPortraitOptionsData -> textBoxOptionsData.copy(dialogBoxType = dialogBoxType)
                        is TextBoxWithGamePortraitOptionsData -> textBoxOptionsData.copy(dialogBoxType = dialogBoxType)
                        is TextBoxWithNoPortraitOptionsData -> textBoxOptionsData.copy(dialogBoxType = dialogBoxType)
                    }

                    val builtMessage = createMessage(
                        context.loritta,
                        gabrielaImageServerClient,
                        context.user,
                        context.i18nContext,
                        newData
                    )

                    val dialogBox = gabrielaImageServerClient.handleExceptions(context) {
                        createDialogBox(gabrielaImageServerClient, newData)
                    }

                    deferredEdit.editOriginal(
                        MessageEdit {
                            files += FileUpload.fromData(dialogBox, "undertale_box.gif")
                            apply(builtMessage)
                        }
                    ).await()
                }

                actionRow(
                    if (textBoxOptionsData.dialogBoxType == DialogBoxType.ORIGINAL) {
                        loritta.interactivityManager.buttonForUser(
                            user,
                            ButtonStyle.SECONDARY,
                            i18nContext.get(I18N_TEXTBOX_PREFIX.DarkWorldDialogBox.Name),
                            {
                                loriEmoji = Emotes.DarkWorldBox
                            }
                        ) { context ->
                            changeDialogBoxBlock.invoke(context, DialogBoxType.DARK_WORLD)
                        }
                    } else {
                        loritta.interactivityManager.buttonForUser(
                            user,
                            ButtonStyle.SECONDARY,
                            i18nContext.get(I18N_TEXTBOX_PREFIX.OriginalDialogBox.Name),
                            {
                                loriEmoji = Emotes.OriginalBox
                            }
                        ) { context ->
                            changeDialogBoxBlock.invoke(context, DialogBoxType.ORIGINAL)
                        }
                    },

                    // If it is a TextBoxWithCustomPortraitOptionsData, we will allow the user to change the portrait color type (sweet!)
                    //
                    // Did you know that Lightners have black and white portraits, while Darkners have colored portraits?
                    // TODO: Fix this!!!
                    /* if (textBoxOptionsData is TextBoxWithCustomPortraitOptionsData) {
                        val newColor = when (data.colorPortraitType) {
                            ColorPortraitType.COLORED -> ColorPortraitType.BLACK_AND_WHITE
                            ColorPortraitType.BLACK_AND_WHITE -> ColorPortraitType.SHADES_OF_GRAY
                            ColorPortraitType.SHADES_OF_GRAY -> ColorPortraitType.COLORED
                        }

                        interactiveButton(
                            ButtonStyle.Secondary,
                            ChangeColorPortraitTypeButtonClickExecutor,
                            ComponentDataUtils.encode(
                                SelectColorPortraitTypeData(
                                    user.id,
                                    newColor,
                                    interactionDataId
                                )
                            )
                        ) {
                            loriEmoji = when (newColor) {
                                ColorPortraitType.COLORED -> Emotes.LoriColored
                                ColorPortraitType.BLACK_AND_WHITE -> Emotes.LoriBlackAndWhite
                                ColorPortraitType.SHADES_OF_GRAY -> Emotes.LoriGrayscale
                            }

                            label = i18nContext.get(
                                when (newColor) {
                                    ColorPortraitType.COLORED -> UndertaleCommand.I18N_TEXTBOX_PREFIX.Colored
                                    ColorPortraitType.BLACK_AND_WHITE -> UndertaleCommand.I18N_TEXTBOX_PREFIX.BlackAndWhite
                                    ColorPortraitType.SHADES_OF_GRAY -> UndertaleCommand.I18N_TEXTBOX_PREFIX.Grayscale
                                }
                            )
                        }
                    } */

                    loritta.interactivityManager.buttonForUser(
                        user,
                        ButtonStyle.SUCCESS,
                        i18nContext.get(I18N_TEXTBOX_PREFIX.Confirm),
                        {
                            loriEmoji = savePointBasedOnTheDialogBoxStyleType
                        }
                    ) { context ->
                        context.editMessage(
                            false,
                            MessageEditBuilder()
                                .setContent("")
                                .setComponents(listOf())
                                .build()
                        )
                    }
                )
            }
        }

        // Everything that uses this should wrap the call in a "context.handleExceptions"!
        suspend fun createDialogBox(
            client: GabrielaImageServerClient,
            data: TextBoxOptionsData
        ): InputStream {
            val text = data.text
            val type = data.dialogBoxType

            val result = when (data) {
                is TextBoxWithGamePortraitOptionsData -> {
                    client.images.tobyTextBox(
                        TobyTextBoxRequest(
                            text,
                            TobyTextBoxRequest.TextBoxType.valueOf(type.name),
                            data.portrait
                        )
                    )
                }
                is TextBoxWithCustomPortraitOptionsData -> {
                    client.images.tobyTextBox(
                        TobyTextBoxRequest(
                            text,
                            TobyTextBoxRequest.TextBoxType.valueOf(type.name),
                            image = URLImageData(data.imageUrl),
                            colorPortraitType = TobyTextBoxRequest.ColorPortraitType.valueOf(data.colorPortraitType.name)
                        )
                    )
                }
                is TextBoxWithNoPortraitOptionsData -> {
                    client.images.tobyTextBox(
                        TobyTextBoxRequest(
                            text,
                            TobyTextBoxRequest.TextBoxType.valueOf(type.name),
                        )
                    )
                }
            }

            return result.inputStream()
        }
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, TodoFixThisData, CommandCategory.UNDERTALE, UUID.fromString("78453b96-c6de-47ba-ac2f-07d1e3484d78")) {
        subcommandGroup(I18N_TEXTBOX_PREFIX.Label, I18N_TEXTBOX_PREFIX.Description) {
            subcommand(I18N_TEXTBOX_PREFIX.LabelGame, I18N_TEXTBOX_PREFIX.DescriptionGame, UUID.fromString("91d02e5b-1347-4ff5-b91f-fca73fd65f2a")) {
                executor = TextBoxExecutor(loritta, gabrielaImageServerClient)
            }

            subcommand(I18N_TEXTBOX_PREFIX.LabelCustom, I18N_TEXTBOX_PREFIX.DescriptionCustom, UUID.fromString("61743312-8cb2-40dd-92dc-aa1edca64c02")) {
                executor = CustomTextBoxExecutor(loritta, gabrielaImageServerClient)
            }
        }
    }

    class TextBoxExecutor(val loritta: LorittaBot, val gabrielaImageServerClient: GabrielaImageServerClient) : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val text = string("text", I18N_TEXTBOX_PREFIX.Options.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false) // Defer because we will create a image

            val text = args[options.text]

            val data = TextBoxWithGamePortraitOptionsData(
                text,
                DialogBoxType.DARK_WORLD,
                UniverseType.DELTARUNE,
                CharacterType.DELTARUNE_RALSEI,
                "deltarune/ralsei/neutral"
            )

            val builtMessage = createMessage(
                context.loritta,
                gabrielaImageServerClient,
                context.user,
                context.i18nContext,
                data
            )

            val dialogBox = createDialogBox(gabrielaImageServerClient, data)
            context.reply(false) {
                files += FileUpload.fromData(dialogBox, "undertale_box.gif")
                apply(builtMessage)
            }
        }
    }

    class CustomTextBoxExecutor(val loritta: LorittaBot, val gabrielaImageServerClient: GabrielaImageServerClient) : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val text = string("text", I18N_TEXTBOX_PREFIX.Options.Text)
            val imageReference = imageReferenceOrAttachment("image", TodoFixThisData)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false) // Defer because we will create an image

            val imageReferenceUrl = args[options.imageReference].get(context)
            val text = args[options.text]

            val data = TextBoxWithCustomPortraitOptionsData(
                text,
                DialogBoxType.DARK_WORLD,
                imageReferenceUrl,
                ColorPortraitType.COLORED
            )

            val builtMessage = createMessage(
                context.loritta,
                gabrielaImageServerClient,
                context.user,
                context.i18nContext,
                data
            )

            val dialogBox = createDialogBox(gabrielaImageServerClient, data)
            context.reply(false) {
                files += FileUpload.fromData(dialogBox, "undertale_box.gif")
                apply(builtMessage)
            }
        }
    }
}