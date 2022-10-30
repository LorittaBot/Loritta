package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.entity.User
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.TobyTextBoxRequest
import net.perfectdreams.gabrielaimageserver.data.URLImageData
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.TextBoxHelper.textBoxTextOption
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.declarations.UndertaleCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.textbox.ChangeCharacterSelectMenuExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.textbox.ChangeColorPortraitTypeButtonClickExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.textbox.ChangeDialogBoxTypeButtonClickExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.textbox.ChangeUniverseSelectMenuExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.textbox.ColorPortraitType
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.textbox.ConfirmDialogBoxButtonClickExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.textbox.DialogBoxType
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.textbox.PortraitSelectMenuExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.textbox.SelectColorPortraitTypeData
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.textbox.SelectDialogBoxTypeData
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.textbox.SelectGenericData
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.textbox.TextBoxOptionsData
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.textbox.TextBoxWithCustomPortraitOptionsData
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.textbox.TextBoxWithGamePortraitOptionsData
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.textbox.TextBoxWithNoPortraitOptionsData
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.textbox.TextBoxWithUniverseOptionsData
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.textbox.characters.CharacterType
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.textbox.characters.UniverseType
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.interactiveButton
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.loriEmoji
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.selectMenu
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentDataUtils
import java.io.InputStream
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class TextBoxExecutor(loritta: LorittaBot, val client: GabrielaImageServerClient) :
    CinnamonSlashCommandExecutor(loritta) {
    companion object {
        @OptIn(ExperimentalTime::class)
        suspend fun createMessage(
            loritta: LorittaBot,
            user: User,
            i18nContext: I18nContext,
            data: TextBoxOptionsData
        ): MessageBuilder.() -> (Unit) {
            val now = Clock.System.now()

            val interactionDataId = loritta.pudding.interactionsData.insertInteractionData(
                Json.encodeToJsonElement<TextBoxOptionsData>(data).jsonObject,
                now,
                now + Duration.Companion.minutes(15) // Expires after 15m
            )

            val encodedComponent = ComponentDataUtils.encode(
                SelectGenericData(
                    user.id,
                    interactionDataId
                )
            )

            // I suppose that, if someday, DELTARUNE has save points outside of the Dark World, they won't be blue-ish
            // So let's do it based on the Dialog Box Style, not on the Universe Type!
            val savePointBasedOnTheDialogBoxStyleType = when (data.dialogBoxType) {
                DialogBoxType.ORIGINAL -> Emotes.UndertaleSavePoint
                DialogBoxType.DARK_WORLD -> Emotes.DeltaruneSavePoint
            }

            return {
                styled(
                    i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.CustomizeYourMessage),
                    savePointBasedOnTheDialogBoxStyleType
                )

                // ===[ UNIVERSE ]===
                if (data is TextBoxWithUniverseOptionsData) {
                    actionRow {
                        selectMenu(
                            ChangeUniverseSelectMenuExecutor,
                            encodedComponent
                        ) {
                            for (universe in UniverseType.values()) {
                                option(i18nContext.get(universe.universeName), universe.name) {
                                    default = data.universeType == universe
                                    if (universe.emote != null) // The "None" emote does not have any emotes
                                        loriEmoji = universe.emote
                                }
                            }
                        }
                    }
                }

                // ===[ CHANGE CHARACTER ]===
                // Those two options should only be available if the Universe is not "NONE"
                if (data is TextBoxWithGamePortraitOptionsData) {
                    actionRow {
                        selectMenu(
                            ChangeCharacterSelectMenuExecutor,
                            encodedComponent
                        ) {
                            CharacterType.values()
                                .filter { it.universe == data.universeType }
                                .sortedBy { it.name }
                                .forEach {
                                    option(i18nContext.get(it.charName), it.name) {
                                        default = it == data.character
                                        if (it.emote != null)
                                            loriEmoji = it.emote
                                    }
                                }
                        }
                    }

                    // ===[ CHANGE PORTRAIT ]===
                    actionRow {
                        selectMenu(PortraitSelectMenuExecutor, encodedComponent) {
                            data.character.data.menuOptions(i18nContext, data.portrait, this)
                        }
                    }
                }

                // ===[ BUTTONS ]===
                actionRow {
                    if (data.dialogBoxType == DialogBoxType.ORIGINAL) {
                        interactiveButton(
                            ButtonStyle.Secondary,
                            ChangeDialogBoxTypeButtonClickExecutor,
                            ComponentDataUtils.encode(
                                SelectDialogBoxTypeData(
                                    user.id,
                                    DialogBoxType.DARK_WORLD,
                                    interactionDataId
                                )
                            )
                        ) {
                            loriEmoji = Emotes.DarkWorldBox
                            label = i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.DarkWorldDialogBox.Name)
                        }
                    } else {
                        interactiveButton(
                            ButtonStyle.Secondary,
                            ChangeDialogBoxTypeButtonClickExecutor,
                            ComponentDataUtils.encode(
                                SelectDialogBoxTypeData(
                                    user.id,
                                    DialogBoxType.ORIGINAL,
                                    interactionDataId
                                )
                            )
                        ) {
                            loriEmoji = Emotes.OriginalBox
                            label = i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.OriginalDialogBox.Name)
                        }
                    }

                    // If it is a TextBoxWithCustomPortraitOptionsData, we will allow the user to change the portrait color type (sweet!)
                    //
                    // Did you know that Lightners have black and white portraits, while Darkners have colored portraits?
                    if (data is TextBoxWithCustomPortraitOptionsData) {
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
                    }

                    interactiveButton(
                        ButtonStyle.Success,
                        ConfirmDialogBoxButtonClickExecutor,
                        encodedComponent
                    ) {
                        loriEmoji = savePointBasedOnTheDialogBoxStyleType
                        label = i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Confirm)
                    }
                }
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

    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val text = textBoxTextOption()
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage() // Defer because we will create a image

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
            context.user,
            context.i18nContext,
            data
        )

        val dialogBox = createDialogBox(client, data)
        context.sendMessage {
            addFile("undertale_box.gif", dialogBox)
            apply(builtMessage)
        }
    }
}