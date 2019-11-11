package net.perfectdreams.spicymorenitta.views.dashboard

import LoriDashboard
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import kotlinx.serialization.parse
import net.perfectdreams.spicymorenitta.utils.HttpRequest
import net.perfectdreams.spicymorenitta.utils.SaveUtils
import net.perfectdreams.spicymorenitta.utils.loriUrl
import net.perfectdreams.spicymorenitta.utils.page
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.get
import kotlin.browser.document

@ImplicitReflectionSerializer
object ReactionRoleView {
    val reactionEntries by lazy {
        page.getElementById("reaction-entries")
    }

    @JsName("start")
    fun start() {
        document.addEventListener("DOMContentLoaded", {
            val premiumAsJson = document.getElementById("reaction-role-json")?.innerHTML!!

            println("premiumAsJson: $premiumAsJson")

            val guild = JSON.nonstrict.parse<ServerConfig.Guild>(premiumAsJson)

            (page.getElementById("add-reaction-message") as HTMLButtonElement).onclick = {
                val reactionLink = (page.getElementById("reaction-link") as HTMLInputElement)
                val link = reactionLink.value
                val split = link.split("/")
                val channelId = split[5]
                val messageId = split[6]
                GlobalScope.launch {
                    val response = HttpRequest.get("${loriUrl}api/v1/channels/$channelId/messages/$messageId")
                    if (response.statusCode != 200)
                        return@launch

                    val discordMessage = JSON.nonstrict.parse<ServerConfig.DiscordMessage>(response.body)

                    addReactionRoleEntry(discordMessage, guild, guild.reactionRoleConfigs)
                }
            }

            if (guild.reactionRoleConfigs.isNotEmpty()) {
                val discordMessages = mutableListOf<ServerConfig.DiscordMessage>()

                GlobalScope.launch {
                    for (reactionRoleConfig in guild.reactionRoleConfigs) {
                        if (discordMessages.any { it.id == reactionRoleConfig.messageId })
                            continue

                        val response = HttpRequest.get("${loriUrl}api/v1/channels/${reactionRoleConfig.textChannelId}/messages/${reactionRoleConfig.messageId}")
                        if (response.statusCode != 200)
                            continue

                        val discordMessage = JSON.nonstrict.parse<ServerConfig.DiscordMessage>(response.body)

                        discordMessages.add(discordMessage)
                    }

                    for (reactionRoleConfig in guild.reactionRoleConfigs) {
                        println("REACTION ROLE")
                        println("Message ID: " + reactionRoleConfig.messageId)
                        println("Text Channel ID: " + reactionRoleConfig.textChannelId)
                        println("Reaction: " + reactionRoleConfig.reaction)
                        println("Role IDs: " + reactionRoleConfig.roleIds)
                        println("Locks: " + reactionRoleConfig.locks)
                        val discordMessage = discordMessages.firstOrNull { it.id == reactionRoleConfig.messageId }
                        println("Message: ${discordMessage}")
                        if (discordMessage != null) {
                            println("Content: ${discordMessage.content}")
                            println("Reactions...")
                            discordMessage.reactions.forEach {
                                println("Is Discord Emote? " + it.isDiscordEmote)
                                println("Name: " + it.name)
                                println("ID: " + it.id)
                            }
                        }
                    }

                    for (discordMessage in discordMessages) {
                        addReactionRoleEntry(discordMessage, guild, guild.reactionRoleConfigs)
                    }
                }
            }
        })
    }

    fun addReactionRoleEntry(discordMessage: ServerConfig.DiscordMessage, serverConfig: ServerConfig.Guild, reactionOptions: List<ServerConfig.ReactionOption>) {
        println("Adicionando...")

        reactionEntries.append {
            div(classes = "message-stuff") {
                attributes["data-messageId"] = discordMessage.id
                attributes["data-channelId"] = discordMessage.channelId

                h1 {
                    + "Mensagem ${discordMessage.id}"
                }

                div(classes = "pure-g") {
                    style = "text-align: center;"

                    div(classes = "pure-u-1 pure-u-md-7-8") {
                        p(classes = "toggleMainText") {
                            + "Permitir escolher apenas uma das reações"
                        }
                    }

                    div(classes = "pure-u-1 pure-u-md-1-8") {
                        div(classes = "switch") {
                            input(type = InputType.checkBox, classes = "cmn-toggle cmn-toggle-round") {
                                value = "true"
                                id = "rr-toggle-${discordMessage.id}"

                                val reactionOption = reactionOptions.firstOrNull { it.messageId == discordMessage.id }
                                if (reactionOption?.locks?.isNotEmpty() == true)
                                    checked = true
                            }
                            label {
                                attributes["for"] = "rr-toggle-${discordMessage.id}"
                            }
                        }
                    }
                }

                for (reaction in discordMessage.reactions) {
                    div(classes = "userOptionsWrapper reaction-role-option") {
                        div(classes = "pure-g") {
                            div(classes = "pure-u-1 pure-u-md-1-6") {
                                if (reaction.isDiscordEmote) {
                                    img(src = "https://cdn.discordapp.com/emojis/${reaction.id}.png?v=1") {
                                        width = "64"
                                    }
                                } else {
                                    p {
                                        + reaction.name
                                    }
                                }
                            }
                            div(classes = "pure-u-1 pure-u-md-2-3") {
                                val reactionOption = reactionOptions.firstOrNull { it.messageId == discordMessage.id && (it.reaction == reaction.name || it.reaction == reaction.id) }

                                div(classes = "pure-g") {
                                    style = "text-align: center;"

                                    div(classes = "pure-u-1 pure-u-md-7-8") {
                                        p(classes = "toggleMainText") {
                                            + "Ativar Reaction Role"
                                        }
                                    }

                                    div(classes = "pure-u-1 pure-u-md-1-8") {
                                        div(classes = "switch") {
                                            input(type = InputType.checkBox, classes = "cmn-toggle cmn-toggle-round reaction-role-enabled-toggle") {
                                                value = "true"
                                                checked = reactionOption != null
                                                id = "rr-toggle-${discordMessage.id}-${reaction.name}"
                                            }
                                            label {
                                                attributes["for"] = "rr-toggle-${discordMessage.id}-${reaction.name}"
                                            }
                                        }
                                    }
                                }

                                div {
                                    id = "rr-hidden-${discordMessage.id}-${reaction.name}"

                                    val currentRoleId = reactionOption?.roleIds?.firstOrNull()

                                    select {
                                        for (role in serverConfig.roles) {
                                            option {
                                                + role.name

                                                if (role.id == currentRoleId)
                                                    selected = true
                                            }
                                        }
                                    }
                                }
                                if (reactionOption != null) {
                                    p {
                                        + "Existe um reaction role associado!"
                                    }
                                } else {
                                    p {
                                        + "Nenhum reaction role associado!"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        for (reaction in discordMessage.reactions) {
            LoriDashboard.applyBlur(
                    "#rr-hidden-${discordMessage.id}-${reaction.name}",
                    "#rr-toggle-${discordMessage.id}-${reaction.name}"
            )
        }
    }

    @JsName("prepareSave")
    fun prepareSave() {
        SaveUtils.prepareSave("reaction_role", extras = {
            val messageStuffs = document.getElementsByClassName("message-stuff")

            var i = 0
            while (messageStuffs.length > i) {
                var j = 0

                val entry = messageStuffs[i]!!
                println("$i. $entry")
                val messageId = entry.getAttribute("data-messageId")!!
                val channelId = entry.getAttribute("data-channelId")!!

                val reactionOptionsDiv = entry.getElementsByClassName("reaction-role-option")

                while (reactionOptionsDiv.length > j) {
                    val entry = reactionOptionsDiv[j]!!

                    println("Option: ${entry}")

                    j++
                }

                i++
            }
        })
    }
}