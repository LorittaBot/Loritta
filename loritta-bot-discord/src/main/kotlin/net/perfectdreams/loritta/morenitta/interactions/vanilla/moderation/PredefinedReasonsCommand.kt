package net.perfectdreams.loritta.morenitta.interactions.vanilla.moderation

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.ModerationPredefinedPunishmentMessages
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationBuilder
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

class PredefinedReasonsCommand: SlashCommandDeclarationWrapper {
    override fun command(): SlashCommandDeclarationBuilder =
        slashCommand(
            name = I18N_PREFIX.Label,
            description = I18N_PREFIX.Label,
            category = CommandCategory.MODERATION,
            uniqueId = UUID.fromString("e126bb95-1bbd-4117-bd42-db467276ece4")
        ) {
            enableLegacyMessageSupport = true
            isGuildOnly = true
            defaultMemberPermissions = DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS)
            executor = PredefinedReasonsExecutor()
        }

    class PredefinedReasonsExecutor: LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(
            context: UnleashedContext,
            args: SlashCommandArguments
        ) {
            context.deferChannelMessage(ephemeral = true)

            // TODO: Allow further customization of predefined reasons
            val predefinedReasons = DefaultPredefinedPunishmentMessages

            registerPredefinedReasons(
                context.loritta,
                context.guild.idLong,
                predefinedReasons
            )

            context.reply(ephemeral = true) {
                styled(
                    "Motivos de punições pré-definidos foram criados com sucesso! Você agora pode usar eles no ${context.loritta.commandMentions.ban} na opção de `predefined_reason`!",
                    Emotes.LoriHi
                )
                styled(
                    "No futuro, quando o MrPowerGamerBR parar de ser preguiçoso, motivos de punições pré-definidos poderão ser customizados pelo meu painel!",
                    Emotes.LoriLick
                )
            }
        }

        private suspend fun registerPredefinedReasons(
            loritta: LorittaBot,
            guildId: Long,
            reasons: Collection<PredefinedPunishmentMessage>
        ) {
            loritta.pudding.transaction {
                ModerationPredefinedPunishmentMessages.deleteWhere {
                    guild eq guildId
                }

                for (reason in reasons) {
                    ModerationPredefinedPunishmentMessages.insert {
                        it[guild] = guildId
                        it[short] = reason.shortened
                        it[message] = reason.message
                    }
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? = emptyMap()
    }

    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Predefinedreasons

        data class PredefinedPunishmentMessage(
            val shortened: String,
            val message: String
        )

        val DefaultPredefinedPunishmentMessages = listOf(
            PredefinedPunishmentMessage(
                shortened = "divdm",
                message = "Enviar conteúdo (não solicitado!) via mensagem direta, fazer spam (ou seja, mandar conteúdo indesejado para outras pessoas) é contra as regras do servidor da Loritta e dos termos de uso do Discord e, caso continuar, você poderá ser suspenso do Discord e irá perder a sua conta!"
            ),
            PredefinedPunishmentMessage(
                shortened = "div",
                message = "Não é permitido divulgar conteúdos em canais de texto sem que a equipe permita."
            ),
            PredefinedPunishmentMessage(
                shortened = "spam",
                message = "Floodar/spammar (Enviar várias mensagens repetidas, enviar uma mensagem com caracteres aletórios, adicionar reações aleatórias, etc) nos canais de texto."
            ),
            PredefinedPunishmentMessage(
                shortened = "nsfw",
                message = "É proibido compartilhar conteúdo NSFW (coisas obscenas como pornografia, gore e coisas relacionadas), conteúdo sugestivo, jumpscares, conteúdo de ódio, racismo, assédio, links com conteúdo ilegal e links falsos. Será punido até se passar via mensagem direta, até mesmo se a outra pessoa pedir."
            ),
            PredefinedPunishmentMessage(
                shortened = "toxic",
                message = "Ser tóxico (irritar e desrespeitar) com outros membros do servidor. Aprenda a respeitar e conviver com outras pessoas!"
            ),
            PredefinedPunishmentMessage(
                shortened = "under13",
                message = "É proibido ter uma conta de Discord caso você tenha menos de 13 anos!"
            ),
            PredefinedPunishmentMessage(
                shortened = "bob",
                message = "Imagine fazer spam dizendo que não é para fazer spam."
            ),
            PredefinedPunishmentMessage(
                shortened = "selfbot",
                message = "Não é permitido o uso de selfbot no nosso servidor, caso continue, a conta poderá ser suspensa da plataforma e inutilizada."
            )
        )
    }
}