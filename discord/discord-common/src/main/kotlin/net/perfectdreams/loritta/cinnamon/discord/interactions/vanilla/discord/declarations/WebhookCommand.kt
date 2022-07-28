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

    override fun declaration() = slashCommand("webhook", CommandCategory.DISCORD, I18N_PREFIX.Description) {
        subcommandGroup("send", TodoFixThisData) {
            subcommand("simple", I18N_PREFIX.Send.Simple.Description) {
                executor = { WebhookSendSimpleExecutor(it) }
            }

            subcommand("json", I18N_PREFIX.Send.Json.Description) {
                executor = { WebhookSendJsonExecutor(it) }
            }

            subcommand("repost", I18N_PREFIX.Send.Repost.Description) {
                executor = { WebhookSendRepostExecutor(it) }
            }
        }

        subcommandGroup("edit", TodoFixThisData) {
            subcommand("simple", I18N_PREFIX.Edit.Simple.Description) {
                executor = { WebhookEditSimpleExecutor(it) }
            }

            subcommand("json", I18N_PREFIX.Edit.Json.Description) {
                executor = { WebhookEditJsonExecutor(it) }
            }

            subcommand("repost", I18N_PREFIX.Send.Repost.Description) {
                executor = { WebhookEditRepostExecutor(it) }
            }
        }
    }
}