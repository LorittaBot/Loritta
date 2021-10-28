package net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations

import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.WebhookEditJsonExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.WebhookEditRepostExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.WebhookEditSimpleExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.WebhookSendJsonExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.WebhookSendRepostExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.WebhookSendSimpleExecutor

object WebhookCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Webhook

    override fun declaration() = command(listOf("webhook"), CommandCategory.DISCORD, I18N_PREFIX.Description) {
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