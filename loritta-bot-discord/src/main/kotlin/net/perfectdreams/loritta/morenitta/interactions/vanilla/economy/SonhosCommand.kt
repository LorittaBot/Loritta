package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import net.dv8tion.jda.api.interactions.IntegrationType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import java.util.*

class SonhosCommand(private val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Sonhos
        val CATEGORY_I18N_PREFIX = I18nKeysData.Commands.Category.Economy
        val TRANSACTIONS_I18N_PREFIX = I18nKeysData.Commands.Command.Transactions
        val PAY_I18N_PREFIX = I18nKeysData.Commands.Command.Pay
        val SONHOS_RANK_I18N_PREFIX = I18nKeysData.Commands.Command.Sonhosrank
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, CATEGORY_I18N_PREFIX.RootCommandDescription, CommandCategory.ECONOMY, UUID.fromString("a2f8ef4e-ba83-4fb0-a14f-12a7168acf48")) {
        this.enableLegacyMessageSupport = true
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)

        subcommand(I18nKeysData.Commands.Command.Sonhosatm.Label, I18nKeysData.Commands.Command.Sonhosatm.Description, UUID.fromString("8f1beb59-63b1-4441-b841-d20df01a7ab1")) {
            this.alternativeLegacyAbsoluteCommandPaths.apply {
                add("sonhos")
                add("atm")
                add("bal")
                add("balance")
            }
            executor = SonhosAtmExecutor(loritta)
        }

        subcommand(PAY_I18N_PREFIX.Label, PAY_I18N_PREFIX.Description, UUID.fromString("5cd60131-5e1b-407e-8f73-b20a7370a504")) {
            this.alternativeLegacyAbsoluteCommandPaths.apply {
                add("pay")
                add("pagar")
            }
            executor = SonhosPayExecutor(loritta)
        }

        subcommand(TRANSACTIONS_I18N_PREFIX.Label, TRANSACTIONS_I18N_PREFIX.Description, UUID.fromString("d5e873bf-98e6-3770-a4e3-03f08eb80297")) {
            executor = SonhosTransactionsExecutor(loritta)
        }

        subcommand(SONHOS_RANK_I18N_PREFIX.Label, SONHOS_RANK_I18N_PREFIX.Description, UUID.fromString("edb411ac-25a1-3c7e-98bb-45881f979ac2")) {
            this.alternativeLegacyAbsoluteCommandPaths.apply {
                add("sonhos top")
                add("atm top")
            }

            executor = SonhosRankExecutor(loritta)
        }
    }
}