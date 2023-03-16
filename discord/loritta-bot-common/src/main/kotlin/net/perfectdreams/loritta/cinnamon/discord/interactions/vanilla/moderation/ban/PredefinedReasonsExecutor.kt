package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation.ban

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.cache.data.GuildData
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import net.perfectdreams.discordinteraktions.common.autocomplete.GuildAutocompleteContext
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.interactiveButtonWithDatabaseData
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.loriEmoji
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation.AdminUtils
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation.declarations.BanCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.ModerationPredefinedPunishmentMessages
import net.perfectdreams.loritta.common.utils.text.TextUtils.shortenWithEllipsis
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert

class PredefinedReasonsExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta) {
    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        if (context !is GuildApplicationCommandContext)
            return

        context.deferChannelMessageEphemerally()

        loritta.pudding.transaction {
            ModerationPredefinedPunishmentMessages.deleteWhere {
                ModerationPredefinedPunishmentMessages.guild eq context.guildId.toLong()
            }

            ModerationPredefinedPunishmentMessages.insert {
                it[guild] = context.guildId.toLong()
                it[short] = "divdm"
                it[message] = "Enviar conteúdo (não solicitado!) via mensagem direta, fazer spam (ou seja, mandar conteúdo indesejado para outras pessoas) é contra as regras do servidor da Loritta e dos termos de uso do Discord e, caso continuar, você poderá ser suspenso do Discord e irá perder a sua conta!"
            }
            ModerationPredefinedPunishmentMessages.insert {
                it[guild] = context.guildId.toLong()
                it[short] = "div"
                it[message] = "Não é permitido divulgar conteúdos em canais de texto sem que a equipe permita."
            }
            ModerationPredefinedPunishmentMessages.insert {
                it[guild] = context.guildId.toLong()
                it[short] = "spam"
                it[message] = "Floodar/spammar (Enviar várias mensagens repetidas, enviar uma mensagem com caracteres aletórios, adicionar reações aleatórias, etc) nos canais de texto."
            }
            ModerationPredefinedPunishmentMessages.insert {
                it[guild] = context.guildId.toLong()
                it[short] = "nsfw"
                it[message] = "É proibido compartilhar conteúdo NSFW (coisas obscenas como pornografia, gore e coisas relacionadas), conteúdo sugestivo, jumpscares, conteúdo de ódio, racismo, assédio, links com conteúdo ilegal e links falsos. Será punido até se passar via mensagem direta, até mesmo se a outra pessoa pedir."
            }
            ModerationPredefinedPunishmentMessages.insert {
                it[guild] = context.guildId.toLong()
                it[short] = "toxic"
                it[message] = "Ser tóxico (irritar e desrespeitar) com outros membros do servidor. Aprenda a respeitar e conviver com outras pessoas!"
            }
            ModerationPredefinedPunishmentMessages.insert {
                it[guild] = context.guildId.toLong()
                it[short] = "under13"
                it[message] = "É proibido ter uma conta de Discord caso você tenha menos de 13 anos!"
            }
            ModerationPredefinedPunishmentMessages.insert {
                it[guild] = context.guildId.toLong()
                it[short] = "bob"
                it[message] = "Imagine fazer spam dizendo que não é para fazer spam."
            }
            ModerationPredefinedPunishmentMessages.insert {
                it[guild] = context.guildId.toLong()
                it[short] = "selfbot"
                it[message] = "Não é permitido o uso de selfbot no nosso servidor, caso continue, a conta poderá ser suspensa da plataforma e inutilizada."
            }
        }

        context.sendEphemeralMessage {
            styled(
                "Motivos de punições pré-definidos foram criados com sucesso! Você agora pode usar eles no ${loritta.commandMentions.ban} na opção de `predefined_reason`!",
                Emotes.LoriHi
            )
            styled(
                "No futuro, quando o MrPowerGamerBR parar de ser preguiçoso, motivos de punições pré-definidos poderão ser customizados pelo meu painel!",
                Emotes.LoriLick
            )
        }
    }
}