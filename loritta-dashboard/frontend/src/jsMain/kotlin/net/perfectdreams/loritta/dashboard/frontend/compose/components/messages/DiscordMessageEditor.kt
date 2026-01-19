package net.perfectdreams.loritta.dashboard.frontend.compose.components.messages

import androidx.compose.runtime.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.div
import kotlinx.html.stream.createHTML
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.luna.bliss.Bliss
import net.perfectdreams.luna.bliss.HttpMethod
import net.perfectdreams.luna.modals.Modal
import net.perfectdreams.luna.toasts.Toast
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
import net.perfectdreams.loritta.dashboard.frontend.utils.Details
import net.perfectdreams.loritta.dashboard.frontend.utils.SVGIconManager
import net.perfectdreams.loritta.dashboard.frontend.utils.Summary
import net.perfectdreams.loritta.dashboard.messageeditor.LorittaMessageTemplate
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorMessagePlaceholderGroup
import net.perfectdreams.loritta.dashboard.renderer.discordMessageRenderer
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.B
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
        val parsedMessage = DiscordMessage.decodeFromJsonString(rawMessage)

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
                                                                    DiscordMessage.JsonForDiscordMessages.decodeFromString<DiscordEmbed>(
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
                                                                            DiscordMessage.JsonForDiscordMessages.encodeToString(
                                                                                DiscordMessage(
                                                                                    "",
                                                                                    false,
                                                                                    embed?.let { listOf(it) }
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
                                onMessageContentChange.invoke(DiscordMessage.JsonForDiscordMessages.encodeToString(parsedMessage))
                            }
                        } else disabled()
                    }
                ) {
                    Text("Formatar JSON")
                }
            }
        }

        // Mode selector (Normal vs Components V2)
        FieldWrapper {
            FieldInformation { FieldLabel("Modo de Mensagem") }

            VerticalList {
                FancyRadioInput(
                    name = "message-mode",
                    value = "normal",
                    checked = !mutableMessage.isComponentsV2(),
                    onChange = {
                        m.modalManager.openModalWithCloseButton(
                            "Alterar Modo de Mensagem",
                            Modal.Size.MEDIUM,
                            {
                                Div {
                                    Text("Você deseja alterar para o modo Normal?")
                                }
                                Div {
                                    B {
                                        Text("Atenção!")
                                    }

                                    Text(" ")

                                    Text("Esta operação irá apagar a sua mensagem atual!")
                                }
                            },
                            { modal ->
                                DiscordButton(DiscordButtonType.DANGER, attrs = {
                                    onClick {
                                        mutableMessage.content = ""
                                        mutableMessage.components.clear()
                                        mutableMessage.setComponentsV2Mode(false)
                                        mutableMessage.triggerUpdate()
                                        modal.close()
                                        m.toastManager.showToast(
                                            Toast.Type.SUCCESS,
                                            "Alterado para o Modo Básico!"
                                        )
                                    }
                                }) {
                                    Text("Confirmar")
                                }
                            }
                        )
                    }
                ) {
                    Div(attrs = {
                        classes("radio-option-info")
                    }) {
                        Div(attrs = {
                            classes("radio-option-title")
                        }) {
                            Text("Modo Básico")
                        }

                        Div(attrs = {
                            classes("radio-option-description")
                        }) {
                            Text("Mensagens com texto, embeds e botões")
                        }
                    }
                }

                FancyRadioInput(
                    name = "message-mode",
                    value = "components-v2",
                    checked = mutableMessage.isComponentsV2(),
                    onChange = {
                        m.modalManager.openModalWithCloseButton(
                            "Alterar Modo de Mensagem",
                            Modal.Size.MEDIUM,
                            {
                                Div {
                                    Text("Você deseja alterar para o modo Components V2?")
                                }
                                Div {
                                    B {
                                        Text("Atenção!")
                                    }

                                    Text(" ")

                                    Text("Esta operação irá apagar a sua mensagem atual!")
                                }
                            },
                            { modal ->
                                DiscordButton(DiscordButtonType.DANGER, attrs = {
                                    onClick {
                                        mutableMessage.content = null
                                        mutableMessage.embeds.clear()
                                        mutableMessage.components.clear()
                                        mutableMessage.setComponentsV2Mode(true)
                                        mutableMessage.triggerUpdate()
                                        modal.close()
                                        m.toastManager.showToast(
                                            Toast.Type.SUCCESS,
                                            "Alterado para o Modo Components V2!"
                                        )
                                    }
                                }) {
                                    Text("Confirmar")
                                }
                            }
                        )
                    }
                ) {
                    Div(attrs = {
                        classes("radio-option-info")
                    }) {
                        Div(attrs = {
                            classes("radio-option-title")
                        }) {
                            Text("Modo Components V2 (Avançado)")
                        }

                        Div(attrs = {
                            classes("radio-option-description")
                        }) {
                            Text("Mensagens usando o novo sistema de \"Components V2\" do Discord, com texto, botões, seções, galerias, separadores e containers")
                        }
                    }
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
                        // Content field (only in Components V1)
                        if (!mutableMessage.isComponentsV2()) {
                            FieldWrapper {
                                FieldInformation {
                                    FieldLabel("Conteúdo da Mensagem")
                                }

                                // For Components v1, the message MUST have a non-null content
                                TextAreaWithEntityPickers(targetGuild, mutableMessage.content ?: "") {
                                    mutableMessage.content = it
                                    mutableMessage.triggerUpdate()
                                }
                            }
                        }

                        // Embeds (only in Components V1)
                        if (!mutableMessage.isComponentsV2()) {
                            val embeds = mutableMessage.embeds

                            for (embed in embeds) {
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
                                            embed.color = it?.rgb
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
                                                        DiscordButtonType.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT,
                                                        attrs = {
                                                            onClick {
                                                                embed.fields.removeAt(index)
                                                                mutableMessage.triggerUpdate()
                                                            }
                                                        }
                                                    ) {
                                                        Text("Remover")
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
                                                mutableMessage.embeds.remove(embed)
                                                mutableMessage.triggerUpdate()
                                            }
                                        }
                                    ) {
                                        Text("Remover")
                                    }
                                }
                            }
                        }

                        val canAddMoreEmbeds = 10 > mutableMessage.embeds.size
                        VerticalList {
                            val components = mutableMessage.components

                            for (component in components) {
                                ComponentEditor(m, components, component, mutableMessage, targetGuild, verifiedIconRawHtml, eyeDropperIconRawHtml) {
                                    components.remove(component)
                                    mutableMessage.triggerUpdate()
                                }
                            }

                            if (mutableMessage.isComponentsV2()) {
                                // Show Components V2 components if it is enabled
                                HorizontalList(attrs = { classes("child-flex-grow") }) {
                                    AddTextDisplayButton(
                                        mutableMessage,
                                        mutableMessage.components
                                    )

                                    AddActionRowButton(
                                        mutableMessage,
                                        mutableMessage.components,
                                        true
                                    )

                                    AddSectionButton(
                                        mutableMessage,
                                        mutableMessage.components
                                    )

                                    AddSeparatorButton(
                                        mutableMessage,
                                        mutableMessage.components
                                    )

                                    AddGalleryButton(
                                        mutableMessage,
                                        mutableMessage.components
                                    )

                                    DiscordButton(DiscordButtonType.PRIMARY, attrs = {
                                        onClick {
                                            mutableMessage.components.add(
                                                MutableDiscordMessage.MutableDiscordComponent.MutableContainer(
                                                    DiscordComponent.DiscordContainer(
                                                        components = listOf(),
                                                        accentColor = null,
                                                        spoiler = false
                                                    )
                                                )
                                            )
                                            mutableMessage.triggerUpdate()
                                        }
                                    }) { Text("Adicionar Container") }
                                }
                            } else {
                                // If Components V2 isn't enabled, then let's show the embed + action row buttons
                                if (canAddMoreEmbeds && components.isEmpty()) {
                                    HorizontalList(attrs = {
                                        classes("child-flex-grow")
                                    }) {
                                        AddEmbedButton(mutableMessage, components)
                                        AddActionRowButton(mutableMessage, components, false)
                                    }
                                } else {
                                    AddActionRowButton(mutableMessage, components, false)
                                }
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
fun SectionEditor(
    m: LorittaDashboardFrontend,
    components: MutableList<MutableDiscordMessage.MutableDiscordComponent>,
    component: MutableDiscordMessage.MutableDiscordComponent.MutableSection,
    message: MutableDiscordMessage,
    targetGuild: DiscordGuild,
    verifiedIconRawHtml: SVGIconManager.SVGIcon,
    eyeDropperIconRawHtml: SVGIconManager.SVGIcon,
    onRemove: () -> Unit
) {
    FieldWrapper {
        FieldInformation { FieldLabel("Seção") }

        VerticalList(attrs = {
            attr(
                "style", "border: 1px solid var(--card-border-color);\n" +
                        "  border-radius: var(--nice-border-radius);\n" +
                        "  padding: 1em;"
            )
        }) {
            // TextDisplay components (1-3)
            for ((index, textDisplay) in component.components.withIndex()) {
                if (textDisplay is MutableDiscordMessage.MutableDiscordComponent.MutableTextDisplay) {
                    Div(attrs = {
                        attr(
                            "style", "border: 1px solid var(--card-border-color);\n" +
                                    "  border-radius: var(--nice-border-radius);\n" +
                                    "  padding: 1em;\n" +
                                    "  margin-bottom: 0.5em;"
                        )
                    }) {
                        FieldWrapper {
                            FieldInformation { FieldLabel("Texto ${index + 1}") }
                            TextAreaWithEntityPickers(targetGuild, textDisplay.content) {
                                textDisplay.content = it.take(2000)
                                message.triggerUpdate()
                            }
                        }

                        if (component.components.size > 1) {
                            DiscordButton(DiscordButtonType.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT, attrs = {
                                onClick {
                                    component.components.removeAt(index)
                                    message.triggerUpdate()
                                }
                            }) { Text("Remover") }
                        }
                    }
                }
            }

            // Add TextDisplay button
            if (component.components.size < 3) {
                DiscordButton(DiscordButtonType.PRIMARY, attrs = {
                    onClick {
                        component.components.add(
                            MutableDiscordMessage.MutableDiscordComponent.MutableTextDisplay(
                                DiscordComponent.DiscordTextDisplay(content = "Texto")
                            )
                        )
                        message.triggerUpdate()
                    }
                }) { Text("Adicionar Texto (${component.components.size}/3)") }
            }

            // Accessory section
            FieldWrapper {
                FieldInformation { FieldLabel("Acessório") }

                val accessory = component.accessory
                if (accessory != null) {
                    Div(attrs = {
                        attr(
                            "style", "border: 1px solid var(--card-border-color);\n" +
                                    "  border-radius: var(--nice-border-radius);\n" +
                                    "  padding: 1em;"
                        )
                    }) {
                        ComponentEditor(m, component.components, accessory, message, targetGuild, verifiedIconRawHtml, eyeDropperIconRawHtml) {
                            component.accessory = null
                            message.triggerUpdate()
                        }
                    }
                } else {
                    HorizontalList(attrs = { classes("child-flex-grow") }) {
                        DiscordButton(DiscordButtonType.PRIMARY, attrs = {
                            onClick {
                                component.accessory = MutableDiscordMessage.MutableDiscordComponent.MutableButton(
                                    DiscordComponent.DiscordButton(label = "Link", style = 5, url = "https://loritta.website/")
                                )
                                message.triggerUpdate()
                            }
                        }) { Text("Adicionar Botão") }

                        DiscordButton(DiscordButtonType.PRIMARY, attrs = {
                            onClick {
                                component.accessory = MutableDiscordMessage.MutableDiscordComponent.MutableThumbnail(
                                    DiscordComponent.DiscordThumbnail(media = DiscordComponent.UnfurledMediaItem(url = ""))
                                )
                                message.triggerUpdate()
                            }
                        }) { Text("Adicionar Thumbnail") }
                    }
                }
            }

            // Remove Section button
            DiscordButton(DiscordButtonType.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT, attrs = {
                onClick {
                    onRemove()
                }
            }) { Text("Remover") }
        }
    }
}

@Composable
fun TextDisplayEditor(
    targetGuild: DiscordGuild,
    components: MutableList<MutableDiscordMessage.MutableDiscordComponent>,
    component: MutableDiscordMessage.MutableDiscordComponent.MutableTextDisplay,
    message: MutableDiscordMessage,
    onRemove: () -> Unit
) {
    FieldWrapper {
        FieldInformation { FieldLabel("Texto") }
        VerticalList(attrs = {
            attr(
                "style", "border: 1px solid var(--card-border-color);\n" +
                        "  border-radius: var(--nice-border-radius);\n" +
                        "  padding: 1em;"
            )
        }) {
            FieldWrapper {
                FieldInformation { FieldLabel("Conteúdo") }
                TextAreaWithEntityPickers(targetGuild, component.content) {
                    component.content = it.take(2000)
                    message.triggerUpdate()
                }
            }

            DiscordButton(DiscordButtonType.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT, attrs = {
                onClick {
                    components.remove(component)
                    message.triggerUpdate()
                }
            }) { Text("Remover") }
        }
    }
}

@Composable
fun ThumbnailEditor(
    targetGuild: DiscordGuild,
    component: MutableDiscordMessage.MutableDiscordComponent.MutableThumbnail,
    message: MutableDiscordMessage,
    onRemove: () -> (Unit)
) {
    VerticalList {
        FieldWrapper {
            FieldInformation { FieldLabel("URL da Imagem") }
            TextInput(component.media.url) {
                onInput {
                    component.media.url = it.value
                    message.triggerUpdate()
                }
            }
        }

        FieldWrapper {
            FieldInformation { FieldLabel("Descrição (opcional)") }
            TextAreaWithEntityPickers(targetGuild, component.description ?: "") {
                component.description = it.take(1024).ifEmpty { null }
                message.triggerUpdate()
            }
        }

        DiscordToggle(
            "thumbnail-spoiler",
            "Marcar como Spoiler",
            null,
            component.spoiler,
            onChange = { newValue ->
                component.spoiler = newValue
                message.triggerUpdate()
            }
        )

        DiscordButton(
            DiscordButtonType.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT,
            attrs = {
                onClick {
                    onRemove()
                }
            }
        ) {
            Text("Remover")
        }
    }
}

@Composable
fun MediaGalleryEditor(
    components: MutableList<MutableDiscordMessage.MutableDiscordComponent>,
    component: MutableDiscordMessage.MutableDiscordComponent.MutableMediaGallery,
    message: MutableDiscordMessage,
    targetGuild: DiscordGuild,
    onRemove: () -> (Unit)
) {
    FieldWrapper {
        FieldInformation { FieldLabel("Galeria de Mídia") }

        VerticalList(attrs = {
            attr(
                "style", "border: 1px solid var(--card-border-color);\n" +
                        "  border-radius: var(--nice-border-radius);\n" +
                        "  padding: 1em;"
            )
        }) {
            for ((index, item) in component.items.withIndex()) {
                Div(attrs = {
                    attr(
                        "style", "border: 1px solid var(--card-border-color);\n" +
                                "  border-radius: var(--nice-border-radius);\n" +
                                "  padding: 1em;\n" +
                                "  margin-bottom: 0.5em;"
                    )
                }) {
                    FieldInformation { FieldLabel("Item ${index + 1}") }

                    FieldWrapper {
                        FieldInformation { FieldLabel("URL da Imagem") }
                        TextInput(item.media.url) {
                            onInput {
                                item.media.url = it.value
                                message.triggerUpdate()
                            }
                        }
                    }

                    FieldWrapper {
                        FieldInformation { FieldLabel("Descrição (opcional)") }
                        TextAreaWithEntityPickers(targetGuild, item.description ?: "") {
                            item.description = it.take(1024).ifEmpty { null }
                            message.triggerUpdate()
                        }
                    }

                    DiscordToggle(
                        "gallery-item-spoiler-$index",
                        "Marcar como Spoiler",
                        null,
                        item.spoiler,
                        onChange = { newValue ->
                            item.spoiler = newValue
                            message.triggerUpdate()
                        }
                    )

                    DiscordButton(DiscordButtonType.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT, attrs = {
                        onClick {
                            component.items.removeAt(index)
                            message.triggerUpdate()
                        }
                    }) { Text("Remover") }
                }
            }

            if (component.items.size < 10) {
                DiscordButton(DiscordButtonType.PRIMARY, attrs = {
                    onClick {
                        component.items.add(
                            MutableDiscordMessage.MutableDiscordComponent.MutableMediaGallery.MutableMediaGalleryItem(
                                DiscordComponent.DiscordMediaGallery.MediaGalleryItem(
                                    media = DiscordComponent.UnfurledMediaItem(""),
                                    description = null,
                                    spoiler = false
                                )
                            )
                        )
                        message.triggerUpdate()
                    }
                }) { Text("Adicionar Item (${component.items.size}/10)") }
            }

            DiscordButton(DiscordButtonType.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT, attrs = {
                onClick {
                    onRemove()
                }
            }) { Text("Remover") }
        }
    }
}

@Composable
fun SeparatorEditor(
    components: MutableList<MutableDiscordMessage.MutableDiscordComponent>,
    component: MutableDiscordMessage.MutableDiscordComponent.MutableSeparator,
    message: MutableDiscordMessage,
    onRemove: () -> (Unit)
) {
    VerticalList {
        FieldWrapper {
            FieldInformation { FieldLabel("Separador") }

            DiscordToggle(
                "separator-divider",
                "Mostrar Linha Divisória",
                null,
                component.divider,
                onChange = { newValue ->
                    component.divider = newValue
                    message.triggerUpdate()
                }
            )

            FieldWrapper {
                FieldInformation { FieldLabel("Espaçamento") }
                VerticalList {
                    FancyRadioInput(
                        name = "separator-spacing",
                        value = "1",
                        checked = component.spacing == 1,
                        onChange = {
                            component.spacing = 1
                            message.triggerUpdate()
                        }
                    ) {
                        Div(attrs = {
                            classes("radio-option-info")
                        }) {
                            Div(attrs = {
                                classes("radio-option-title")
                            }) {
                                Text("Pequeno")
                            }

                            Div(attrs = {
                                classes("radio-option-description")
                            }) {
                                Text("Espaçamento menor entre elementos")
                            }
                        }
                    }

                    FancyRadioInput(
                        name = "separator-spacing",
                        value = "2",
                        checked = component.spacing == 2,
                        onChange = {
                            component.spacing = 2
                            message.triggerUpdate()
                        }
                    ) {
                        Div(attrs = {
                            classes("radio-option-info")
                        }) {
                            Div(attrs = {
                                classes("radio-option-title")
                            }) {
                                Text("Grande")
                            }

                            Div(attrs = {
                                classes("radio-option-description")
                            }) {
                                Text("Espaçamento maior entre elementos")
                            }
                        }
                    }
                }
            }
        }

        DiscordButton(DiscordButtonType.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT, attrs = {
            onClick {
                onRemove()
            }
        }) { Text("Remover") }
    }
}

@Composable
fun ContainerEditor(
    m: LorittaDashboardFrontend,
    components: MutableList<MutableDiscordMessage.MutableDiscordComponent>,
    component: MutableDiscordMessage.MutableDiscordComponent.MutableContainer,
    message: MutableDiscordMessage,
    targetGuild: DiscordGuild,
    verifiedIconRawHtml: SVGIconManager.SVGIcon,
    eyeDropperIconRawHtml: SVGIconManager.SVGIcon,
    onRemove: () -> (Unit)
) {
    FieldWrapper {
        FieldInformation { FieldLabel("Container") }

        VerticalList(attrs = {
            val accentColor = component.accentColor
            val borderColor = if (accentColor != null) {
                val red = accentColor shr 16 and 0xFF
                val green = accentColor shr 8 and 0xFF
                val blue = accentColor and 0xFF
                "rgb($red, $green, $blue)"
            } else "#e3e5e8"

            attr(
                "style", "border: 1px solid var(--card-border-color);\n" +
                        "  border-radius: var(--nice-border-radius);\n" +
                        "  padding: 1em; border-left: 4px solid $borderColor;"
            )
        }) {
            // Accent Color Picker
            FieldWrapper {
                FieldInformation { FieldLabel("Cor de Destaque") }
                ColorPicker(
                    m,
                    verifiedIconRawHtml,
                    eyeDropperIconRawHtml,
                    component.accentColor?.let {
                        val red = it shr 16 and 0xFF
                        val green = it shr 8 and 0xFF
                        val blue = it and 0xFF
                        Color(red, green, blue)
                    }
                ) {
                    component.accentColor = it?.rgb
                    message.triggerUpdate()
                }
            }

            // Spoiler Toggle
            DiscordToggle(
                "container-spoiler",
                "Marcar como Spoiler",
                null,
                component.spoiler,
                onChange = { newValue ->
                    component.spoiler = newValue
                    message.triggerUpdate()
                }
            )

            // Nested Components
            for (childComponent in component.components) {
                Div(attrs = {
                    attr(
                        "style", "border: 1px solid var(--card-border-color);\n" +
                                "  border-radius: var(--nice-border-radius);\n" +
                                "  padding: 1em;\n" +
                                "  margin-bottom: 0.5em;"
                    )
                }) {
                    ComponentEditor(m, component.components, childComponent, message, targetGuild, verifiedIconRawHtml, eyeDropperIconRawHtml) {
                        component.components.remove(childComponent)
                        message.triggerUpdate()
                    }
                }
            }

            // Add Component Buttons
            HorizontalList(attrs = { classes("child-flex-grow") }) {
                AddTextDisplayButton(
                    message,
                    component.components
                )

                AddActionRowButton(
                    message,
                    component.components,
                    true
                )

                AddSectionButton(
                    message,
                    component.components
                )

                AddSeparatorButton(
                    message,
                    component.components
                )

                AddGalleryButton(
                    message,
                    component.components
                )
            }

            // Remove Container
            DiscordButton(DiscordButtonType.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT, attrs = {
                onClick {
                    components.remove(component)
                    message.triggerUpdate()
                }
            }) { Text("Remover") }
        }
    }
}

@Composable
fun ActionRowEditor(
    m: LorittaDashboardFrontend,
    components: MutableList<MutableDiscordMessage.MutableDiscordComponent>,
    component: MutableDiscordMessage.MutableDiscordComponent.MutableActionRow,
    message: MutableDiscordMessage,
    targetGuild: DiscordGuild,
    verifiedIconRawHtml: SVGIconManager.SVGIcon,
    eyeDropperIconRawHtml: SVGIconManager.SVGIcon,
    onRemove: () -> (Unit)
) {
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

            classes("child-flex-grow")
        }) {
            for (childComponent in component.components) {
                Div(attrs = {
                    attr(
                        "style", "border: 1px solid var(--card-border-color);\n" +
                                "  border-radius: var(--nice-border-radius);\n" +
                                "  padding: 1em;"
                    )
                }) {
                    ComponentEditor(m, component.components, childComponent, message, targetGuild, verifiedIconRawHtml, eyeDropperIconRawHtml, onRemove)
                }
            }

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
                DiscordButtonType.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT,
                attrs = {
                    onClick {
                        components.remove(component)
                        message.triggerUpdate()
                    }
                }
            ) {
                Text("Remover")
            }
        }
    }
}

@Composable
fun ButtonEditor(
    components: MutableList<MutableDiscordMessage.MutableDiscordComponent>,
    component: MutableDiscordMessage.MutableDiscordComponent.MutableButton,
    message: MutableDiscordMessage,
    onRemove: () -> (Unit)
) {
    VerticalList {
        FieldWrapper {
            FieldInformation {
                FieldLabel("Label")
            }

            TextInput(component.label ?: "") {
                onInput {
                    component.label = it.value.take(80).ifEmpty { null }
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
            DiscordButtonType.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT,
            attrs = {
                onClick {
                    onRemove()
                }
            }
        ) {
            Text("Remover")
        }
    }
}

@Composable
fun ComponentEditor(
    m: LorittaDashboardFrontend,
    // Components that are on the same "level" as this component
    // We need this because we need to be able to remove this component!
    components: MutableList<MutableDiscordMessage.MutableDiscordComponent>,
    component: MutableDiscordMessage.MutableDiscordComponent,
    message: MutableDiscordMessage,
    targetGuild: DiscordGuild,
    verifiedIconRawHtml: SVGIconManager.SVGIcon,
    eyeDropperIconRawHtml: SVGIconManager.SVGIcon,
    onRemove: () -> (Unit)
) {
    when (component) {
        is MutableDiscordMessage.MutableDiscordComponent.MutableActionRow -> ActionRowEditor(m, components, component, message, targetGuild, verifiedIconRawHtml, eyeDropperIconRawHtml, onRemove)
        is MutableDiscordMessage.MutableDiscordComponent.MutableButton -> ButtonEditor(components, component, message, onRemove)
        is MutableDiscordMessage.MutableDiscordComponent.MutableContainer -> ContainerEditor(m,  components, component, message, targetGuild, verifiedIconRawHtml, eyeDropperIconRawHtml, onRemove)
        is MutableDiscordMessage.MutableDiscordComponent.MutableMediaGallery -> MediaGalleryEditor(components, component, message, targetGuild, onRemove)
        is MutableDiscordMessage.MutableDiscordComponent.MutableSection -> SectionEditor(m, components, component, message, targetGuild, verifiedIconRawHtml, eyeDropperIconRawHtml, onRemove)
        is MutableDiscordMessage.MutableDiscordComponent.MutableSeparator -> SeparatorEditor(components, component, message, onRemove)
        is MutableDiscordMessage.MutableDiscordComponent.MutableTextDisplay -> TextDisplayEditor(targetGuild, components, component, message, onRemove)
        is MutableDiscordMessage.MutableDiscordComponent.MutableThumbnail -> ThumbnailEditor(targetGuild, component, message, onRemove)
    }
}

@Composable
fun AddTextDisplayButton(
    message: MutableDiscordMessage,
    components: MutableList<MutableDiscordMessage.MutableDiscordComponent>
) {
    DiscordButton(DiscordButtonType.PRIMARY, attrs = {
        onClick {
            components.add(
                MutableDiscordMessage.MutableDiscordComponent.MutableTextDisplay(
                    DiscordComponent.DiscordTextDisplay(content = "")
                )
            )
            message.triggerUpdate()
        }
    }) { Text("Adicionar Texto") }
}

@Composable
fun AddSectionButton(
    message: MutableDiscordMessage,
    components: MutableList<MutableDiscordMessage.MutableDiscordComponent>
) {
    DiscordButton(DiscordButtonType.PRIMARY, attrs = {
        onClick {
            components.add(
                MutableDiscordMessage.MutableDiscordComponent.MutableSection(
                    DiscordComponent.DiscordSection(
                        components = listOf(DiscordComponent.DiscordTextDisplay(content = "A Loritta é muito fofa! :3")),
                        accessory = null
                    )
                )
            )
            message.triggerUpdate()
        }
    }) { Text("Adicionar Seção") }
}

@Composable
fun AddSeparatorButton(
    message: MutableDiscordMessage,
    components: MutableList<MutableDiscordMessage.MutableDiscordComponent>
) {
    DiscordButton(DiscordButtonType.PRIMARY, attrs = {
        onClick {
            components.add(
                MutableDiscordMessage.MutableDiscordComponent.MutableSeparator(
                    DiscordComponent.DiscordSeparator()
                )
            )
            message.triggerUpdate()
        }
    }) { Text("Adicionar Separador") }
}

@Composable
fun AddGalleryButton(
    message: MutableDiscordMessage,
    components: MutableList<MutableDiscordMessage.MutableDiscordComponent>
) {
    DiscordButton(DiscordButtonType.PRIMARY, attrs = {
        onClick {
            components.add(
                MutableDiscordMessage.MutableDiscordComponent.MutableMediaGallery(
                    DiscordComponent.DiscordMediaGallery(
                        items = listOf(
                            DiscordComponent.DiscordMediaGallery.MediaGalleryItem(
                                media = DiscordComponent.UnfurledMediaItem(""),
                                description = null,
                                spoiler = false
                            )
                        )
                    )
                )
            )
            message.triggerUpdate()
        }
    }) { Text("Adicionar Galeria") }
}

@Composable
fun AddActionRowButton(
    message: MutableDiscordMessage,
    components: MutableList<MutableDiscordMessage.MutableDiscordComponent>,
    isComponentsV2: Boolean
) {
    DiscordButton(
        DiscordButtonType.PRIMARY,
        attrs = {
            if (!isComponentsV2 && components.size >= 5)
                disabled()
            else
                onClick {
                    components.add(
                        MutableDiscordMessage.MutableDiscordComponent.MutableActionRow(
                            DiscordComponent.DiscordActionRow(components = listOf())
                        )
                    )
                    message.triggerUpdate()
                }
        }
    ) {
        if (isComponentsV2)
            Text("Adicionar Linha de Botões")
        else
            Text("Adicionar Linha de Botões (${message.components.size}/5)")
    }
}

@Composable
fun AddEmbedButton(
    message: MutableDiscordMessage,
    components: MutableList<MutableDiscordMessage.MutableDiscordComponent>,
) {
    DiscordButton(
        DiscordButtonType.PRIMARY,
        attrs = {
            onClick {
                message.embeds.add(
                    MutableDiscordMessage.MutableDiscordEmbed(
                        DiscordEmbed(
                            description = "A Loritta é muito fofa!"
                        )
                    )
                )
                message.triggerUpdate()
            }
        }
    ) {
        Text("Adicionar Embed")
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