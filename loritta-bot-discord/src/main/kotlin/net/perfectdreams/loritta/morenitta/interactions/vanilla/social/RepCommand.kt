package net.perfectdreams.loritta.morenitta.interactions.vanilla.social

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinTimeZone
import kotlinx.datetime.toLocalDateTime
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingReputation
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.text.TextUtils.shortenAndStripCodeBackticks
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.autocomplete.AutocompleteContext
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.loritta.morenitta.utils.Constants
import java.util.*

class RepCommand : SlashCommandDeclarationWrapper {
    private val I18N_PREFIX = I18nKeysData.Commands.Command.Rep

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.SOCIAL, UUID.fromString("912b9e16-bb52-47eb-afba-87ffabe9b6c9")) {
        subcommand(I18N_PREFIX.Give.Label, I18N_PREFIX.Give.Description, UUID.fromString("fb361757-9bf8-443c-83bb-733f24f482ae")) {
            executor = GiveRepExecutor()
        }

        subcommand(I18N_PREFIX.Delete.Label, I18N_PREFIX.Delete.Description, UUID.fromString("76c36a28-7eb1-4dac-86df-36b4da6cf13c")) {
            executor = DeleteRepExecutor()
        }
    }

    inner class GiveRepExecutor : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val user = user("user", I18N_PREFIX.Give.Options.User.Description)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val user = args[options.user].user

            if (context.user.id == user.id) {
                context.fail(true) {
                    this.styled(
                        context.i18nContext.get(I18N_PREFIX.Give.RepSelf),
                        Emotes.Error
                    )
                }
            }

            if (!context.loritta.pudding.sonhos.userGotDailyRecently(context.user.id.toLong(), 14)) {
                context.fail(true) {
                    this.styled(
                        context.i18nContext.get(
                            I18nKeysData.Commands.YouNeedToGetDailyRewardBeforeDoingThisAction(
                                context.loritta.commandMentions.daily
                            )
                        ),
                        Emotes.LoriSob
                    )
                }
            }

            val lastReputationGiven = context.loritta.pudding.reputations.getGivenReputationsByUser(context.user.id.toLong())
                .maxByOrNull { it.receivedAt }

            if (lastReputationGiven != null) {
                val diff = Clock.System.now().toEpochMilliseconds() - lastReputationGiven.receivedAt

                if (3_600_000 > diff) {
                    context.fail(true) {
                        this.styled(
                            context.i18nContext.get(
                                I18N_PREFIX.Give.Wait(
                                    "<t:${(lastReputationGiven.receivedAt + 3.6e+6.toLong()) / 1000}:R>"
                                )
                            ),
                            Emotes.Error
                        )
                    }
                }
            }

            var url = "${context.loritta.config.loritta.website.url}user/${user.id}/rep"

            if (context.guildId != null)
                url += "?guild=${context.guildId}&channel=${context.channel.idLong}"

            context.reply(true) {
                this.styled(
                    context.i18nContext.get(
                        I18N_PREFIX.Give.ReputationLink(url)
                    ),
                    Emotes.LoriHeart
                )
            }
        }
    }

    inner class DeleteRepExecutor : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val rep = string("rep", I18N_PREFIX.Delete.Options.Rep.Description) {
                autocomplete { context ->
                    val reputations = context.loritta.pudding.reputations.getReceivedReputationsByUser(
                        context.event.user.id.toLong()
                    )

                    return@autocomplete reputations.sortedByDescending { it.receivedAt }
                        .associate { formatReputation(it, context) to it.id.toString() }
                        .filterKeys { it.contains(context.event.interaction.focusedOption.value, true) }
                        .entries
                        .take(DiscordResourceLimits.Command.Options.ChoicesCount)
                        .associate { it.key to it.value }
                }
            }

            private suspend fun formatReputation(reputation: PuddingReputation, context: AutocompleteContext): String {
                return buildString {
                    val givenAtTime = Instant.fromEpochMilliseconds(reputation.receivedAt)
                        .toLocalDateTime(Constants.LORITTA_TIMEZONE.toKotlinTimeZone())

                    val year = givenAtTime.year
                    val month = givenAtTime.month.value.toString().padStart(2, '0')
                    val day = givenAtTime.dayOfMonth.toString().padStart(2, '0')
                    val hour = givenAtTime.hour.toString().padStart(2, '0')
                    val minute = givenAtTime.minute.toString().padStart(2, '0')

                    append("[$day/$month/$year $hour:$minute] ")

                    val user = context.loritta.lorittaShards.retrieveUserInfoById(reputation.givenById)
                    if (user == null)
                        append("Unknown: ")
                    else
                        append("${user.name}#${user.discriminator}: ")

                    append(reputation.content?.shortenAndStripCodeBackticks(50))
                }
            }
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val reputation = context.loritta.pudding.reputations.getReputation(args[options.rep].toLong())

            if (reputation == null || reputation.receivedById != context.user.id.toLong()) {
                context.fail(true) {
                    this.styled(
                        context.i18nContext.get(I18N_PREFIX.Delete.ReputationNotFound),
                        Emotes.Error
                    )
                }
            }

            reputation.delete()

            context.reply(true) {
                this.styled(
                    context.i18nContext.get(I18N_PREFIX.Delete.SuccessfullyDeleted),
                    Emotes.LoriHappy
                )
            }
        }
    }
}