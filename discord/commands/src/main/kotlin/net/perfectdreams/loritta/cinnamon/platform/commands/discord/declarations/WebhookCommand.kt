package net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations

import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.webhook.WebhookEditJsonExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.webhook.WebhookEditRepostExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.webhook.WebhookEditSimpleExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.webhook.WebhookSendJsonExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.webhook.WebhookSendRepostExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.webhook.WebhookSendSimpleExecutor

object WebhookCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Webhook

    override fun declaration() = slashCommand(listOf("webhook"), CommandCategory.DISCORD, I18N_PREFIX.Description) {
        subcommandGroup(listOf("send"), TodoFixThisData) {
            subcommand(listOf("simple"), I18N_PREFIX.Send.Simple.Description) {
                executor = WebhookSendSimpleExecutor
            }

            subcommand(listOf("json"), I18N_PREFIX.Send.Json.Description) {
                executor = WebhookSendJsonExecutor
            }

            subcommand(listOf("repost"), I18N_PREFIX.Send.Repost.Description) {
                executor = WebhookSendRepostExecutor
            }
        }

        subcommandGroup(listOf("edit"), TodoFixThisData) {
            subcommand(listOf("simple"), I18N_PREFIX.Edit.Simple.Description) {
                executor = WebhookEditSimpleExecutor
            }

            subcommand(listOf("json"), I18N_PREFIX.Edit.Json.Description) {
                executor = WebhookEditJsonExecutor
            }

            subcommand(listOf("repost"), I18N_PREFIX.Send.Repost.Description) {
                executor = WebhookEditRepostExecutor
            }
        }
    }
}