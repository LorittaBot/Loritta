package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import dev.minn.jda.ktx.messages.InlineMessage
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import java.util.*

class SonhosAtmExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
    companion object {
        val SONHOS_I18N_PREFIX = I18nKeysData.Commands.Command.Sonhosatm
        private val logger by HarmonyLoggerFactory.logger {}

        suspend fun executeSonhosAtm(
            loritta: LorittaBot,
            context: UnleashedContext,
            isEphemeral: Boolean,
            user: User,
            informationType: InformationType
        ) {
            context.deferChannelMessage(isEphemeral) // Defer because this sometimes takes too long

            val profile = context.loritta.pudding.users.getUserProfile(net.perfectdreams.loritta.serializable.UserId(user.idLong))
            val userSonhos = profile?.money ?: 0L
            val isSelf = context.user.id == user.id

            // Needs to be in here because MessageBuilder is not suspendable!
            val sonhosRankPosition = if (userSonhos != 0L && profile != null) // Only show the ranking position if the user has any sonhos, this avoids querying the db with useless stuff
                profile.getRankPositionInSonhosRanking()
            else
                null

            var extendedSonhosInfo: ExtendedSonhosInfo? = null

            if (informationType == InformationType.EXTENDED) {
                // Get how many sonecas the user has on SparklyPower
                val sparklySonecasResult = run {
                    try {
                        return@run withTimeout(5_000) {
                            val response = loritta.http.get("${loritta.config.loritta.sparklyPower.sparklySurvivalUrl.removeSuffix("/")}/loritta/${user.idLong}/sonecas")

                            // User does not have account
                            if (response.status == HttpStatusCode.NotFound)
                                return@withTimeout ExtendedSonhosInfo.SparklySonecasNotFound

                            if (!response.status.isSuccess()) {
                                return@withTimeout ExtendedSonhosInfo.SparklySonecasFailure
                            }

                            val json = Json.parseToJsonElement(response.bodyAsText())
                                .jsonObject

                            return@withTimeout ExtendedSonhosInfo.SparklySonecasSuccess(
                                UUID.fromString(json["userUniqueId"]!!.jsonPrimitive.content),
                                json["username"]!!.jsonPrimitive.content,
                                json["sonecas"]!!.jsonPrimitive.double
                            )
                        }
                    } catch (e: Exception) {
                        if (e is TimeoutCancellationException) {
                            logger.warn { "Took too long to get ${user.idLong}'s sonecas in SparklyPower! Ignoring..." }
                        } else {
                            logger.warn(e) { "Something went wrong while trying to get ${user.idLong}'s sonecas in SparklyPower!" }
                        }
                        return@run ExtendedSonhosInfo.SparklySonecasFailure
                    }
                }

                extendedSonhosInfo = ExtendedSonhosInfo(sparklySonecasResult)
            }

            fun InlineMessage<*>.addExtendedSonhosInfoEmbed(extendedSonhosInfo: ExtendedSonhosInfo) {
                val totalSonhos = userSonhos + extendedSonhosInfo.totalSonhos

                embed {
                    title = "${Emotes.Sonhos3} ${context.i18nContext.get(SONHOS_I18N_PREFIX.SonhosSummary)}"

                    field(
                        "${Emotes.LoriCard} ${context.i18nContext.get(SONHOS_I18N_PREFIX.SonhosInTheWallet)}",
                        context.i18nContext.get(SONHOS_I18N_PREFIX.SonhosField(userSonhos)),
                        false
                    )

                    when (val result = extendedSonhosInfo.sparklySonecas) {
                        ExtendedSonhosInfo.SparklySonecasFailure -> {
                            field(
                                "${Emotes.PantufaPickaxe} ${context.i18nContext.get(SONHOS_I18N_PREFIX.SparklySonecasUnknown)}",
                                "*${context.i18nContext.get(SONHOS_I18N_PREFIX.SparklySonecasFail)}*",
                                false
                            )
                        }
                        ExtendedSonhosInfo.SparklySonecasNotFound -> {
                            field(
                                "${Emotes.PantufaPickaxe} ${context.i18nContext.get(SONHOS_I18N_PREFIX.SparklySonecasUnknown)}",
                                "*${context.i18nContext.get(SONHOS_I18N_PREFIX.SparklySonecasAccountNotConnected(user.asMention))}*",
                                false
                            )
                        }
                        is ExtendedSonhosInfo.SparklySonecasSuccess -> {
                            field(
                                "${Emotes.PantufaPickaxe} ${context.i18nContext.get(SONHOS_I18N_PREFIX.SparklySonecasPlayerName(result.username))}",
                                context.i18nContext.get(SONHOS_I18N_PREFIX.SonhosField(result.sonhos)),
                                false
                            )
                        }
                    }

                    field(
                        "${SonhosUtils.getSonhosEmojiOfQuantity(totalSonhos)} ${context.i18nContext.get(SONHOS_I18N_PREFIX.TotalSonhos)}",
                        context.i18nContext.get(SONHOS_I18N_PREFIX.SonhosField(totalSonhos)),
                        false
                    )

                    color = LorittaColors.LorittaAqua.rgb
                }

            }
            if (isSelf) {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(
                            SONHOS_I18N_PREFIX.YouHaveSonhos(
                                SonhosUtils.getSonhosEmojiOfQuantity(userSonhos),
                                userSonhos,
                                if (sonhosRankPosition != null) {
                                    SONHOS_I18N_PREFIX.YourCurrentRankPosition(
                                        sonhosRankPosition,
                                        loritta.commandMentions.sonhosRank
                                    )
                                } else {
                                    ""
                                }
                            )
                        ),
                        Emotes.LoriRich
                    )

                    val extendedSonhosInfo = extendedSonhosInfo
                    if (extendedSonhosInfo != null)
                        addExtendedSonhosInfoEmbed(extendedSonhosInfo)
                }

                if (context is ApplicationCommandContext) {
                    SonhosUtils.sendEphemeralMessageIfUserHaventGotDailyRewardToday(
                        context.loritta,
                        context,
                        net.perfectdreams.loritta.serializable.UserId(user.idLong)
                    )
                }
            } else {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(
                            SONHOS_I18N_PREFIX.UserHasSonhos(
                                user.asMention, // We don't want to notify the user!
                                SonhosUtils.getSonhosEmojiOfQuantity(userSonhos),
                                userSonhos,
                                if (sonhosRankPosition != null) {
                                    SONHOS_I18N_PREFIX.UserCurrentRankPosition(
                                        user.asMention, // Again, we don't want to notify the user!
                                        sonhosRankPosition,
                                        loritta.commandMentions.sonhosRank
                                    )
                                } else {
                                    ""
                                }
                            )
                        ),
                        Emotes.LoriRich
                    )

                    val extendedSonhosInfo = extendedSonhosInfo
                    if (extendedSonhosInfo != null)
                        addExtendedSonhosInfoEmbed(extendedSonhosInfo)
                }
            }
        }
    }

    inner class Options : ApplicationCommandOptions() {
        val user = optionalUser("user", SONHOS_I18N_PREFIX.Options.User)

        val informationType = optionalString("information_type", SONHOS_I18N_PREFIX.Options.InformationType.Text) {
            choice(SONHOS_I18N_PREFIX.Options.InformationType.Choice.Normal, InformationType.NORMAL.name)
            choice(SONHOS_I18N_PREFIX.Options.InformationType.Choice.Extended, InformationType.EXTENDED.name)
        }
    }

    override val options = Options()

    override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
        executeSonhosAtm(
            loritta,
            context,
            false,
            args[options.user]?.user ?: context.user,
            args[options.informationType]?.let { InformationType.valueOf(it) } ?: InformationType.NORMAL
        )
    }

    enum class InformationType {
        NORMAL,
        EXTENDED
    }

    data class ExtendedSonhosInfo(
        val sparklySonecas: SparklySonecasResult
    ) {
        /**
         * Gets the total sonhos of everything related to this [ExtendedSonhosInfo]
         */
        val totalSonhos
            get() = run {
                var totalSonhos = 0L
                if (sparklySonecas is SparklySonecasSuccess)
                    totalSonhos += sparklySonecas.sonhos
                return@run totalSonhos
            }

        sealed class SparklySonecasResult
        data class SparklySonecasSuccess(val userId: UUID, val username: String, val sonecas: Double) : SparklySonecasResult() {
            val sonhos: Long
                get() = (sonecas / 2).toLong()
        }
        data object SparklySonecasNotFound : SparklySonecasResult()
        data object SparklySonecasFailure : SparklySonecasResult()
    }

    override suspend fun convertToInteractionsArguments(
        context: LegacyMessageCommandContext,
        args: List<String>
    ): Map<OptionReference<*>, Any?> {
        return mapOf(
            options.user to context.getUserAndMember(0)
        )
    }
}