package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.content.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.InteractionHook
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.lorituber.UUIDSerializer
import net.perfectdreams.loritta.lorituber.rpc.packets.GetCharactersByOwnerRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberResponse
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.UnleashedHook
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens.CreateCharacterScreen
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens.LoriTuberScreen
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens.ViewMotivesScreen
import net.perfectdreams.loritta.serializable.lorituber.requests.LoriTuberRPCRequest
import net.perfectdreams.loritta.serializable.lorituber.responses.LoriTuberRPCResponse
import java.util.*

class LoriTuberCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Lorituber
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.ECONOMY, UUID.fromString("9692a1f9-0566-454d-9df7-c76950dc1f72")) {
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)
        this.interactionContexts = listOf(InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL)

        executor = LoriTuberExecutor()
    }

    inner class LoriTuberExecutor : LorittaSlashCommandExecutor() {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            // TODO: Remove this cast if we end up supporting message commands for LoriTuber
            val hook = (context.deferChannelMessage(false) as UnleashedHook.InteractionHook)
                .jdaHook

            val character = sendLoriTuberRPCRequestNew<net.perfectdreams.loritta.lorituber.rpc.packets.GetCharactersByOwnerResponse>(
                GetCharactersByOwnerRequest(context.user.idLong)
            ).characters.firstOrNull()

            if (character != null) {
                ViewMotivesScreen(
                    this@LoriTuberCommand,
                    context.user,
                    hook,
                    PlayerCharacter(character.id, character.name, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0)
                ).render()
            } else {
                CreateCharacterScreen(
                    this@LoriTuberCommand,
                    context.user,
                    hook
                ).render()
            }
        }
    }

    suspend inline fun <reified T> sendLoriTuberRPCRequestNew(request: LoriTuberRequest): T {
        val response = loritta.http.post("http://127.0.0.1:3001/rpc") {
            setBody(TextContent(Json.encodeToString<LoriTuberRequest>(request), ContentType.Application.Json))
        }

        val x = response.bodyAsText()
        println("Response: $x")

        val parsed = Json.decodeFromString<LoriTuberResponse>(x)

        if (parsed !is T)
            error("Result does not match what we expect! $parsed")

        return parsed
    }

    suspend fun <T> sendLoriTuberRPCRequest(request: LoriTuberRPCRequest): T {
        val response = loritta.http.post("http://127.0.0.1:8080/rpc") {
            setBody(TextContent(Json.encodeToString<LoriTuberRPCRequest>(request), ContentType.Application.Json))
        }
        return Json.decodeFromString<LoriTuberRPCResponse>(response.bodyAsText()) as T
    }

    suspend fun switchScreen(screen: LoriTuberScreen) = screen.render()

    suspend fun checkMail(user: User, hook: InteractionHook, character: PlayerCharacter, currentScreen: LoriTuberScreen): Boolean {
        if (true)
            return false

        /* val mail = sendLoriTuberRPCRequest<GetMailResponse>(GetMailRequest(character.id))
            .mail

        if (mail != null) {
            // omg we have pending mails! let's read them!!
            switchScreen(
                ReceivedMailScreen(
                    this,
                    user,
                    hook,
                    character,
                    mail,
                    currentScreen
                )
            )
            return true
        }

        return false */
        return false
    }

    suspend fun createUI(user: User, hook: InteractionHook, screen: LoriTuberScreen) {
        /* when (screen) {
            is LoriTuberScreen.ViewChannel -> {
                val button = loritta.interactivityManager.buttonForUser(
                    user,
                    ButtonStyle.PRIMARY,
                    "Switch to Motives",
                ) {
                    createUI(user, it.deferEdit(), LoriTuberScreen.ViewMotives(screen.character))
                }

                hook.editOriginal(
                    MessageEdit {
                        embed {
                            title = "Sobre o seu Canal MrBeast6000"

                            field("ayaya", "yay!!!")
                        }

                        actionRow(button)
                    }
                ).await()
            }

            is LoriTuberScreen.ViewMotives -> {
                val motives = sendLoriTuberRPCRequest<GetCharacterMotivesResponse>(GetCharacterMotivesRequest(screen.character.id))

                val button = loritta.interactivityManager.buttonForUser(
                    user,
                    ButtonStyle.PRIMARY,
                    "Switch to Channel",
                ) {
                    createUI(user, it.deferEdit(), LoriTuberScreen.ViewChannel(screen.character))
                }

                val sleep = loritta.interactivityManager.buttonForUser(
                    user,
                    ButtonStyle.PRIMARY,
                    "Dormir",
                ) {
                    createUI(user, it.deferEdit(), LoriTuberScreen.Sleeping(screen.character))
                }

                val goToComputerPartsShop = loritta.interactivityManager.buttonForUser(
                    user,
                    ButtonStyle.PRIMARY,
                    "Ir para a Loja de Peças de Computadores",
                ) {
                    createUI(user, it.deferEdit(), LoriTuberScreen.ComputerPartsStore(screen.character))
                }

                val debugMenu = loritta.interactivityManager.buttonForUser(
                    user,
                    ButtonStyle.SECONDARY,
                    "Menu de Depuração",
                ) {
                    createUI(user, it.deferEdit(), LoriTuberScreen.DebugMenu)
                }

                hook.editOriginal(
                    MessageEdit {
                        embed {
                            title = "Loritta Morenitta"

                            description = "\uD83D\uDCFB *Starry Shores Rádio*\n⏰ 19:00\n\uD83C\uDF21️ 16º C\n☁ Nublado"

                            field("Fome", "${motives.hunger}%")
                            field("Energia", "${motives.energy}%")
                            // field("Energia", "[XXXXX]")
                        }

                        actionRow(button)
                        actionRow(sleep)
                        actionRow(goToComputerPartsShop)
                        actionRow(debugMenu)
                    }
                ).await()
            }

            is LoriTuberScreen.Sleeping -> {
                val wakeUp = loritta.interactivityManager.buttonForUser(
                    user,
                    ButtonStyle.PRIMARY,
                    "Acordar!",
                ) {
                    createUI(user, it.deferEdit(), LoriTuberScreen.ViewMotives(screen.character))
                }

                hook.editOriginal(
                    MessageEdit {
                        content = "A mimir..."

                        actionRow(wakeUp)
                    }
                ).setReplace(true).await()
            }

            is LoriTuberScreen.ComputerPartsStore -> {
                hook.editOriginal(
                    MessageEdit {
                        embed {
                            title = "Loja de Peças de Computadores"

                            description = "Olá, eu sou o Jerbs!"

                            image = "https://cdn.discordapp.com/attachments/739823666891849729/1052664743426523227/jerbs_store.png"
                        }
                    }
                ).await()
            }

            LoriTuberScreen.DebugMenu -> {
                val response = sendLoriTuberRPCRequest<LoriTuberRPCResponse>(GetServerInfoRequest())

                hook.editOriginal(
                    MessageEdit {
                        embed {
                            title = "Menu de Depuração"

                            field("Server Tick", response.currentTick.toString())
                            field("Última Atualização", "<t:${response.lastUpdate / 1_000}:R>")
                        }
                    }
                ).setReplace(true).await()
            }
        } */
    }

    data class PlayerCharacter(
        @Serializable(UUIDSerializer::class)
        val id: UUID,
        val name: String,
        val hungerNeed: Double,
        val energyNeed: Double,
        val funNeed: Double,
        val hygieneNeed: Double,
        val bladderNeed: Double,
        val socialNeed: Double
    )
}