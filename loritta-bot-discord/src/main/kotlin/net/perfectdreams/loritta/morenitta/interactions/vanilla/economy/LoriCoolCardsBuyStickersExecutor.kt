package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import dev.minn.jda.ktx.coroutines.await
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.json.Json
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils.appendUserHaventGotDailyTodayOrUpsellSonhosBundles
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsEvents
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsUserBoughtBoosterPacks
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedButton
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.components.ComponentContext
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.LoriCoolCardsBuyStickersExecutor.BuyStickersResult.NotEnoughSonhos
import net.perfectdreams.loritta.morenitta.loricoolcards.StickerAlbumTemplate
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.serializable.StoredLoriCoolCardsBoughtBoosterPackSonhosTransaction
import net.perfectdreams.loritta.serializable.UserId
import org.jetbrains.exposed.sql.*
import java.time.Instant
import kotlin.time.measureTimedValue

class LoriCoolCardsBuyStickersExecutor(val loritta: LorittaBot, private val loriCoolCardsCommand: LoriCoolCardsCommand) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Loricoolcards.Buy
        private val logger by HarmonyLoggerFactory.logger {}
    }

    override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
        context.deferChannelMessage(false)

        buyStickers(context)
    }

    suspend fun buyStickers(context: UnleashedContext) {
        if (SonhosUtils.checkIfEconomyIsDisabled(context))
            return

        // We expect that this is already deferred by the caller
        val now = Instant.now()

        logger.info { "User ${context.user.idLong} is *starting* to buy a booster pack! Let's get the event info..." }

        // Load the current active event
        val result = loritta.transaction {
            // First we will get the active cards event to get the album template
            val event = LoriCoolCardsEvents.selectAll().where {
                LoriCoolCardsEvents.endsAt greaterEq now and (LoriCoolCardsEvents.startsAt lessEq now)
            }.firstOrNull() ?: return@transaction BuyStickersPreResult.EventUnavailable

            val template = Json.decodeFromString<StickerAlbumTemplate>(event[LoriCoolCardsEvents.template])
            if (template.boosterPacksPurchaseAvailableAfter != null && template.boosterPacksPurchaseAvailableAfter > now.toKotlinInstant())
                return@transaction BuyStickersPreResult.StoreNotAvailable(template.boosterPacksPurchaseAvailableAfter.toJavaInstant())

            BuyStickersPreResult.Success(template)
        }

        logger.info { "Got event info information for ${context.user.idLong}'s *starting* to buy a booster pack thingy!" }

        when (result) {
            BuyStickersPreResult.EventUnavailable -> {
                context.reply(false) {
                    styled(
                        "Nenhum evento de figurinhas ativo"
                    )
                }
            }
            is BuyStickersPreResult.StoreNotAvailable -> {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(
                            I18N_PREFIX.StoreNotAvailable(
                                timestamp = DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(result.opensAt),
                                dailyCommand = loritta.commandMentions.daily
                            )
                        ),
                        Emotes.LoriSleeping
                    )
                }
            }
            is BuyStickersPreResult.Success -> {
                val buyBoosterPack1xButton = UnleashedButton.of(
                    ButtonStyle.PRIMARY,
                    context.i18nContext.get(I18N_PREFIX.BuyAndOpenBoosterPack(1, result.template.sonhosPrice))
                )

                val buyBoosterPack2xButton = UnleashedButton.of(
                    ButtonStyle.PRIMARY,
                    context.i18nContext.get(I18N_PREFIX.BuyAndOpenBoosterPack(2, result.template.sonhosPrice * 2))
                )

                val buyBoosterPack3xButton = UnleashedButton.of(
                    ButtonStyle.PRIMARY,
                    context.i18nContext.get(I18N_PREFIX.BuyAndOpenBoosterPack(3, result.template.sonhosPrice * 3))
                )

                val buyBoosterPack4xButton = UnleashedButton.of(
                    ButtonStyle.PRIMARY,
                    context.i18nContext.get(I18N_PREFIX.BuyAndOpenBoosterPack(4, result.template.sonhosPrice * 4))
                )

                val buyBoosterPack5xButton = UnleashedButton.of(
                    ButtonStyle.PRIMARY,
                    context.i18nContext.get(I18N_PREFIX.BuyAndOpenBoosterPack(5, result.template.sonhosPrice * 5))
                )

                val clickCallback: suspend (ComponentContext, Int) -> (Unit) = { it, stickerPacksCount ->
                    val future = it.deferEditAsync()

                    logger.info { "User ${context.user.idLong} is buying ${stickerPacksCount}x booster packs! Giving stickers and stuff..." }

                    val (result, time) = measureTimedValue {
                        loritta.transaction {
                            // First we will get the active cards event
                            // OPTIMIZATION: Only get the event ID, we don't need the rest of the things (like the template data) anyway
                            val event = LoriCoolCardsEvents.select(LoriCoolCardsEvents.id).where { 
                                LoriCoolCardsEvents.endsAt greaterEq now and (LoriCoolCardsEvents.startsAt lessEq now)
                            }.firstOrNull() ?: return@transaction BuyStickersResult.EventUnavailable

                            // We don't *need* to check if the store is open because, due to the way it works, it would be impossible for us to get to this part WITHOUT the store being open

                            // Check if we have enough money to buy the booster pack
                            val userProfile = loritta.getOrCreateLorittaProfile(context.user.idLong)
                            val priceOfAllStickerPacksThatTheUserWants = result.template.sonhosPrice * stickerPacksCount
                            if (priceOfAllStickerPacksThatTheUserWants > userProfile.money)
                                return@transaction NotEnoughSonhos(userProfile.money, priceOfAllStickerPacksThatTheUserWants)

                            Profiles.update({ Profiles.id eq context.user.idLong }) {
                                with(SqlExpressionBuilder) {
                                    it.update(Profiles.money, Profiles.money - priceOfAllStickerPacksThatTheUserWants)
                                }
                            }

                            // Technically this is *bad* for performance, but it is what it is rn
                            repeat(stickerPacksCount) {
                                val boosterPackId = LoriCoolCardsUserBoughtBoosterPacks.insertAndGetId {
                                    it[LoriCoolCardsUserBoughtBoosterPacks.user] = context.user.idLong
                                    it[LoriCoolCardsUserBoughtBoosterPacks.event] = event[LoriCoolCardsEvents.id]
                                    it[LoriCoolCardsUserBoughtBoosterPacks.boughtAt] = Instant.now()
                                }

                                // Cinnamon transactions log
                                SimpleSonhosTransactionsLogUtils.insert(
                                    context.user.idLong,
                                    now,
                                    TransactionType.LORI_COOL_CARDS,
                                    result.template.sonhosPrice,
                                    StoredLoriCoolCardsBoughtBoosterPackSonhosTransaction(
                                        event[LoriCoolCardsEvents.id].value,
                                        boosterPackId.value
                                    )
                                )
                            }

                            BuyStickersResult.Success
                        }
                    }

                    logger.info { "User ${context.user.idLong} bought ${stickerPacksCount}x booster packs! - Took $time" }

                    val hook = future.await()

                    when (result) {
                        BuyStickersResult.EventUnavailable -> {
                            it.reply(true) {
                                styled(
                                    "Nenhum evento de figurinhas ativo"
                                )
                            }
                        }
                        is BuyStickersResult.NotEnoughSonhos -> {
                            it.reply(true) {
                                styled(
                                    context.i18nContext.get(SonhosUtils.insufficientSonhos(result.userSonhos, result.howMuch)),
                                    Emotes.LoriSob
                                )

                                appendUserHaventGotDailyTodayOrUpsellSonhosBundles(
                                    context.loritta,
                                    context.i18nContext,
                                    UserId(context.user.idLong),
                                    "lori-cool-cards",
                                    "buy-booster-pack-not-enough-sonhos"
                                )
                            }
                        }
                        is BuyStickersResult.Success -> {
                            it.reply(false) {
                                styled(
                                    context.i18nContext.get(I18N_PREFIX.YouBoughtXStickerPacks(stickerPacksCount, loritta.commandMentions.loriCoolCardsOpen)),
                                    Emotes.LoriCoolSticker
                                )
                            }
                        }
                    }
                }

                context.reply(false) {
                    embed {
                        title = "${Emotes.LoriCoolSticker} Pacote de Figurinhas"
                        description = "${Emotes.Scissors} ${context.i18nContext.get(I18N_PREFIX.HowManyBoosterPacksDoYouWantToBuy)}"
                        color = LorittaColors.LorittaAqua.rgb

                        image = result.template.stickerPackImageUrl
                    }

                    actionRow(
                        loritta.interactivityManager.buttonForUser(
                            context.user,
                            context.alwaysEphemeral,
                            buyBoosterPack1xButton
                        ) {
                            clickCallback.invoke(it, 1)
                        },
                        loritta.interactivityManager.buttonForUser(
                            context.user,
                            context.alwaysEphemeral,
                            buyBoosterPack2xButton
                        ) {
                            clickCallback.invoke(it, 2)
                        },
                        loritta.interactivityManager.buttonForUser(
                            context.user,
                            context.alwaysEphemeral,
                            buyBoosterPack3xButton
                        ) {
                            clickCallback.invoke(it, 3)
                        },
                        loritta.interactivityManager.buttonForUser(
                            context.user,
                            context.alwaysEphemeral,
                            buyBoosterPack4xButton
                        ) {
                            clickCallback.invoke(it, 4)
                        },
                        loritta.interactivityManager.buttonForUser(
                            context.user,
                            context.alwaysEphemeral,
                            buyBoosterPack5xButton
                        ) {
                            clickCallback.invoke(it, 5)
                        }
                    )
                }
            }
        }
    }

    sealed class BuyStickersPreResult {
        data object EventUnavailable : BuyStickersPreResult()
        data class StoreNotAvailable(val opensAt: Instant) : BuyStickersPreResult()
        class Success(
            val template: StickerAlbumTemplate
        ) : BuyStickersPreResult()
    }

    sealed class BuyStickersResult {
        data object EventUnavailable : BuyStickersResult()
        class NotEnoughSonhos(val userSonhos: Long, val howMuch: Long) : BuyStickersResult()
        data object Success
    }

    override suspend fun convertToInteractionsArguments(
        context: LegacyMessageCommandContext,
        args: List<String>
    ): Map<OptionReference<*>, Any?>? {
        return mapOf()
    }
}