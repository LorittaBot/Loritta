package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber

import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.utils.extensions.await

class LoriTuberCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Lorituber
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.ECONOMY) {
        executor = LoriTuberExecutor()
    }

    inner class LoriTuberExecutor : LorittaSlashCommandExecutor() {
        override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
            val hook = context.deferChannelMessage()

            createUI(context.user, hook, LoriTuberScreen.ViewChannel)
        }
    }

    suspend fun createUI(user: User, hook: InteractionHook, screen: LoriTuberScreen) {
        when (screen) {
            is LoriTuberScreen.ViewChannel -> {
                val button = loritta.interactivityManager.buttonForUser(
                    user,
                    ButtonStyle.PRIMARY,
                    "Switch to Motives",
                ) {
                    createUI(user, it.deferEdit(), LoriTuberScreen.ViewMotives)
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

            LoriTuberScreen.ViewMotives -> {
                val button = loritta.interactivityManager.buttonForUser(
                    user,
                    ButtonStyle.PRIMARY,
                    "Switch to Channel",
                ) {
                    createUI(user, it.deferEdit(), LoriTuberScreen.ViewChannel)
                }

                val sleep = loritta.interactivityManager.buttonForUser(
                    user,
                    ButtonStyle.PRIMARY,
                    "Dormir",
                ) {
                    createUI(user, it.deferEdit(), LoriTuberScreen.Sleeping)
                }

                hook.editOriginal(
                    MessageEdit {
                        embed {
                            title = "Loritta Morenitta"

                            field("Energia", "[XXXXX]")
                        }

                        actionRow(button)
                        actionRow(sleep)
                    }
                ).await()
            }

            LoriTuberScreen.Sleeping -> {
                val wakeUp = loritta.interactivityManager.buttonForUser(
                    user,
                    ButtonStyle.PRIMARY,
                    "Acordar!",
                ) {
                    createUI(user, it.deferEdit(), LoriTuberScreen.ViewMotives)
                }

                hook.editOriginal(
                    MessageEdit {
                        content = "A mimir..."

                        actionRow(wakeUp)
                    }
                ).setReplace(true).await()
            }
        }
    }

    sealed class LoriTuberScreen {
        object ViewChannel : LoriTuberScreen()
        object ViewMotives : LoriTuberScreen()
        object Sleeping : LoriTuberScreen()
    }
}