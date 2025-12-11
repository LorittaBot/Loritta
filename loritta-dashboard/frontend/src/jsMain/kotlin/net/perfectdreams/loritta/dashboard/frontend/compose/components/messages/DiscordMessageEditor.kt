package net.perfectdreams.loritta.dashboard.frontend.compose.components.messages

import androidx.compose.runtime.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.div
import kotlinx.html.stream.createHTML
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.bliss.Bliss
import net.perfectdreams.bliss.HttpMethod
import net.perfectdreams.loritta.dashboard.discord.DiscordGuild
import net.perfectdreams.loritta.dashboard.discordmessages.DiscordComponent
import net.perfectdreams.loritta.dashboard.discordmessages.DiscordEmbed
import net.perfectdreams.loritta.dashboard.discordmessages.DiscordMessage
import net.perfectdreams.loritta.dashboard.discordmessages.RenderableDiscordUser
import net.perfectdreams.loritta.dashboard.frontend.DiscordMessageEditorUtils
import net.perfectdreams.loritta.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.dashboard.frontend.compose.components.*
import net.perfectdreams.loritta.dashboard.frontend.compose.components.colorpicker.Color
import net.perfectdreams.loritta.dashboard.frontend.compose.components.colorpicker.ColorPicker
import net.perfectdreams.loritta.dashboard.frontend.modals.Modal
import net.perfectdreams.loritta.dashboard.frontend.toasts.Toast
import net.perfectdreams.loritta.dashboard.frontend.utils.Details
import net.perfectdreams.loritta.dashboard.frontend.utils.SVGIconManager
import net.perfectdreams.loritta.dashboard.frontend.utils.Summary
import net.perfectdreams.loritta.dashboard.messageeditor.LorittaMessageTemplate
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorMessagePlaceholderGroup
import net.perfectdreams.loritta.dashboard.renderer.discordMessageRenderer
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Code
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Table
import org.jetbrains.compose.web.dom.Tbody
import org.jetbrains.compose.web.dom.Td
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextArea
import org.jetbrains.compose.web.dom.TextInput
import org.jetbrains.compose.web.dom.Th
import org.jetbrains.compose.web.dom.Thead
import org.jetbrains.compose.web.dom.Tr
import kotlin.random.Random

val JsonForDiscordMessages = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
}

@Composable
fun DiscordMessageEditor(
    m: LorittaDashboardFrontend,
    templates: List<LorittaMessageTemplate>,
    placeholderGroups: List<MessageEditorMessagePlaceholderGroup>,
    targetGuild: DiscordGuild,
    testMessageEndpoint: String,
    targetChannel: TargetChannelResult,
    renderableSelfUser: RenderableDiscordUser,
    verifiedIconRawHtml: SVGIconManager.SVGIcon,
    eyeDropperIconRawHtml: SVGIconManager.SVGIcon,
    chevdronDownIconRawHtml: SVGIconManager.SVGIcon,
    rawMessage: String,
    onMessageContentChange: (String) -> (Unit)
) {
    var editorType by remember { mutableStateOf(EditorType.INTERACTIVE) }

    Div(attrs = {
        classes("message-editor")
    }) {
        val parsedMessage = try {
            JsonForDiscordMessages.decodeFromString<DiscordMessage>(rawMessage)
        } catch (e: SerializationException) {
            null
        } catch (e: IllegalStateException) {
            null // This may be triggered when a message has invalid message components
        } catch (e: IllegalArgumentException) {
            null // This may be triggered when a message has invalid message components²
        }

        val mutableMessage = MutableDiscordMessage(
            parsedMessage ?: DiscordMessage(
                content = rawMessage
            ),
            onMessageContentChange
        )

        Div(attrs = {
            classes("message-editor-buttons")
        }) {
            DiscordButton(
                DiscordButtonType.PRIMARY,
                attrs = {
                    if (templates.isNotEmpty()) {
                        onClick {
                            m.modalManager.openModalWithOnlyCloseButton(
                                "Templates de Mensagens",
                                Modal.Size.SMALL,
                            ) { templatesModal ->
                                Text("Sem criatividade? Então pegue um template!")

                                VerticalList {
                                    for (template in templates) {
                                        DiscordButton(
                                            DiscordButtonType.PRIMARY,
                                            attrs = {
                                                onClick {
                                                    m.modalManager.openModalWithCloseButton(
                                                        "Você realmente quer substituir?",
                                                        Modal.Size.MEDIUM,
                                                        {
                                                            Text(
                                                                "Ao aplicar o template, a sua mensagem atual será perdida! A não ser se você tenha copiado ela para outro lugar, aí vida que segue né."
                                                            )
                                                        },
                                                        { modal ->
                                                            DiscordButton(
                                                                DiscordButtonType.PRIMARY,
                                                                attrs = {
                                                                    onClick {
                                                                        onMessageContentChange.invoke(template.content)
                                                                        templatesModal.close()
                                                                        modal.close()
                                                                        m.toastManager.showToast(
                                                                            Toast.Type.SUCCESS,
                                                                            "Template importado!"
                                                                        )
                                                                    }
                                                                }
                                                            ) {
                                                                Text("Aplicar")
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                        ) {
                                            Text(template.name)
                                        }
                                    }
                                }
                            }
                        }
                    } else disabled()
                }
            ) {
                Text("Template de Mensagens")
            }

            Div {
                DiscordButton(
                    DiscordButtonType.PRIMARY,
                    attrs = {
                        onClick {
                            m.modalManager.openModalWithOnlyCloseButton(
                                "Importar",
                                Modal.Size.SMALL,
                            ) { importModal ->
                                Text("Qual mensagem você deseja importar?")

                                VerticalList {
                                    DiscordButton(
                                        DiscordButtonType.PRIMARY,
                                        attrs = {
                                            onClick {
                                                var embed by mutableStateOf<DiscordEmbed?>(null)

                                                m.modalManager.openModalWithCloseButton(
                                                    "Embed do Carl-bot (Embed em JSON)",
                                                    Modal.Size.MEDIUM,
                                                    {
                                                        TextArea {
                                                            onInput {
                                                                embed = try {
                                                                    JsonForDiscordMessages.decodeFromString<DiscordEmbed>(
                                                                        it.value
                                                                    )
                                                                } catch (e: SerializationException) {
                                                                    // If the embed couldn't be deserialized, set it to null!
                                                                    null
                                                                }
                                                            }
                                                        }
                                                    },
                                                    { modal ->
                                                        DiscordButton(
                                                            DiscordButtonType.PRIMARY,
                                                            attrs = {
                                                                if (embed == null)
                                                                    disabled()
                                                                else
                                                                    onClick {
                                                                        onMessageContentChange.invoke(
                                                                            JsonForDiscordMessages.encodeToString(
                                                                                DiscordMessage(
                                                                                    "",
                                                                                    embed
                                                                                )
                                                                            )
                                                                        )
                                                                        importModal.close()
                                                                        modal.close()
                                                                        m.toastManager.showToast(
                                                                            Toast.Type.SUCCESS,
                                                                            "Mensagem importada!"
                                                                        )
                                                                    }
                                                            }
                                                        ) {
                                                            Text("Importar")
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    ) {
                                        Text("Embed do Carl-bot (Embed em JSON)")
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Text("Importar")
                }
            }

            Div {
                DiscordButton(
                    DiscordButtonType.PRIMARY,
                    attrs = {
                        onClick {
                            editorType = when (editorType) {
                                EditorType.INTERACTIVE -> EditorType.RAW
                                EditorType.RAW -> EditorType.INTERACTIVE
                            }
                        }
                    }
                ) {
                    Text("Alterar modo de edição")
                }
            }

            Div {
                DiscordButton(
                    DiscordButtonType.PRIMARY,
                    attrs = {
                        if (targetChannel is TargetChannelResult.ChannelNotSelected) {
                            disabled()
                        } else {
                            onClick {
                                GlobalScope.launch {
                                    m.toastManager.showToast(Toast.Type.INFO, "Enviando mensagem...")

                                    Bliss.executeAjax(
                                        null,
                                        HttpMethod.Post,
                                        testMessageEndpoint,
                                        mapOf(),
                                        null,
                                        null,
                                        mapOf(),
                                        mapOf(
                                            "channelId" to when (targetChannel) {
                                                TargetChannelResult.ChannelNotSelected -> error("Should NEVER happen!")
                                                TargetChannelResult.DirectMessageTarget -> JsonNull
                                                is TargetChannelResult.GuildMessageChannelTarget -> JsonPrimitive(targetChannel.id)
                                            },
                                            "message" to JsonPrimitive(rawMessage),
                                            "placeholders" to buildJsonObject {
                                                for (placeholderGroup in placeholderGroups) {
                                                    for (placeholder in placeholderGroup.placeholders) {
                                                        put(placeholder.name, placeholderGroup.replaceWithBackend)
                                                    }
                                                }
                                            }
                                        ),
                                        mapOf(),
                                        listOf(),
                                        listOf()
                                    )
                                }
                            }
                        }
                    }
                ) {
                    Text("Testar Mensagem")
                }
            }

            Div(attrs = {
                classes("change-message-preview-direction")
            }) {
                DiscordButton(
                    DiscordButtonType.PRIMARY,
                    {
                        onClick {
                            DiscordMessageEditorUtils.messageEditorRenderDirection = when (DiscordMessageEditorUtils.messageEditorRenderDirection) {
                                DiscordMessageEditorUtils.RenderDirection.VERTICAL -> DiscordMessageEditorUtils.RenderDirection.HORIZONTAL
                                DiscordMessageEditorUtils.RenderDirection.HORIZONTAL -> DiscordMessageEditorUtils.RenderDirection.VERTICAL
                            }
                        }
                    }
                ) {
                    when (DiscordMessageEditorUtils.messageEditorRenderDirection) {
                        DiscordMessageEditorUtils.RenderDirection.VERTICAL -> Text("Visualização na Horizontal")
                        DiscordMessageEditorUtils.RenderDirection.HORIZONTAL -> Text("Visualização na Vertical")
                    }
                }
            }

            Div {
                DiscordButton(
                    DiscordButtonType.PRIMARY,
                    attrs = {
                        // Automatically disable the format JSON button if you are editing it raw
                        if (parsedMessage != null && editorType == EditorType.RAW) {
                            onClick {
                                onMessageContentChange.invoke(JsonForDiscordMessages.encodeToString(parsedMessage))
                            }
                        } else disabled()
                    }
                ) {
                    Text("Formatar JSON")
                }
            }
        }

        Div(attrs = {
            classes("message-textarea-and-preview")

            if (DiscordMessageEditorUtils.messageEditorRenderDirection == DiscordMessageEditorUtils.RenderDirection.VERTICAL)
                classes("vertical-render")
        }) {
            when (editorType) {
                EditorType.INTERACTIVE -> {
                    VerticalList {
                        FieldWrapper {
                            FieldInformation {
                                FieldLabel("Conteúdo da Mensagem")
                            }

                            TextAreaWithEntityPickers(targetGuild, mutableMessage.content) {
                                mutableMessage.content = it
                                mutableMessage.triggerUpdate()
                            }
                        }

                        @Composable
                        fun CreateEmbedButton() {
                            DiscordButton(
                                DiscordButtonType.PRIMARY,
                                attrs = {
                                    onClick {
                                        mutableMessage.embed = MutableDiscordMessage.MutableDiscordEmbed(
                                            DiscordEmbed(
                                                description = "A Loritta é muito fofa!"
                                            )
                                        )
                                        mutableMessage.triggerUpdate()
                                    }
                                }
                            ) {
                                Text("Adicionar Embed")
                            }
                        }

                        @Composable
                        fun CreateActionRowButton() {
                            DiscordButton(
                                DiscordButtonType.PRIMARY,
                                attrs = {
                                    if (mutableMessage.components.size >= 5)
                                        disabled()
                                    else
                                        onClick {
                                            mutableMessage.components.add(
                                                MutableDiscordMessage.MutableDiscordComponent.MutableActionRow(
                                                    DiscordComponent.DiscordActionRow(components = listOf())
                                                )
                                            )
                                            mutableMessage.triggerUpdate()
                                        }
                                }
                            ) {
                                Text("Adicionar Linha de Botões (${mutableMessage.components.size}/5)")
                            }
                        }

                        val embed = mutableMessage.embed

                        // We check for the components to make it look BETTER, putting two buttons side by side
                        if (embed == null) {
                            if (mutableMessage.components.isNotEmpty()) {
                                // If an embed is NOT present...
                                // Add embed button
                                CreateEmbedButton()
                            }
                        } else {
                            VerticalList(attrs = {
                                val embedColor = embed.color
                                val hex = if (embedColor != null) {
                                    Color(embedColor).toHex()
                                } else "#e3e5e8"

                                attr("style", "border: 1px solid var(--card-border-color);\n" +
                                        "  border-radius: var(--nice-border-radius);\n" +
                                        "  padding: 1em; border-left: 4px solid $hex;\n")
                            }) {
                                FieldWrapper {
                                    FieldInformation {
                                        FieldLabel("Cor")
                                    }

                                    ColorPicker(
                                        m,
                                        verifiedIconRawHtml,
                                        eyeDropperIconRawHtml,
                                        embed.color?.let {
                                            val red = it shr 16 and 0xFF
                                            val green = it shr 8 and 0xFF
                                            val blue = it and 0xFF

                                            Color(red, green, blue)
                                        }
                                    ) {
                                        mutableMessage.embed?.color = it?.rgb
                                        mutableMessage.triggerUpdate()
                                    }
                                }

                                FieldWrapper {
                                    FieldInformation {
                                        FieldLabel("Nome do Autor")
                                    }

                                    TextInput(embed.author?.name ?: "") {
                                        onInput {
                                            val author = embed.author
                                            if (author != null) {
                                                val newValue = it.value.ifEmpty { null }
                                                if (newValue == null) {
                                                    if (author.url != null || author.iconUrl != null) {
                                                        // If the author text is null BUT there's an icon or URL set, tell the user that they must delete both before deleting the text
                                                        m.toastManager.showToast(Toast.Type.WARN, "Embed Inválida") {
                                                            text("Você não pode ter um ícone ou URL de autor sem ter um texto! Apague o ícone e a URL antes de deletar o texto do autor.")
                                                        }
                                                        // TODO (bliss-dash): Fix the sound effect!
                                                        // m.soundEffects.error.play(1.0)
                                                        return@onInput
                                                    }
                                                    embed.author = null
                                                } else {
                                                    author.name = it.value.ifEmpty { null }
                                                }
                                            } else {
                                                embed.author = MutableDiscordMessage.MutableDiscordEmbed.MutableAuthor(
                                                    DiscordEmbed.Author(
                                                        it.value,
                                                        null,
                                                        null
                                                    )
                                                )
                                            }

                                            mutableMessage.triggerUpdate()
                                        }
                                    }
                                }

                                FieldWrapper {
                                    FieldInformation {
                                        FieldLabel("URL do Autor")
                                    }

                                    TextInput(embed.author?.url ?: "") {
                                        if (embed.author?.name == null)
                                            disabled()
                                        else {
                                            onInput {
                                                val author = embed.author
                                                // Should NEVER be null here, but smart cast ain't that smart to figure this out...
                                                author?.url = it.value.ifEmpty { null }

                                                mutableMessage.triggerUpdate()
                                            }
                                        }
                                    }
                                }

                                FieldWrapper {
                                    FieldInformation {
                                        FieldLabel("URL do Ícone do Autor")
                                    }

                                    TextInput(embed.author?.iconUrl ?: "") {
                                        if (embed.author?.name == null)
                                            disabled()
                                        else {
                                            onInput {
                                                val author = embed.author
                                                // Should NEVER be null here, but smart cast ain't that smart to figure this out...
                                                author?.iconUrl = it.value.ifEmpty { null }

                                                mutableMessage.triggerUpdate()
                                            }
                                        }
                                    }
                                }

                                FieldWrapper {
                                    FieldInformation {
                                        FieldLabel("Título")
                                    }

                                    TextAreaWithEntityPickers(targetGuild, embed.title ?: "") {
                                        embed.title = it.ifEmpty { null }
                                        mutableMessage.triggerUpdate()
                                    }
                                }

                                FieldWrapper {
                                    FieldInformation {
                                        FieldLabel("URL do Título")
                                    }

                                    TextInput(embed.url ?: "") {
                                        onInput {
                                            embed.url = it.value.ifEmpty { null }
                                            mutableMessage.triggerUpdate()
                                        }
                                    }
                                }

                                FieldWrapper {
                                    FieldInformation {
                                        FieldLabel("Descrição")
                                    }

                                    TextAreaWithEntityPickers(targetGuild, embed.description ?: "") {
                                        embed.description = it.ifEmpty { null }
                                        mutableMessage.triggerUpdate()
                                    }
                                }

                                FieldWrapper {
                                    FieldInformation {
                                        FieldLabel("Fields")
                                    }

                                    VerticalList(attrs = {
                                        attr(
                                            "style", "border: 1px solid var(--card-border-color);\n" +
                                                    "  border-radius: var(--nice-border-radius);\n" +
                                                    "  padding: 1em;"
                                        )
                                    }) {
                                        for ((index, field) in embed.fields.withIndex()) {
                                            FieldWrappers(attrs = {
                                                attr(
                                                    "style", "border: 1px solid var(--card-border-color);\n" +
                                                            "  border-radius: var(--nice-border-radius);\n" +
                                                            "  padding: 1em;"
                                                )
                                            }) {
                                                FieldInformation {
                                                    FieldLabel("Field ${index + 1}")
                                                }
                                                Div {
                                                    FieldInformation {
                                                        FieldLabel("Nome")
                                                    }

                                                    TextAreaWithEntityPickers(targetGuild, field.name) {
                                                        field.name = it
                                                        mutableMessage.triggerUpdate()
                                                    }
                                                }

                                                Div {
                                                    FieldInformation {
                                                        FieldLabel("Valor")
                                                    }

                                                    TextAreaWithEntityPickers(targetGuild, field.value) {
                                                        field.value = it
                                                        mutableMessage.triggerUpdate()
                                                    }
                                                }

                                                DiscordToggle(
                                                    "inline-field-$index",
                                                    "Field Inline",
                                                    null,
                                                    field.inline,
                                                    onChange = { newValue ->
                                                        field.inline = newValue
                                                        mutableMessage.triggerUpdate()
                                                    }
                                                )

                                                DiscordButton(
                                                    DiscordButtonType.DANGER,
                                                    attrs = {
                                                        onClick {
                                                            embed.fields.removeAt(index)
                                                            mutableMessage.triggerUpdate()
                                                        }
                                                    }
                                                ) {
                                                    Text("Remover Field")
                                                }
                                            }
                                        }

                                        DiscordButton(
                                            DiscordButtonType.PRIMARY,
                                            attrs = {
                                                // TODO - bliss-dash: Fix this!
                                                if (/* DiscordResourceLimits.Embed.FieldsPerEmbed */ 25 > embed.fields.size) {
                                                    onClick {
                                                        embed.fields.add(
                                                            MutableDiscordMessage.MutableDiscordEmbed.MutableField(
                                                                DiscordEmbed.Field(
                                                                    "Loritta Morenitta",
                                                                    "Ela é muito fofa!",
                                                                    true
                                                                )
                                                            )
                                                        )
                                                        mutableMessage.triggerUpdate()
                                                    }
                                                } else {
                                                    disabled()
                                                }
                                            }
                                        ) {
                                            Text("Adicionar Field")
                                        }
                                    }
                                }

                                FieldWrapper {
                                    FieldInformation {
                                        FieldLabel("URL da Imagem")
                                    }

                                    TextInput(embed.imageUrl ?: "") {
                                        onInput {
                                            embed.imageUrl = it.value.ifEmpty { null }
                                            mutableMessage.triggerUpdate()
                                        }
                                    }
                                }

                                FieldWrapper {
                                    FieldInformation {
                                        FieldLabel("URL da Thumbnail")
                                    }

                                    TextInput(embed.thumbnailUrl ?: "") {
                                        onInput {
                                            embed.thumbnailUrl = it.value.ifEmpty { null }
                                            mutableMessage.triggerUpdate()
                                        }
                                    }
                                }

                                val mutableFooter = embed.footer
                                FieldWrapper {
                                    FieldInformation {
                                        FieldLabel("Texto do Rodapé")
                                    }

                                    TextInput(embed.footer?.text ?: "") {
                                        onInput {
                                            val footer = embed.footer
                                            if (footer != null) {
                                                val newValue = it.value.ifEmpty { null }
                                                if (newValue == null) {
                                                    if (footer.iconUrl != null) {
                                                        // If the footer text is null BUT there's an icon set, tell the user that they must delete the icon before deleting the text
                                                        m.toastManager.showToast(Toast.Type.WARN, "Embed Inválida") {
                                                            text("Você não pode ter um ícone de rodapé sem ter um texto! Apague o ícone antes de deletar o texto do rodapé.")
                                                        }
                                                        // TODO (bliss-dash): Fix the sound effect!
                                                        // m.soundEffects.error.play(1.0)
                                                        return@onInput
                                                    }
                                                    embed.footer = null
                                                } else {
                                                    footer.text = it.value.ifEmpty { null }
                                                }
                                            } else {
                                                embed.footer = MutableDiscordMessage.MutableDiscordEmbed.MutableFooter(
                                                    DiscordEmbed.Footer(
                                                        it.value,
                                                        null
                                                    )
                                                )
                                            }

                                            mutableMessage.triggerUpdate()
                                        }
                                    }
                                }

                                FieldWrapper {
                                    FieldInformation {
                                        FieldLabel("URL do Ícone do Rodapé")
                                    }

                                    TextInput(embed.footer?.iconUrl ?: "") {
                                        // Only allow setting the Icon URL if the footer text is present
                                        if (mutableFooter?.text == null)
                                            disabled()
                                        else {
                                            onInput {
                                                val footer = embed.footer
                                                // Should NEVER be null here, but smart cast ain't that smart to figure this out...
                                                footer?.iconUrl = it.value.ifEmpty { null }

                                                mutableMessage.triggerUpdate()
                                            }
                                        }
                                    }
                                }

                                // Remove embed button
                                DiscordButton(
                                    DiscordButtonType.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT,
                                    attrs = {
                                        onClick {
                                            mutableMessage.embed = null
                                            mutableMessage.triggerUpdate()
                                        }
                                    }
                                ) {
                                    Text("Remover Embed")
                                }
                            }
                        }

                        VerticalList {
                            val components = mutableMessage.components

                            for (component in components) {
                                ComponentEditor(m, null, component, mutableMessage)
                            }

                            if (embed == null && components.isEmpty()) {
                                HorizontalList(attrs = {
                                    classes("child-flex-grow")
                                }) {
                                    CreateEmbedButton()
                                    CreateActionRowButton()
                                }
                            } else {
                                CreateActionRowButton()
                            }
                        }
                    }
                }

                EditorType.RAW -> {
                    VerticalList {
                        FieldWrapper {
                            FieldInformation {
                                FieldLabel("Conteúdo da Mensagem em JSON")
                            }

                            TextArea(rawMessage) {
                                onInput {
                                    onMessageContentChange.invoke(it.value)
                                }
                            }

                            Div {
                                // Before we render the message as a normal message, we will check if the user *tried* to do a JSON message
                                // We check if it starts AND ends with {} because we don't want to trigger checks in "{@user} hello"
                                if (parsedMessage == null && (rawMessage.startsWith("{") && rawMessage.endsWith("}"))) {
                                    Div {
                                        Text("Você tentou fazer uma mensagem em JSON? Se sim, tem algo errado nela!")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            val rId = Random.nextLong()

            Div(attrs = {
                classes("message-preview-section")
            }) {
                FieldWrapper {
                    FieldInformation {
                        FieldLabel("Pré-visualização da Mensagem")
                    }

                    Div(attrs = {
                        id("message-preview-wrapper-$rId")
                        classes("message-preview-wrapper")
                    }) {
                        Div(attrs = {
                            id("message-preview-$rId")
                            classes("message-preview")
                        }) {
                            if (parsedMessage != null) {
                                RawHtml(createHTML(false).div {
                                    discordMessageRenderer(
                                        renderableSelfUser,
                                        parsedMessage,
                                        null,
                                        verifiedIconRawHtml.rawHtml.toString(),
                                        targetGuild.channels,
                                        targetGuild.roles,
                                        placeholderGroups
                                    )
                                })
                            } else {
                                // If the message couldn't be parsed, render it as a normal message
                                RawHtml(createHTML(false).div {
                                    discordMessageRenderer(
                                        renderableSelfUser,
                                        DiscordMessage(
                                            content = rawMessage
                                        ),
                                        null,
                                        verifiedIconRawHtml.rawHtml.toString(),
                                        targetGuild.channels,
                                        targetGuild.roles,
                                        placeholderGroups
                                    )
                                })
                            }
                        }
                    }
                }
            }
        }

        // TODO: Fix this! (do we need this in here? can't we render it on the backend?
        Details(attrs = {
            classes("fancy-details")
        }) {
            Summary {
                Text("Quais variáveis/placeholders eu posso usar?")

                Div(attrs = { classes("chevron-icon") }) {
                    SVGIcon(chevdronDownIconRawHtml) {
                        setAttribute("style", "width: 100%; height: 100%;")
                    }
                }
            }

            Div(attrs = {
                classes("details-content")
                attr("style", "overflow: auto;")}) {

                Table(attrs = {
                    // Make the table always use all available width
                    attr("style", "width: 100%;")
                }) {
                    Thead {
                        Th {
                            Text("Placeholder")
                        }

                        Th {
                            Text("Valor")
                        }

                        Th {
                            Text("Descrição")
                        }
                    }

                    Tbody {
                        for (placeholderGroup in placeholderGroups) {
                            val anyVisible = placeholderGroup.placeholders.any { !it.hidden }

                            if (anyVisible) {
                                Tr {
                                    Td {
                                        var isFirst = true
                                        for (placeholder in placeholderGroup.placeholders.filter { !it.hidden }) {
                                            if (!isFirst)
                                                Text(", ")

                                            Code {
                                                Text(placeholder.asKey)
                                            }

                                            isFirst = false
                                        }
                                    }

                                    Td {
                                        Text(placeholderGroup.replaceWithFrontend)
                                    }

                                    Td {
                                        val description = placeholderGroup.description

                                        if (description != null)
                                            Text(description)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionRowEditor(m: LorittaDashboardFrontend, component: MutableDiscordMessage.MutableDiscordComponent.MutableActionRow, message: MutableDiscordMessage) {
    FieldWrapper {
        FieldInformation {
            FieldLabel("Linha de Botões")
        }

        VerticalList(attrs = {
            attr(
                "style", "border: 1px solid var(--card-border-color);\n" +
                        "  border-radius: var(--nice-border-radius);\n" +
                        "  padding: 1em;"
            )
        }) {
            for (childComponent in component.components) {
                Div(attrs = {
                    attr(
                        "style", "border: 1px solid var(--card-border-color);\n" +
                                "  border-radius: var(--nice-border-radius);\n" +
                                "  padding: 1em;"
                    )
                }) {
                    ComponentEditor(m, component, childComponent, message)
                }
            }

            HorizontalList(attrs = {
                classes("child-flex-grow")
            }) {
                DiscordButton(
                    DiscordButtonType.PRIMARY,
                    attrs = {
                        if (component.components.size >= 5)
                            disabled()
                        else
                            onClick {
                                component.components.add(
                                    MutableDiscordMessage.MutableDiscordComponent.MutableButton(
                                        DiscordComponent.DiscordButton(
                                            label = "Website da Loritta",
                                            style = 5,
                                            url = "https://loritta.website/"
                                        )
                                    )
                                )
                                message.triggerUpdate()
                            }
                    }
                ) {
                    Text("Adicionar Botão (${component.components.size}/5)")
                }

                DiscordButton(
                    DiscordButtonType.DANGER,
                    attrs = {
                        onClick {
                            message.components.remove(component)
                            message.triggerUpdate()
                        }
                    }
                ) {
                    Text("Remover Linha")
                }
            }
        }
    }
}

@Composable
fun ButtonEditor(parentComponent: MutableDiscordMessage.MutableDiscordComponent.MutableActionRow, component: MutableDiscordMessage.MutableDiscordComponent.MutableButton, message: MutableDiscordMessage) {
    VerticalList {
        FieldWrapper {
            FieldInformation {
                FieldLabel("Label")
            }

            TextInput(component.label ?: "") {
                onInput {
                    component.label = it.value.ifEmpty { null }
                    message.triggerUpdate()
                }
            }
        }

        FieldWrapper {
            FieldInformation {
                FieldLabel("URL")
            }

            TextInput(component.url ?: "") {
                onInput {
                    component.url = it.value.ifEmpty { null }
                    message.triggerUpdate()
                }
            }
        }

        DiscordButton(
            DiscordButtonType.DANGER,
            attrs = {
                onClick {
                    parentComponent.components.remove(component)
                    message.triggerUpdate()
                }
            }
        ) {
            Text("Remover Botão")
        }
    }
}

@Composable
fun ComponentEditor(
    m: LorittaDashboardFrontend,
    parentComponent: MutableDiscordMessage.MutableDiscordComponent.MutableActionRow?,
    component: MutableDiscordMessage.MutableDiscordComponent,
    message: MutableDiscordMessage
) {
    when (component) {
        is MutableDiscordMessage.MutableDiscordComponent.MutableActionRow -> ActionRowEditor(m, component, message)
        is MutableDiscordMessage.MutableDiscordComponent.MutableButton -> ButtonEditor(
            parentComponent ?: error("Button on the root component is not allowed!"), component, message
        )
    }
}

sealed class TargetChannelResult {
    class GuildMessageChannelTarget(val id: Long) : TargetChannelResult()
    data object DirectMessageTarget : TargetChannelResult()
    data object ChannelNotSelected : TargetChannelResult()
}

// TODO - bliss-dash: Fix this!
/* data class DiscordMessageWithAuthor(
    val author: RenderableDiscordUser,
    val message: DiscordMessage
) */

enum class EditorType {
    INTERACTIVE,
    RAW
}