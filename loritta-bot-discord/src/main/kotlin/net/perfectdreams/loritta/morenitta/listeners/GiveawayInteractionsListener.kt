package net.perfectdreams.loritta.morenitta.listeners

import dev.minn.jda.ktx.generics.getChannel
import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.messages.MessageEditData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.EmojiFightMatches
import net.perfectdreams.loritta.cinnamon.pudding.tables.EmojiFightMatchmakingResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.EmojiFightParticipants
import net.perfectdreams.loritta.cinnamon.pudding.tables.SentMessages
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GiveawayParticipants
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.Giveaways
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.AccountUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.utils.giveaway.GiveawayManager
import net.perfectdreams.loritta.serializable.GiveawayRoles
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.awt.Color
import java.sql.Connection
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

class GiveawayInteractionsListener(val m: LorittaBot) : ListenerAdapter() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val guild = event.guild ?: return

        if (event.componentId.startsWith(GiveawayManager.GIVEAWAY_JOIN_COMPONENT_PREFIX + ":")) {
            val dbId = event.componentId.substringAfter(":").toLong()

            GlobalScope.launch {
                val deferredReply = event.interaction.deferReply(true)
                    .await()

                val serverConfig = m.getOrCreateServerConfig(guild.idLong, true)
                val i18nContext = m.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

                // Serializable is used because REPEATABLE READ will cause issues if someone spam clicks the button, or if the giveaway ends at the same time someone clicks on the button
                val state = m.transaction(transactionIsolation = Connection.TRANSACTION_SERIALIZABLE) {
                    val giveaway = Giveaways.select { Giveaways.id eq dbId }
                        .firstOrNull() ?: return@transaction GiveawayState.UnknownGiveaway

                    // The giveaway has already finished!
                    if (giveaway[Giveaways.finished])
                        return@transaction GiveawayState.AlreadyFinished

                    if (GiveawayParticipants.select { GiveawayParticipants.giveawayId eq giveaway[Giveaways.id].value and (GiveawayParticipants.userId eq event.user.idLong) }.count() != 0L)
                        return@transaction GiveawayState.AlreadyParticipating

                    // Check if the user has all allowed roles
                    val allowedRoles = giveaway[Giveaways.allowedRoles]?.let { Json.decodeFromString<GiveawayRoles>(it) }

                    if (allowedRoles != null) {
                        val memberRoleIds = event.member!!.roles.map { it.idLong }.toSet()

                        if (allowedRoles.isAndCondition) {
                            val missingRoleIds = (allowedRoles.roleIds - memberRoleIds)

                            if (missingRoleIds.isNotEmpty())
                                return@transaction GiveawayState.MissingRoles(allowedRoles)
                        } else {
                            val hasAnyRole = allowedRoles.roleIds.any { it in memberRoleIds }
                            if (!hasAnyRole)
                                return@transaction GiveawayState.MissingRoles(allowedRoles)
                        }
                    }

                    // Check if the user does not have any blocked roles
                    val deniedRoles = giveaway[Giveaways.deniedRoles]?.let { Json.decodeFromString<GiveawayRoles>(it) }

                    if (deniedRoles != null) {
                        val memberRoleIds = event.member!!.roles.map { it.idLong }.toSet()

                        if (deniedRoles.isAndCondition) {
                            val hasAllRoles = deniedRoles.roleIds.all { it in deniedRoles.roleIds }

                            if (hasAllRoles)
                                return@transaction GiveawayState.BlockedRoles(deniedRoles)
                        } else {
                            val hasAnyRole = deniedRoles.roleIds.any { it in memberRoleIds }
                            if (hasAnyRole)
                                return@transaction GiveawayState.BlockedRoles(deniedRoles)
                        }
                    }

                    // Check if the user has got their daily reward today
                    val needsToGetDailyBeforeParticipating = giveaway[Giveaways.needsToGetDailyBeforeParticipating] ?: false
                    if (needsToGetDailyBeforeParticipating) {
                        val gotTodaysDailyReward = AccountUtils.getUserTodayDailyReward(m, event.user.idLong) != null

                        if (!gotTodaysDailyReward)
                            return@transaction GiveawayState.NeedsToGetDailyRewardBeforeParticipating
                    }

                    // This super globby mess is here because Exposed can't behave and select the correct columns
                    val innerJoin = EmojiFightParticipants.innerJoin(EmojiFightMatches.innerJoin(EmojiFightMatchmakingResults, { EmojiFightMatches.id }, { EmojiFightMatchmakingResults.match }), { EmojiFightParticipants.match }, { EmojiFightMatches.id })

                    val giveawayCreatedAt = giveaway[Giveaways.createdAt]
                    val selfServerEmojiFightBetVictories = giveaway[Giveaways.selfServerEmojiFightBetVictories]
                    if (selfServerEmojiFightBetVictories != null && selfServerEmojiFightBetVictories > 0 && giveawayCreatedAt != null) {
                        val matchesWon = innerJoin.select {
                            // Yes, it looks wonky, but it is correct
                            EmojiFightParticipants.user eq event.user.idLong and (EmojiFightMatchmakingResults.winner eq EmojiFightParticipants.id) and (EmojiFightMatchmakingResults.entryPrice neq 0) and (EmojiFightMatches.guild eq guild.idLong) and (EmojiFightMatches.createdAt greaterEq giveawayCreatedAt)
                        }.count()

                        if (selfServerEmojiFightBetVictories > matchesWon) {
                            return@transaction GiveawayState.NeedsToWinMoreEmojiFightBets(selfServerEmojiFightBetVictories, matchesWon)
                        }
                    }

                    val selfServerEmojiFightBetLosses = giveaway[Giveaways.selfServerEmojiFightBetLosses]
                    if (selfServerEmojiFightBetLosses != null && selfServerEmojiFightBetLosses > 0 && giveawayCreatedAt != null) {
                        val matchesLost = innerJoin.select {
                            // Yes, it looks wonky, but it is correct
                            EmojiFightParticipants.user eq event.user.idLong and (EmojiFightMatchmakingResults.winner neq EmojiFightParticipants.id) and (EmojiFightMatchmakingResults.entryPrice neq 0) and (EmojiFightMatches.guild eq guild.idLong) and (EmojiFightMatches.createdAt greaterEq giveawayCreatedAt)
                        }.count()

                        if (selfServerEmojiFightBetLosses > matchesLost) {
                            return@transaction GiveawayState.NeedsToLoseMoreEmojiFightBets(selfServerEmojiFightBetLosses, matchesLost)
                        }
                    }

                    val messagesRequired = giveaway[Giveaways.messagesRequired]
                    val messagesTimeThreshold = giveaway[Giveaways.messagesTimeThreshold]
                    if (messagesRequired != null && messagesTimeThreshold != null) {
                        val nowMinusRelativeTime = Instant.now()
                            .minusMillis(messagesTimeThreshold)
                        val messagesSentInTheGuild = SentMessages.select {
                            SentMessages.guildId eq guild.idLong and (SentMessages.userId eq event.user.idLong) and (SentMessages.sentAt greaterEq nowMinusRelativeTime)
                        }.count()

                        if (messagesRequired > messagesSentInTheGuild) {
                            return@transaction GiveawayState.NeedsMoreMessages
                        }
                    }

                    GiveawayParticipants.insert {
                        it[GiveawayParticipants.userId] = event.user.idLong
                        it[GiveawayParticipants.giveawayId] = dbId
                        it[GiveawayParticipants.joinedAt] = Instant.now()
                    }

                    val participants =
                        GiveawayParticipants.select { GiveawayParticipants.giveawayId eq giveaway[Giveaways.id].value }
                            .count()

                    return@transaction GiveawayState.Success(giveaway, participants, allowedRoles, deniedRoles)
                }

                when (state) {
                    GiveawayState.UnknownGiveaway -> {
                        deferredReply.editOriginal(
                            MessageEdit {
                                styled(
                                    i18nContext.get(GiveawayManager.I18N_PREFIX.JoinGiveaway.UnknownGiveaway),
                                    Emotes.LoriSob
                                )
                            }
                        ).await()
                    }

                    GiveawayState.AlreadyFinished -> {
                        deferredReply.editOriginal(
                            MessageEdit {
                                styled(
                                    i18nContext.get(GiveawayManager.I18N_PREFIX.JoinGiveaway.GiveawayHasAlreadyEnded),
                                    Emotes.LoriSob
                                )
                            }
                        )
                            .await()
                    }

                    GiveawayState.AlreadyParticipating -> {
                        deferredReply.editOriginal(
                            MessageEdit {
                                styled(
                                    i18nContext.get(GiveawayManager.I18N_PREFIX.JoinGiveaway.YouAreAlreadyParticipating),
                                    Emotes.LoriSob
                                )

                                actionRow(
                                    m.interactivityManager.buttonForUser(event.user, ButtonStyle.DANGER, i18nContext.get(GiveawayManager.I18N_PREFIX.JoinGiveaway.LeaveGiveaway), { loriEmoji = Emotes.LoriBear }) {
                                        val deferredReply = it.event.deferReply(true).await()

                                        // First we will try to make the user leave the giveaway
                                        val leftGiveaway = m.transaction {
                                            // Shouldn't ever be null here (unless if it is was deleted somewhere else)
                                            val giveaway = Giveaways.select { Giveaways.id eq dbId }
                                                .first()

                                            // Delete the user's entry...
                                            GiveawayParticipants.deleteWhere {
                                                userId eq event.user.idLong and (giveawayId eq dbId)
                                            }

                                            // Get all participants...
                                            val participants = GiveawayParticipants.select { GiveawayParticipants.giveawayId eq giveaway[Giveaways.id].value }
                                                .count()

                                            // Parse the allowed roles into JSON objects
                                            val allowedRoles = giveaway[Giveaways.allowedRoles]?.let {
                                                Json.decodeFromString<GiveawayRoles>(it)
                                            }
                                            val deniedRoles = giveaway[Giveaways.deniedRoles]?.let {
                                                Json.decodeFromString<GiveawayRoles>(it)
                                            }

                                            // And that's a wrap!
                                            LeftGiveaway(giveaway, participants, allowedRoles, deniedRoles)
                                        }

                                        // Update the giveaway message to indicate that the user left
                                        m.giveawayManager.giveawayMessageUpdateJobs[dbId]?.cancel()
                                        m.giveawayManager.giveawayMessageUpdateJobs[dbId] = GlobalScope.launch(m.coroutineDispatcher) {
                                            // We have a 1s delay before *really* updating the message
                                            delay(1.seconds)

                                            guild.getGuildMessageChannelById(leftGiveaway.giveaway[Giveaways.textChannelId])
                                                ?.editMessageById(
                                                    leftGiveaway.giveaway[Giveaways.messageId],
                                                    MessageEditData.fromCreateData(
                                                        m.giveawayManager.createGiveawayMessage(
                                                            m.languageManager.getI18nContextByLegacyLocaleId(
                                                                leftGiveaway.giveaway[Giveaways.locale]
                                                            ),
                                                            leftGiveaway.giveaway[Giveaways.reason],
                                                            leftGiveaway.giveaway[Giveaways.description],
                                                            leftGiveaway.giveaway[Giveaways.reaction],
                                                            leftGiveaway.giveaway[Giveaways.imageUrl],
                                                            leftGiveaway.giveaway[Giveaways.thumbnailUrl],
                                                            leftGiveaway.giveaway[Giveaways.color]?.let {
                                                                Color.decode(
                                                                    it
                                                                )
                                                            },
                                                            leftGiveaway.giveaway[Giveaways.finishAt],
                                                            event.guild!!,
                                                            leftGiveaway.giveaway[Giveaways.customMessage],
                                                            leftGiveaway.giveaway[Giveaways.id].value,
                                                            leftGiveaway.participants,
                                                            leftGiveaway.allowedRoles,
                                                            leftGiveaway.deniedRoles
                                                        )
                                                    )
                                                )
                                                ?.await()

                                            m.giveawayManager.giveawayMessageUpdateJobs.remove(dbId)
                                        }

                                        // Tell the user that they left the giveaway
                                        deferredReply.editOriginal(
                                            MessageEdit {
                                                styled(
                                                    i18nContext.get(GiveawayManager.I18N_PREFIX.JoinGiveaway.YouLeftTheGiveaway),
                                                    Emotes.LoriSob
                                                )
                                            }).await()
                                    }
                                )
                            })
                            .await()
                    }

                    is GiveawayState.MissingRoles -> {
                        if (state.allowedRoles.isAndCondition) {
                            deferredReply.editOriginal(
                                MessageEdit {
                                    styled(
                                        i18nContext.get(GiveawayManager.I18N_PREFIX.JoinGiveaway.MissingRolesAnd(state.allowedRoles.roleIds.joinToString { "<@&${it}>" })),
                                        Emotes.LoriSob
                                    )
                                })
                                .await()
                        } else {
                            deferredReply.editOriginal(
                                MessageEdit {
                                    styled(
                                        i18nContext.get(GiveawayManager.I18N_PREFIX.JoinGiveaway.MissingRolesOr(state.allowedRoles.roleIds.joinToString { "<@&${it}>" })),
                                        Emotes.LoriSob
                                    )
                                })
                                .await()
                        }
                    }

                    is GiveawayState.BlockedRoles -> {
                        if (state.deniedRoles.isAndCondition) {
                            deferredReply.editOriginal(
                                MessageEdit {
                                    styled(
                                        i18nContext.get(GiveawayManager.I18N_PREFIX.JoinGiveaway.BlockedRolesAnd(state.deniedRoles.roleIds.joinToString { "<@&${it}>" })),
                                        Emotes.LoriSob
                                    )
                                })
                                .await()
                        } else {
                            deferredReply.editOriginal(
                                MessageEdit {
                                    styled(
                                        i18nContext.get(GiveawayManager.I18N_PREFIX.JoinGiveaway.BlockedRolesOr(state.deniedRoles.roleIds.joinToString { "<@&${it}>" })),
                                        Emotes.LoriSob
                                    )
                                })
                                .await()
                        }
                    }

                    is GiveawayState.NeedsToGetDailyRewardBeforeParticipating -> {
                        deferredReply.editOriginal(
                            MessageEdit {
                                styled(
                                    i18nContext.get(GiveawayManager.I18N_PREFIX.JoinGiveaway.YouNeedToGetTheDailyRewardBeforeParticipating),
                                    Emotes.LoriSob
                                )
                            })
                            .await()
                    }

                    is GiveawayState.NeedsToWinMoreEmojiFightBets -> {
                        deferredReply.editOriginal(
                            MessageEdit {
                                styled(
                                    i18nContext.get(GiveawayManager.I18N_PREFIX.JoinGiveaway.YouNeedToWinMoreEmojiFightBets(state.matchesRequired, state.matchesAlreadyWon)),
                                    Emotes.LoriSob
                                )
                            })
                            .await()
                    }

                    is GiveawayState.NeedsToLoseMoreEmojiFightBets -> {
                        deferredReply.editOriginal(
                            MessageEdit {
                                styled(
                                    i18nContext.get(GiveawayManager.I18N_PREFIX.JoinGiveaway.YouNeedToLoseMoreEmojiFightBets(state.matchesRequired, state.matchesAlreadyLost)),
                                    Emotes.LoriSob
                                )
                            })
                            .await()
                    }

                    is GiveawayState.NeedsMoreMessages -> {
                        deferredReply.editOriginal(
                            MessageEdit {
                                styled(
                                    i18nContext.get(GiveawayManager.I18N_PREFIX.JoinGiveaway.NeedsMoreMessages),
                                    Emotes.LoriSob
                                )
                            })
                            .await()
                    }

                    is GiveawayState.Success -> {
                        val giveaway = state.giveaway

                        deferredReply.editOriginal(
                            MessageEdit {
                                styled(
                                    i18nContext.get(GiveawayManager.I18N_PREFIX.JoinGiveaway.YouAreNowParticipating),
                                    Emotes.LoriYay
                                )
                            })
                            .await()

                        m.giveawayManager.giveawayMessageUpdateMutexes.getOrPut(dbId) { Mutex() }.withLock {
                            m.giveawayManager.giveawayMessageUpdateJobs[dbId]?.cancel()
                            m.giveawayManager.giveawayMessageUpdateJobs[dbId] =
                                GlobalScope.launch(m.coroutineDispatcher) {
                                    // We have a 5s delay before *really* updating the message
                                    delay(5.seconds)

                                    val channel = event.jda.getChannel<MessageChannel>(giveaway[Giveaways.textChannelId])

                                    if (channel == null) {
                                        logger.warn { "Couldn't update giveaway ${giveaway[Giveaways.id]} because the channel ${giveaway[Giveaways.textChannelId]} doesn't exist!" }
                                        return@launch
                                    }

                                    channel.editMessageById(
                                        giveaway[Giveaways.messageId],
                                        MessageEditData.fromCreateData(
                                            m.giveawayManager.createGiveawayMessage(
                                                m.languageManager.getI18nContextByLegacyLocaleId(giveaway[Giveaways.locale]),
                                                giveaway[Giveaways.reason],
                                                giveaway[Giveaways.description],
                                                giveaway[Giveaways.reaction],
                                                giveaway[Giveaways.imageUrl],
                                                giveaway[Giveaways.thumbnailUrl],
                                                giveaway[Giveaways.color]?.let { Color.decode(it) },
                                                giveaway[Giveaways.finishAt],
                                                event.guild!!,
                                                giveaway[Giveaways.customMessage],
                                                giveaway[Giveaways.id].value,
                                                state.participants,
                                                state.allowedRoles,
                                                state.deniedRoles
                                            )
                                        )
                                    ).await()

                                    m.giveawayManager.giveawayMessageUpdateJobs.remove(dbId)
                                }
                        }
                    }
                }
            }
        } else if (event.componentId.startsWith(GiveawayManager.GIVEAWAY_PARTICIPANTS_COMPONENT_PREFIX + ":")) {
            val dbId = event.componentId.substringAfter(":").toLong()

            GlobalScope.launch {
                val deferredReply = event.interaction.deferReply(true)
                    .await()

                val serverConfig = m.getOrCreateServerConfig(guild.idLong, true)
                val i18nContext = m.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

                // Get all participants of the giveaway and create a nice list with all of them
                val participants = m.transaction {
                    GiveawayParticipants.select { GiveawayParticipants.giveawayId eq dbId }
                        .orderBy(GiveawayParticipants.joinedAt, SortOrder.ASC)
                        .map { it[GiveawayParticipants.userId] }
                }

                val members = participants.associateWith { m.lorittaShards.retrieveUserInfoById(it) }
                val participantsText = StringBuilder()
                members.forEach { (id, info) ->
                    if (info != null) {
                        participantsText.appendLine("${info.name}#${info.discriminator} (${info.id})")
                    } else {
                        participantsText.appendLine(id)
                    }
                }

                deferredReply.editOriginal(
                    MessageEdit {
                        styled(
                            i18nContext.get(GiveawayManager.I18N_PREFIX.GiveawayParticipants.AllDone)
                        )
                    }
                )
                    .setFiles(FileUpload.fromData(participantsText.toString().toByteArray(Charsets.UTF_8), "participants.txt"))
                    .await()
            }
        }
    }

    sealed class GiveawayState {
        object UnknownGiveaway : GiveawayState()
        object AlreadyFinished : GiveawayState()
        object AlreadyParticipating : GiveawayState()
        object NeedsToGetDailyRewardBeforeParticipating : GiveawayState()
        class NeedsToWinMoreEmojiFightBets(val matchesRequired: Int, val matchesAlreadyWon: Long) : GiveawayState()
        class NeedsToLoseMoreEmojiFightBets(val matchesRequired: Int, val matchesAlreadyLost: Long) : GiveawayState()
        object NeedsMoreMessages : GiveawayState()
        class MissingRoles(val allowedRoles: GiveawayRoles) : GiveawayState()
        class BlockedRoles(val deniedRoles: GiveawayRoles) : GiveawayState()
        class Success(val giveaway: ResultRow, val participants: Long, val allowedRoles: GiveawayRoles?, val deniedRoles: GiveawayRoles?) : GiveawayState()
    }

    class LeftGiveaway(val giveaway: ResultRow, val participants: Long, val allowedRoles: GiveawayRoles?, val deniedRoles: GiveawayRoles?)
}