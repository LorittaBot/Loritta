package net.perfectdreams.spicymorenitta.mounters

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import io.ktor.client.request.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.common.utils.embeds.DiscordMessage
import net.perfectdreams.loritta.common.utils.placeholders.PlaceholderSectionType
import net.perfectdreams.loritta.serializable.config.GuildGeneralConfigBootstrap
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.components.DiscordButton
import net.perfectdreams.spicymorenitta.components.DiscordButtonType
import net.perfectdreams.spicymorenitta.components.SimpleSelectMenu
import net.perfectdreams.spicymorenitta.components.SimpleSelectMenuEntry
import net.perfectdreams.spicymorenitta.components.messages.*
import net.perfectdreams.spicymorenitta.utils.Logging
import net.perfectdreams.spicymorenitta.utils.plainStyle
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable
import web.html.HTMLElement

class GuildGeneralComponentMounter(val m: SpicyMorenitta) : SimpleComponentMounter("test-general"), Logging {
    override fun simpleMount(element: HTMLElement) {
        val configWrapper = element.getAttribute("config")!!.let { Json.decodeFromString<GuildGeneralConfigBootstrap>(it) }

        var config by mutableStateOf(configWrapper.config)

        var mutableConfig by mutableStateOf(toMutableConfig(config))

        var selectedBlockedChannel by mutableStateOf(0L)
        var saving by mutableStateOf(false)

        renderComposable(element) {
            Div(attrs = {
                attr("style", "border: solid 1px;\n" +
                        "    border-top-color: currentcolor;\n" +
                        "    border-right-color: currentcolor;\n" +
                        "    border-bottom-color: currentcolor;\n" +
                        "    border-left-color: currentcolor;\n" +
                        "  border-radius: 5px;\n" +
                        "  border-color: #dcddde;\n" +
                        "  padding: 20px;\n" +
                        "  margin-bottom: 24px;\n" +
                        "  background-color: #f8f9f9; display: flex; flex-direction: row; gap: 1em;")
            }) {
                Div {
                    Img(src = "/assets/img/lori_avatar_v3.png", attrs = {
                        attr("style", "border-radius: 99999px; height: 100px;")
                    })
                }

                Div(attrs = { classes("field-wrapper"); attr("style", "flex-grow: 1;") }) {
                    Div(attrs = { classes("field-title") }) {
                        Text("Prefixo da Loritta")
                    }

                    Div {
                        Text("Prefixo é o texto que vem antes de um comando. Por padrão eu venho com o caractere +, mas você pode alterá-lo nesta opção.")
                    }

                    Div {
                        Input(InputType.Text) {
                            onInput {
                                mutableConfig.prefix = it.value
                            }

                            value(mutableConfig.prefix)
                        }
                    }

                    Div(attrs = {
                        plainStyle("""width: 100%;
  overflow: auto;
  border-radius: var(--first-level-border-radius);
  border: 1px solid var(--input-border-color);
  background-color: var(--background-color);
  padding: 1em;
  display: flex;
  flex-direction: column;
  gap: 1em;""")
                    }) {
                        DiscordMessageRenderer(
                            RenderableDiscordUser.fromDiscordUser(configWrapper.user),
                            DiscordMessage("${mutableConfig.prefix}ping"),
                            null,
                            listOf(),
                            listOf(),
                            listOf()
                        )

                        DiscordMessageRenderer(
                            RenderableDiscordUser.fromDiscordUser(configWrapper.selfLorittaUser),
                            DiscordMessage("**Pong!**"),
                            null,
                            listOf(),
                            listOf(),
                            listOf()
                        )
                    }
                }
            }

            Hr()

            DiscordToggle(
                "test1",
                "Deletar mensagem do usuário ao executar um comando",
                "Ao executar um comando, eu irei deletar a mensagem do usuário.",
                mutableConfig._deleteMessageAfterCommand
            )

            Hr()

            DiscordToggle(
                "test2",
                "Avisar para o usuário quando ele executar um comando desconhecido",
                "Caso um usuário executar um comando inexistente, eu irei avisar a ele que eu não possuo o comando desejado, recomendar qual comando ele provavelmente queria usar e, caso o usuário queira saber todos os meus lindos comandos, para que use o meu comando de ajuda.",
                mutableConfig._warnOnUnknownCommand
            )

            Hr()

            Div(attrs = { classes("field-wrapper") }) {
                Div(attrs = { classes("field-title") }) {
                    Text("Canais que serão proibidos usar comandos")
                }

                Div {
                    Text("Nestes canais eu irei ignorar comandos de usuários, como se eu nem estivesse lá! (Mesmo que eu esteja observando as suas mensagens para dar XP, hihi~) Caso você queira configurar que cargos específicos possam burlar a restrição, configure na seção de permissões.")
                }
            }

            Div(attrs = {
                plainStyle("display: flex;")
            }) {
                SimpleSelectMenu(
                    "Selecione um Canal",
                    entries = configWrapper.guild.channels.map {
                        SimpleSelectMenuEntry(
                            {
                                Text(it.name)
                            },
                            it.id.toString(),
                            selectedBlockedChannel == it.id,
                            false,
                            null
                        )
                    }
                ) {
                    val selected = it.first()

                    selectedBlockedChannel = selected.toLong()
                }

                DiscordButton(
                    DiscordButtonType.PRIMARY,
                    attrs = {
                        if (selectedBlockedChannel in mutableConfig.blacklistedChannels || selectedBlockedChannel == 0L) {
                            disabled()
                        } else {
                            onClick {
                                mutableConfig.blacklistedChannels.add(selectedBlockedChannel.toLong())
                            }
                        }
                    }
                ) {
                    Text("Adicionar")
                }
            }

            Hr()

            for (blockedChannel in mutableConfig.blacklistedChannels) {
                val guildChannel = configWrapper.guild.channels.firstOrNull { it.id == blockedChannel }

                Div(attrs = {
                    classes("discord-mention")

                    onClick {
                        mutableConfig.blacklistedChannels.remove(blockedChannel)
                    }
                }) {
                    Text("#${guildChannel?.name} (${blockedChannel})")
                }
            }

            Hr()

            DiscordToggle(
                "test3",
                "Enviar mensagem para o usuário quando ele executar comandos em canais proibidos",
                "Caso você tenha configurado canais que sejam proibidos de usar comandos, você pode ativar esta opção para que, quando um usuário tente executar um comando em canais proibidos, eu avise que não é possível executar comandos no canal.",
                mutableConfig._warnIfBlacklisted
            )

            Hr()

            Div(attrs = { classes("field-wrapper") }) {
                Div(attrs = { classes("field-title") }) {
                    Text("Mensagem ao executar um comando em um canal proibido")
                }
            }

            DiscordMessageEditor(
                m,
                listOf(),
                PlaceholderSectionType.JOIN_MESSAGE,
                listOf(),
                configWrapper.guild,
                "",
                TargetChannelResult.ChannelNotSelected,
                configWrapper.selfLorittaUser,
                listOf(),
                listOf(),
                mutableConfig.blacklistedWarning ?: ""
            ) {
                mutableConfig.blacklistedWarning = it
            }

            Hr()

            Div {
                Text("Current: ${Json.encodeToString(config)} (hash: ${config.hashCode()})")
            }

            Div {
                Text("New: ${Json.encodeToString(toDataConfig(mutableConfig))} (hash: ${toDataConfig(mutableConfig).hashCode()})")
            }

            var hasChanges = config != toDataConfig(mutableConfig)

            Div {
                Text("Has Changes? $hasChanges")
            }

            // Maybe, with what little power you have... You can SAVE something else.
            Div(attrs = { classes("save-bar-fill-screen-height") }) {}

            Div(attrs = {
                id("save-bar")
                classes("save-bar")

                if (hasChanges) {
                    classes("has-changes")
                } else {
                    // classes("initial-state")
                    classes("no-changes")
                }
            }) {
                Div(attrs = { classes("save-bar-small-text") }) {
                    Text("Deseja salvar?")
                }

                Div(attrs = { classes("save-bar-large-text") }) {
                    Text("Cuidado! Você tem alterações que não foram salvas")
                }

                Div(attrs = {
                    classes("save-bar-buttons")
                    id("save-bar-buttons")
                }) {
                    DiscordButton(DiscordButtonType.NO_BACKGROUND_LIGHT_TEXT, attrs = {
                        onClick {
                            mutableConfig = toMutableConfig(config)
                        }
                    }) {
                        Text("Redefinir")
                    }

                    DiscordButton(
                        DiscordButtonType.SUCCESS,
                        attrs = {
                            if (saving)
                                disabled()
                            else {
                                onClick {
                                    val currentConfig = toDataConfig(mutableConfig)

                                    // Something here that would be awesomesauce
                                    m.launch {
                                        saving = true

                                        // TODO: how to save?
                                        m.http.put("/br/guild/268353819409252352/configure") {
                                            setBody(
                                                Json.encodeToString(currentConfig)
                                            )
                                        }

                                        saving = false

                                        config = currentConfig
                                        mutableConfig = toMutableConfig(currentConfig)
                                    }
                                }
                            }
                        }
                    ) {
                        Text("Salvar")
                    }
                }
            }
        }
    }

    fun toMutableConfig(config: GuildGeneralConfigBootstrap.GuildGeneralConfig) = MutableGuildGeneralConfig(config)
    fun toDataConfig(config: MutableGuildGeneralConfig) = GuildGeneralConfigBootstrap.GuildGeneralConfig(
        config.prefix,
        config.deleteMessageAfterCommand,
        config.warnOnUnknownCommand,
        // This does look weird, but that's intentional to fix hash issues
        // We don't use .toList() because StateList has its own toList that doesn't work how we want to
        ArrayList(config.blacklistedChannels),
        config.warnIfBlacklisted,
        config.blacklistedWarning
    )

    class MutableGuildGeneralConfig(config: GuildGeneralConfigBootstrap.GuildGeneralConfig) {
        var _prefix = mutableStateOf(config.prefix)
        var prefix by _prefix

        var _deleteMessageAfterCommand = mutableStateOf(config.deleteMessageAfterCommand)
        var deleteMessageAfterCommand by _deleteMessageAfterCommand

        var _warnOnUnknownCommand = mutableStateOf(config.warnOnUnknownCommand)
        var warnOnUnknownCommand by _warnOnUnknownCommand

        var _blacklistedChannels = config.blacklistedChannels.toMutableStateList()
        var blacklistedChannels = _blacklistedChannels

        var _warnIfBlacklisted = mutableStateOf(config.warnIfBlacklisted)
        var warnIfBlacklisted by _warnIfBlacklisted

        var _blacklistedWarning = mutableStateOf(config.blacklistedWarning)
        var blacklistedWarning by _blacklistedWarning
    }
}