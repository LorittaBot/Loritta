package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.declarations

import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.webhook.*

class WebhookCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Webhook
    }

    override fun declaration() = slashCommand(I18N_PREFIX.Label, CommandCategory.DISCORD, I18N_PREFIX.Description) {
        subcommandGroup(I18N_PREFIX.Send.Label, TodoFixThisData) {
            subcommand(I18N_PREFIX.Send.Simple.Label, I18N_PREFIX.Send.Simple.Description) {
                executor = { WebhookSendSimpleExecutor(it) }
            }

            subcommand(I18N_PREFIX.Send.Json.Label, I18N_PREFIX.Send.Json.Description) {
                executor = { WebhookSendJsonExecutor(it) }
            }

            subcommand(I18N_PREFIX.Send.Repost.Label, I18N_PREFIX.Send.Repost.Description) {
                executor = { WebhookSendRepostExecutor(it) }
            }
        }

        subcommandGroup(I18N_PREFIX.Edit.Label, TodoFixThisData) {
            subcommand(I18N_PREFIX.Edit.Simple.Label, I18N_PREFIX.Edit.Simple.Description) {
                executor = { WebhookEditSimpleExecutor(it) }
            }

            subcommand(I18N_PREFIX.Edit.Json.Label, I18N_PREFIX.Edit.Json.Description) {
                executor = { WebhookEditJsonExecutor(it) }
            }

            subcommand(I18N_PREFIX.Edit.Repost.Label, I18N_PREFIX.Send.Repost.Description) {
                executor = { WebhookEditRepostExecutor(it) }
            }
        }
    }
}