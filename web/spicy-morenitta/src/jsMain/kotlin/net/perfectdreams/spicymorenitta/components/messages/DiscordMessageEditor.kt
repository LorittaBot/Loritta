package net.perfectdreams.spicymorenitta.components.messages

import androidx.compose.runtime.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.charsets.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits
import net.perfectdreams.loritta.common.utils.embeds.DiscordEmbed
import net.perfectdreams.loritta.common.utils.embeds.DiscordMessage
import net.perfectdreams.loritta.common.utils.placeholders.PlaceholderSectionType
import net.perfectdreams.loritta.serializable.DiscordGuild
import net.perfectdreams.loritta.serializable.DiscordUser
import net.perfectdreams.loritta.serializable.messageeditor.LorittaMessageTemplate
import net.perfectdreams.loritta.serializable.messageeditor.MessageEditorMessagePlaceholder
import net.perfectdreams.loritta.serializable.messageeditor.TestMessageRequest
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.components.*
import net.perfectdreams.spicymorenitta.components.colorpicker.ColorPicker
import net.perfectdreams.spicymorenitta.components.colorpicker.ColorUtils
import net.perfectdreams.spicymorenitta.toasts.Toast
import net.perfectdreams.spicymorenitta.utils.htmx
import net.perfectdreams.spicymorenitta.utils.jsObject
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextArea
import org.jetbrains.compose.web.dom.TextInput
import kotlin.random.Random

val JsonForDiscordMessages = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
}

@Composable
fun DiscordMessageEditor(
    m: SpicyMorenitta,
    // TODO: Replace with a "mini" i18nContext-like thing
    // i18nContext: I18nContext,
    templates: List<LorittaMessageTemplate>,
    placeholderSectionType: PlaceholderSectionType,
    placeholders: List<MessageEditorMessagePlaceholder>,
    targetGuild: DiscordGuild,
    testMessageEndpointUrl: String,
    targetChannel: TargetChannelResult,
    selfUser: DiscordUser,
    messagesToBeRenderedBeforeTargetMessage: List<DiscordMessageWithAuthor>,
    messagesToBeRenderedAfterTargetMessage: List<DiscordMessageWithAuthor>,
    rawMessage: String,
    onMessageContentChange: (String) -> (Unit)
) {
    var editorType by remember { mutableStateOf(EditorType.INTERACTIVE) }
    var messageEditorRenderDirection by remember { mutableStateOf(DiscordMessageUtils.RenderDirection.HORIZONTAL) }

    Div(attrs = {
        classes("message-editor")
    }) {
        val parsedMessage = try {
            JsonForDiscordMessages.decodeFromString<DiscordMessage>(rawMessage)
        } catch (e: SerializationException) {
            null
        } catch (e: IllegalStateException) {
            null // This may be triggered when a message has invalid message components
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
                            m.modalManager.openCloseOnlyModal(
                                "Templates de Mensagens",
                            ) {
                                Text("Sem criatividade? Então pegue um template!")

                                VerticalList {
                                    for (template in templates) {
                                        DiscordButton(
                                            DiscordButtonType.PRIMARY,
                                            attrs = {
                                                onClick {
                                                    m.modalManager.openModalWithCloseButton(
                                                        "Você realmente quer substituir?",
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
                                                                        onMessageContentChange.invoke(
                                                                            template.content
                                                                        )
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
                    } else disabledWithSoundEffect()
                }
            ) {
                TextWithIconWrapper(/* SVGIconManager.bars, {} */) {
                    Text("Template de Mensagens")
                }
            }

            Div {
                DiscordButton(
                    DiscordButtonType.PRIMARY,
                    attrs = {
                        onClick {
                            m.modalManager.openCloseOnlyModal(
                                "Importar",
                            ) {
                                Text("Qual mensagem você deseja importar?")

                                VerticalList {
                                    DiscordButton(
                                        DiscordButtonType.PRIMARY,
                                        attrs = {
                                            onClick {
                                                var embed by mutableStateOf<DiscordEmbed?>(null)

                                                m.modalManager.openModalWithCloseButton(
                                                    "Embed do Carl-bot (Embed em JSON)",
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
                                                                    disabledWithSoundEffect()
                                                                else
                                                                    onClick {
                                                                        onMessageContentChange.invoke(
                                                                            JsonForDiscordMessages.encodeToString(
                                                                                DiscordMessage(
                                                                                    "",
                                                                                    embed
                                                                                )
                                                                            ).also {
                                                                                println(it)
                                                                            }
                                                                        )
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
                    TextWithIconWrapper(/* SVGIconManager.fileImport, {} */) {
                        Text("Importar")
                    }
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
                    TextWithIconWrapper(/* SVGIconManager.pencil, {} */) {
                        Text("Alterar modo de edição")
                    }
                }
            }

            Div {
                DiscordButton(
                    DiscordButtonType.PRIMARY,
                    attrs = {
                        if (targetChannel is TargetChannelResult.ChannelNotSelected) {
                            disabledWithSoundEffect()
                        } else {
                            onClick {
                                GlobalScope.launch {
                                    m.toastManager.showToast(Toast.Type.INFO, "Enviando mensagem...")

                                    // We use ktor directly here because we don't really need htmx, and we will use JSON (omg)
                                    val response = m.http.post(testMessageEndpointUrl) {
                                        setBody(
                                            Json.encodeToString(
                                                TestMessageRequest(
                                                    rawMessage,
                                                    when (targetChannel) {
                                                        TargetChannelResult.ChannelNotSelected -> null
                                                        TargetChannelResult.DirectMessageTarget -> null
                                                        is TargetChannelResult.GuildMessageChannelTarget -> targetChannel.id
                                                    },
                                                    placeholderSectionType,
                                                    placeholders.associate {
                                                        it.name to it.replaceWith
                                                    }
                                                )
                                            )
                                        )
                                    }

                                    // This is from the "SpicyMorenitta-Use-Response-As-HXTrigger" code
                                    val responseAsText = response.bodyAsText(Charsets.UTF_8)
                                    val json = Json.parseToJsonElement(responseAsText).jsonObject
                                    json.entries.forEach {
                                        val eventName = it.key
                                        val eventValue = it.value.jsonPrimitive.contentOrNull
                                        println("Triggering event $eventName with $eventValue")
                                        htmx.trigger(
                                            "body",
                                            it.key,
                                            jsObject {
                                                this.value = eventValue
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                ) {
                    TextWithIconWrapper(/* SVGIconManager.paperPlane, {} */) {
                        Text("Testar Mensagem")
                    }
                }
            }

            Div(attrs = {
                classes("change-message-preview-direction")
            }) {
                DiscordButton(
                    DiscordButtonType.PRIMARY,
                    {
                        onClick {
                            messageEditorRenderDirection = when (messageEditorRenderDirection) {
                                DiscordMessageUtils.RenderDirection.VERTICAL -> DiscordMessageUtils.RenderDirection.HORIZONTAL
                                DiscordMessageUtils.RenderDirection.HORIZONTAL -> DiscordMessageUtils.RenderDirection.VERTICAL
                            }
                        }
                    }
                ) {
                    TextWithIconWrapper(/* SVGIconManager.diagramNext, {
                        if (m.globalState.messageEditorRenderDirection == DiscordMessageUtils.RenderDirection.VERTICAL)
                            attr("style", "transform: rotate(270deg);")
                        else
                            attr("style", "transform: initial;")
                    } */) {
                        when (messageEditorRenderDirection) {
                            DiscordMessageUtils.RenderDirection.VERTICAL -> Text("Visualização na Horizontal")
                            DiscordMessageUtils.RenderDirection.HORIZONTAL -> Text("Visualização na Vertical")
                        }
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
                        } else disabledWithSoundEffect()
                    }
                ) {
                    TextWithIconWrapper(/* SVGIconManager.sparkles, {} */) {
                        Text("Formatar JSON")
                    }
                }
            }
        }

        Div(attrs = {
            classes("message-textarea-and-preview")

            if (messageEditorRenderDirection == DiscordMessageUtils.RenderDirection.VERTICAL)
                classes("vertical-render")
        }) {
            when (editorType) {
                EditorType.INTERACTIVE -> {
                    VerticalList {
                        FieldWrapper {
                            FieldLabel("Conteúdo da Mensagem")

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
                                        disabledWithSoundEffect()
                                    else
                                        onClick {
                                            mutableMessage.components.add(
                                                MutableDiscordMessage.MutableDiscordComponent.MutableActionRow(
                                                    net.perfectdreams.loritta.common.utils.embeds.DiscordComponent.DiscordActionRow(components = listOf())
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
                                    ColorUtils.convertFromColorToHex(embedColor)
                                } else "#e3e5e8"

                                attr("style", "border: 1px solid var(--input-border-color);\n" +
                                        "  border-radius: var(--first-level-border-radius);\n" +
                                        "  padding: 1em; border-left: 4px solid $hex;\n")
                            }) {
                                FieldWrapper {
                                    FieldLabel("Cor")

                                    ColorPicker(
                                        m,
                                        embed.color?.let {
                                            val red = it shr 16 and 0xFF
                                            val green = it shr 8 and 0xFF
                                            val blue = it and 0xFF

                                            net.perfectdreams.loritta.common.utils.Color(red, green, blue)
                                        }
                                    ) {
                                        mutableMessage.embed?.color = it?.rgb
                                        mutableMessage.triggerUpdate()
                                    }
                                }

                                FieldWrapper {
                                    FieldLabel("Nome do Autor")

                                    TextInput(embed.author?.name ?: "") {
                                        onInput {
                                            val author = embed.author
                                            if (author != null) {
                                                val newValue = it.value.ifEmpty { null }
                                                if (newValue == null) {
                                                    if (author.url != null || author.iconUrl != null) {
                                                        // If the author text is null BUT there's an icon or URL set, tell the user that they must delete both before deleting the text
                                                        m.toastManager.showToast(Toast.Type.WARN, "Embed Inválida") {
                                                            Text("Você não pode ter um ícone ou URL de autor sem ter um texto! Apague o ícone e a URL antes de deletar o texto do autor.")
                                                        }
                                                        m.soundEffects.error.play(1.0)
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
                                    FieldLabel("URL do Autor")

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
                                    FieldLabel("URL do Ícone do Autor")

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
                                    FieldLabel("Título")

                                    TextAreaWithEntityPickers(targetGuild, embed.title ?: "") {
                                        embed.title = it.ifEmpty { null }
                                        mutableMessage.triggerUpdate()
                                    }
                                }

                                FieldWrapper {
                                    FieldLabel("URL do Título")

                                    TextInput(embed.url ?: "") {
                                        onInput {
                                            embed.url = it.value.ifEmpty { null }
                                            mutableMessage.triggerUpdate()
                                        }
                                    }
                                }

                                FieldWrapper {
                                    FieldLabel("Descrição")

                                    TextAreaWithEntityPickers(targetGuild, embed.description ?: "") {
                                        embed.description = it.ifEmpty { null }
                                        mutableMessage.triggerUpdate()
                                    }
                                }

                                Div {
                                    FieldLabel("Fields")

                                    VerticalList(attrs = {
                                        attr(
                                            "style", "border: 1px solid var(--input-border-color);\n" +
                                                    "  border-radius: var(--first-level-border-radius);\n" +
                                                    "  padding: 1em;"
                                        )
                                    }) {
                                        for ((index, field) in embed.fields.withIndex()) {
                                            FieldWrappers(attrs = {
                                                attr(
                                                    "style", "border: 1px solid var(--input-border-color);\n" +
                                                            "  border-radius: var(--first-level-border-radius);\n" +
                                                            "  padding: 1em;"
                                                )
                                            }) {
                                                FieldLabel("Field ${index + 1}")
                                                Div {
                                                    FieldLabel("Nome")

                                                    TextAreaWithEntityPickers(targetGuild, field.name) {
                                                        field.name = it
                                                        mutableMessage.triggerUpdate()
                                                    }
                                                }

                                                Div {
                                                    FieldLabel("Valor")

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
                                                if (DiscordResourceLimits.Embed.FieldsPerEmbed > embed.fields.size) {
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
                                                    disabledWithSoundEffect()
                                                }
                                            }
                                        ) {
                                            Text("Adicionar Field")
                                        }
                                    }
                                }

                                FieldWrapper {
                                    FieldLabel("URL da Imagem")

                                    TextInput(embed.imageUrl ?: "") {
                                        onInput {
                                            embed.imageUrl = it.value.ifEmpty { null }
                                            mutableMessage.triggerUpdate()
                                        }
                                    }
                                }

                                FieldWrapper {
                                    FieldLabel("URL da Thumbnail")

                                    TextInput(embed.thumbnailUrl ?: "") {
                                        onInput {
                                            embed.thumbnailUrl = it.value.ifEmpty { null }
                                            mutableMessage.triggerUpdate()
                                        }
                                    }
                                }

                                val mutableFooter = embed.footer
                                FieldWrapper {
                                    FieldLabel("Texto do Rodapé")

                                    TextInput(embed.footer?.text ?: "") {
                                        onInput {
                                            val footer = embed.footer
                                            if (footer != null) {
                                                val newValue = it.value.ifEmpty { null }
                                                if (newValue == null) {
                                                    if (footer.iconUrl != null) {
                                                        // If the footer text is null BUT there's an icon set, tell the user that they must delete the icon before deleting the text
                                                        m.toastManager.showToast(Toast.Type.WARN, "Embed Inválida") {
                                                            Text("Você não pode ter um ícone de rodapé sem ter um texto! Apague o ícone antes de deletar o texto do rodapé.")
                                                        }
                                                        m.soundEffects.error.play(1.0)
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
                                    FieldLabel("URL do Ícone do Rodapé")

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
                                    DiscordButtonType.DANGER,
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
                            FieldLabel("Conteúdo da Mensagem em JSON")

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
                    FieldLabel("Pré-visualização da Mensagem")

                    Div(attrs = {
                        id("message-preview-wrapper-$rId")
                        classes("message-preview-wrapper")
                    }) {
                        Div(attrs = {
                            id("message-preview-$rId")
                            classes("message-preview")
                        }) {
                            for (message in messagesToBeRenderedBeforeTargetMessage) {
                                DiscordMessageRenderer(
                                    message.author,
                                    message.message,
                                    null,
                                    targetGuild.channels,
                                    targetGuild.roles,
                                    placeholders,
                                )
                            }

                            if (parsedMessage != null) {
                                DiscordMessageRenderer(
                                    RenderableDiscordUser.fromDiscordUser(selfUser)
                                        .copy(name = DiscordMessageUtils.LORITTA_MORENITTA_FANCY_NAME),
                                    parsedMessage,
                                    null,
                                    targetGuild.channels,
                                    targetGuild.roles,
                                    placeholders,
                                )
                            } else {
                                // If the message couldn't be parsed, render it as a normal message
                                DiscordMessageRenderer(
                                    RenderableDiscordUser.fromDiscordUser(selfUser)
                                        .copy(name = DiscordMessageUtils.LORITTA_MORENITTA_FANCY_NAME),
                                    DiscordMessage(
                                        content = rawMessage
                                    ),
                                    null,
                                    targetGuild.channels,
                                    targetGuild.roles,
                                    placeholders
                                )
                            }

                            for (message in messagesToBeRenderedAfterTargetMessage) {
                                DiscordMessageRenderer(
                                    message.author,
                                    message.message,
                                    null,
                                    targetGuild.channels,
                                    targetGuild.roles,
                                    placeholders,
                                )
                            }
                        }
                    }
                }
            }
        }

// TODO: Fix this! (do we need this in here? can't we render it on the backend?
        /* Div {
            FancyDetails(
                {},
                {
                    Text("Quais são as variáveis/placeholders que eu posso usar?")
                },
                {
                    Table {
                        Thead {
                            Tr {
                                Th {
                                    Text("Placeholder")
                                }
                                Th {
                                    Text("Significado")
                                }
                            }
                        }

                        Tbody {
                            customTokens.forEach {
                                Tr {
                                    Td {
                                        var isFirst = true
                                        for (placeholder in it.placeholder.names.filter { !it.hidden }) {
                                            if (!isFirst)
                                                Text(", ")

                                            Code {
                                                Text(placeholder.placeholder.asKey)
                                            }
                                            isFirst = false
                                        }
                                    }

                                    Td {
                                        val description = it.placeholder.description
                                        if (description != null)
                                            Text(i18nContext.get(description))
                                    }
                                }
                            }
                        }
                    }
                }
            )
        } */
    }
}

@Composable
fun ActionRowEditor(m: SpicyMorenitta, component: MutableDiscordMessage.MutableDiscordComponent.MutableActionRow, message: MutableDiscordMessage) {
    FieldWrapper {
        FieldLabel("Linha de Botões")

        VerticalList(attrs = {
            attr(
                "style", "border: 1px solid var(--input-border-color);\n" +
                        "  border-radius: var(--first-level-border-radius);\n" +
                        "  padding: 1em;"
            )
        }) {
            for (childComponent in component.components) {
                Div(attrs = {
                    attr(
                        "style", "border: 1px solid var(--input-border-color);\n" +
                                "  border-radius: var(--first-level-border-radius);\n" +
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
                            disabledWithSoundEffect()
                        else
                            onClick {
                                component.components.add(
                                    MutableDiscordMessage.MutableDiscordComponent.MutableButton(
                                        net.perfectdreams.loritta.common.utils.embeds.DiscordComponent.DiscordButton(
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
            FieldLabel("Label")

            TextInput(component.label ?: "") {
                onInput {
                    component.label = it.value.ifEmpty { null }
                    message.triggerUpdate()
                }
            }
        }

        FieldWrapper {
            FieldLabel("URL")

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
    m: SpicyMorenitta,
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

data class DiscordMessageWithAuthor(
    val author: RenderableDiscordUser,
    val message: DiscordMessage
)

enum class EditorType {
    INTERACTIVE,
    RAW
}