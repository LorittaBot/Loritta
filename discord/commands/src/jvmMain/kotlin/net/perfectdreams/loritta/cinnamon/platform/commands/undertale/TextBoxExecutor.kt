package net.perfectdreams.loritta.cinnamon.platform.commands.undertale

import dev.kord.common.entity.ButtonStyle
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import net.perfectdreams.discordinteraktions.api.entities.User
import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.gabrielaimageserver.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.TextBoxHelper.textBoxTextOption
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.declarations.UndertaleCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.ChangeCharacterSelectMenuExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.ChangeColorPortraitTypeButtonClickExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.ChangeDialogBoxTypeButtonClickExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.ChangeUniverseSelectMenuExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.ColorPortraitType
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.ConfirmDialogBoxButtonClickExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.DialogBoxType
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.PortraitSelectMenuExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.SelectColorPortraitTypeData
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.SelectDialogBoxTypeData
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.SelectGenericData
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.TextBoxOptionsData
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.TextBoxWithCustomPortraitOptionsData
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.TextBoxWithGamePortraitOptionsData
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.TextBoxWithUniverseOptionsData
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.characters.CharacterType
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.characters.UniverseType
import net.perfectdreams.loritta.cinnamon.platform.components.interactiveButton
import net.perfectdreams.loritta.cinnamon.platform.components.loriEmoji
import net.perfectdreams.loritta.cinnamon.platform.components.selectMenu
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentDataUtils
import java.io.InputStream
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class TextBoxExecutor(val client: GabrielaImageServerClient) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(TextBoxExecutor::class) {
        object Options : CommandOptions() {
            val text = textBoxTextOption()
                .register()
        }

        override val options = Options

        @OptIn(ExperimentalTime::class)
        suspend fun createMessage(
            loritta: LorittaCinnamon,
            user: User,
            i18nContext: I18nContext,
            data: TextBoxOptionsData
        ): MessageBuilder.() -> (Unit) {
            val now = Clock.System.now()

            val interactionDataId = loritta.services.interactionsData.insertInteractionData(
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
                styled(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.CustomizeYourMessage), savePointBasedOnTheDialogBoxStyleType)

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

        suspend fun createDialogBox(
            client: GabrielaImageServerClient,
            data: TextBoxOptionsData
        ): InputStream {
            val text = data.text
            val type = data.dialogBoxType

            val result = client.execute(
                "/api/v1/images/toby-text-box",
                buildJsonObject {
                    put("type", type.name)

                    // Only put the portrait if the save data contains a Game Portrait
                    if (data is TextBoxWithGamePortraitOptionsData) {
                        val portrait = data.portrait
                        put("portrait", portrait)
                    } else if (data is TextBoxWithCustomPortraitOptionsData) {
                        // If it is a custom portrait, then add a image array!
                        putJsonArray("images") {
                            addJsonObject {
                                put("type", "url")
                                put("content", data.imageUrl)
                            }
                        }

                        put("colorPortraitType", data.colorPortraitType.name)
                    }

                    putJsonArray("strings") {
                        addJsonObject {
                            put("string", text)
                        }
                    }
                }
            )

            return result.inputStream()
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        context.deferChannelMessage() // Defer because we will create a image

        val text = args[Options.text]

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