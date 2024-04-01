package net.perfectdreams.loritta.morenitta.interactions.vanilla.aprilfools

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordInviteUtils
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.AprilFoolsCoinFlipBugs
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.utils.AprilFools
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.escapeMentions
import net.perfectdreams.loritta.morenitta.utils.stripCodeMarks
import org.jetbrains.exposed.sql.insert
import java.time.Instant
import java.time.LocalDateTime

class CoinFlipBetBugCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    override fun command() = slashCommand(I18nKeysData.Commands.Command.Coinflipbetbug.Label, I18nKeysData.Commands.Command.Coinflipbetbug.Description, CommandCategory.ECONOMY) {
        executor = CoinFlipBetBugExecutor()
    }

    inner class CoinFlipBetBugExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val code = string("code", I18nKeysData.Commands.Command.Coinflipbetbug.Options.Code.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            if (!AprilFools.isAprilFools()) {
                context.reply(true) {
                    styled(
                        "Infelizmente o Bug do Coin Flip™ foi sequestrado pelo governo pois perceberam que era um bug muito poderoso para um mero mortal como você... Disseram que o bug agora mora na Ilha de Alcatraz, que o Netflix vai fazer um filme sobre as balbúrdias que ele causou, e ele também te desejou um Feliz Primeiro de Abril!",
                        Emotes.LoriFire
                    )
                }
                return
            }

            val input = DiscordInviteUtils.stripInvites(args[options.code].stripCodeMarks().escapeMentions())

            if (input.length > 100) {
                context.reply(true) {
                    styled(
                        "Eu sei que você quer bugar intensamente o coin flip, mas o bug é grande demais! O seu bug deve ter, no máximo, 100 caracteres.",
                        Emotes.LoriFire
                    )
                }
                return
            }

            GlobalScope.launch {
                context.reply(true) {
                    styled(
                        "Bugando coin flip - 0%",
                        Emotes.LoriLurk
                    )
                    styled(
                        "**Status:** Iniciando bug..."
                    )
                }

                delay(5_000)

                context.reply(true) {
                    styled(
                        "Bugando coin flip - 5%",
                        Emotes.LoriReading
                    )
                    styled(
                        "**Status:** Detectando sequência do coin flip..."
                    )
                }

                delay(3_000)

                context.reply(true) {
                    styled(
                        "Bugando coin flip - 8%",
                        Emotes.LoriCoffee
                    )
                    styled(
                        "**Status:** Analisando `$input` na sequência do coin flip..."
                    )
                }

                delay(3_000)

                context.reply(true) {
                    styled(
                        "Bugando coin flip - 11%",
                        Emotes.LoriCoffee
                    )
                    styled(
                        "**Status:** Analisando `${input.reversed()}`, que é só o seu bug mas ao contrário..."
                    )
                }

                delay(5_000)

                context.reply(true) {
                    styled(
                        "Bugando coin flip - 18%",
                        Emotes.LoriSleeping
                    )
                    styled(
                        "**Status:** Pensando sobre a vida..."
                    )
                }

                delay(2_000)

                context.reply(true) {
                    styled(
                        "Bugando coin flip - 21%",
                        Emotes.LoriSleeping
                    )
                    styled(
                        "**Status:** Filosofando sobre a vida..."
                    )
                }

                delay(3_000)

                context.reply(true) {
                    styled(
                        "Bugando coin flip - 32%",
                        Emotes.LoriSleeping
                    )
                    styled(
                        "**Status:** Rodando `java -jar BugDoCoinFlipInjector.jar --code \"${input}\"`..."
                    )
                }

                delay(5_000)

                context.reply(true) {
                    styled(
                        "Bugando coin flip - 40%",
                        Emotes.LoriFire
                    )
                    styled(
                        "**Status:** SEQUÊNCIA DO COIN FLIP FOI INJETADA!!! Verificando bug na sequência..."
                    )
                }

                delay(5_000)

                context.reply(true) {
                    styled(
                        "Bugando coin flip - 47%",
                        Emotes.LoriBlackAndWhite
                    )
                    styled(
                        "**Status:** Eles estão chegando. É melhor pensar duas vezes antes de abrir a porta."
                    )
                }

                delay(10_000)

                context.reply(true) {
                    styled(
                        "Bugando coin flip - 60%",
                        Emotes.LoriReading
                    )
                    styled(
                        "**Status:** Analisando resultados da sequência do coin flip..."
                    )
                }

                delay(3_000)

                context.reply(true) {
                    styled(
                        "Bugando coin flip - 69%",
                        Emotes.LoriBonk
                    )
                    styled(
                        "**Status:** nice"
                    )
                }

                delay(3_000)

                context.reply(true) {
                    styled(
                        "Bugando coin flip - 71%",
                        Emotes.LoriHanglooseRight
                    )
                    styled(
                        "**Status:** Editando análise combinatória..."
                    )
                }

                delay(3_000)

                context.reply(true) {
                    styled(
                        "Bugando coin flip - 72%",
                        Emotes.LoriAngel
                    )
                    styled(
                        "**Status:** Substituindo `SecureRandom` da Loritta por um random que te favorece..."
                    )
                }

                delay(3_000)

                context.reply(true) {
                    styled(
                        "Bugando coin flip - 75%",
                        Emotes.LoriFire
                    )
                    styled(
                        "**Status:** Jogando sal em cima do seu coin flip para as recalcadas..."
                    )
                }

                delay(3_000)

                context.reply(true) {
                    styled(
                        "Bugando coin flip - 76%",
                        Emotes.LoriReading
                    )
                    styled(
                        "**Status:** Analisando as análises..."
                    )
                }

                delay(5_000)

                context.reply(true) {
                    styled(
                        "Bugando coin flip - 77%",
                        Emotes.LoriBlackAndWhite
                    )
                    styled(
                        "**Status:** O fim do coin flip está próximo. Faça os preparativos."
                    )
                }

                delay(10_000)

                context.reply(true) {
                    styled(
                        "Bugando coin flip - 90%",
                        Emotes.LoriFire
                    )
                    styled(
                        "**Status:** ATENÇÃO: SEU BUG DO COIN FLIP FOI DETECTADO NA SEQUÊNCIA DO COIN FLIP!!!"
                    )
                }

                delay(5_000)

                context.reply(true) {
                    styled(
                        "Bugando coin flip - 100%",
                        Emotes.LoriFire
                    )
                    styled(
                        "**Status:** Bug do Coin Flip™ foi ativado com sucesso! As suas chances de ganhar o coin flip foram alteradas de 50% para... ||50% ${Emotes.LoriBonk}||"
                    )
                }

                loritta.transaction {
                    AprilFoolsCoinFlipBugs.insert {
                        it[AprilFoolsCoinFlipBugs.userId] = context.user.idLong
                        it[AprilFoolsCoinFlipBugs.bug] = input
                        it[AprilFoolsCoinFlipBugs.beggedAt] = Instant.now()
                        it[AprilFoolsCoinFlipBugs.year] = LocalDateTime.now(Constants.LORITTA_TIMEZONE).year
                    }
                }

                delay(10_000)

                context.reply(true) {
                    styled(
                        "Bug do Coin Flip™ não existe, Feliz Primeiro de Abril!",
                        Emotes.LoriKiss
                    )
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val args = context.args.joinToString(" ")
            if (args.isBlank()) {
                context.explain()
                return null
            }

            return mapOf(
                options.code to args
            )
        }
    }

    sealed class CoinFlipTaxResult(val totalRewardPercentage: Double) {
        class LorittaCommunity(val isWeekend: Boolean, totalRewardPercentage: Double) : CoinFlipTaxResult(totalRewardPercentage)
        class PremiumUser(val premiumUser: User, totalRewardPercentage: Double) : CoinFlipTaxResult(totalRewardPercentage)
        class Default(totalRewardPercentage: Double) : CoinFlipTaxResult(totalRewardPercentage)
    }
}