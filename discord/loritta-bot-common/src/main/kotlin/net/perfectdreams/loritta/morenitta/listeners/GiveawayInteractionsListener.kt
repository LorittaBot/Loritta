package net.perfectdreams.loritta.morenitta.listeners

import dev.minn.jda.ktx.messages.MessageCreate
import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.messages.MessageEditData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.tables.servers.GiveawayParticipants
import net.perfectdreams.loritta.morenitta.tables.servers.Giveaways
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.giveaway.GiveawayManager
import net.perfectdreams.loritta.serializable.GiveawayRoles
import org.jetbrains.exposed.sql.*
import java.awt.Color
import java.time.Instant

class GiveawayInteractionsListener(val m: LorittaBot) : ListenerAdapter() {
    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val guild = event.guild ?: return

        if (event.componentId.startsWith(GiveawayManager.GIVEAWAY_JOIN_COMPONENT_PREFIX + ":")) {
            val dbId = event.componentId.substringAfter(":").toLong()

            GlobalScope.launch {
                val deferredEdit = event.interaction.deferEdit()
                    .await()

                val serverConfig = m.getOrCreateServerConfig(guild.idLong, true)
                val i18nContext = m.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

                m.giveawayManager.giveawayMutexes.getOrPut(dbId) { Mutex() }
                    .withLock {
                        val state = m.transaction {
                            val giveaway = Giveaways.select { Giveaways.id eq dbId }
                                .firstOrNull() ?: return@transaction GiveawayState.UnknownGiveaway

                            // The giveaway has already finished!
                            if (giveaway[Giveaways.finished])
                                return@transaction GiveawayState.AlreadyFinished

                            if (GiveawayParticipants.select { GiveawayParticipants.giveawayId eq giveaway[Giveaways.id].value and (GiveawayParticipants.userId eq event.user.idLong) }
                                    .count() != 0L)
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
                                deferredEdit.sendMessage(
                                    MessageCreate {
                                        styled(
                                            i18nContext.get(GiveawayManager.I18N_PREFIX.JoinGiveaway.UnknownGiveaway),
                                            Emotes.LoriSob
                                        )
                                    }
                                )
                                    .setEphemeral(true)
                                    .await()
                            }

                            GiveawayState.AlreadyFinished -> {
                                deferredEdit.sendMessage(MessageCreate {
                                    styled(
                                        i18nContext.get(GiveawayManager.I18N_PREFIX.JoinGiveaway.GiveawayHasAlreadyEnded),
                                        Emotes.LoriSob
                                    )
                                })
                                    .setEphemeral(true)
                                    .await()
                            }

                            GiveawayState.AlreadyParticipating -> {
                                deferredEdit.sendMessage(
                                    MessageCreate {
                                        styled(
                                            i18nContext.get(GiveawayManager.I18N_PREFIX.JoinGiveaway.YouAreAlreadyParticipating),
                                            Emotes.LoriSob
                                        )
                                    })
                                    .setEphemeral(true)
                                    .await()
                            }

                            is GiveawayState.MissingRoles -> {
                                if (state.allowedRoles.isAndCondition) {
                                    deferredEdit.sendMessage(
                                        MessageCreate {
                                            styled(
                                                i18nContext.get(GiveawayManager.I18N_PREFIX.JoinGiveaway.MissingRolesAnd(state.allowedRoles.roleIds.joinToString { "<@&${it}>" })),
                                                Emotes.LoriSob
                                            )
                                        })
                                        .setEphemeral(true)
                                        .await()
                                } else {
                                    deferredEdit.sendMessage(
                                        MessageCreate {
                                            styled(
                                                i18nContext.get(GiveawayManager.I18N_PREFIX.JoinGiveaway.MissingRolesOr(state.allowedRoles.roleIds.joinToString { "<@&${it}>" })),
                                                Emotes.LoriSob
                                            )
                                        })
                                        .setEphemeral(true)
                                        .await()
                                }
                            }

                            is GiveawayState.BlockedRoles -> {
                                if (state.deniedRoles.isAndCondition) {
                                    deferredEdit.sendMessage(
                                        MessageCreate {
                                            styled(
                                                i18nContext.get(GiveawayManager.I18N_PREFIX.JoinGiveaway.BlockedRolesAnd(state.deniedRoles.roleIds.joinToString { "<@&${it}>" })),
                                                Emotes.LoriSob
                                            )
                                        })
                                        .setEphemeral(true)
                                        .await()
                                } else {
                                    deferredEdit.sendMessage(
                                        MessageCreate {
                                            styled(
                                                i18nContext.get(GiveawayManager.I18N_PREFIX.JoinGiveaway.BlockedRolesOr(state.deniedRoles.roleIds.joinToString { "<@&${it}>" })),
                                                Emotes.LoriSob
                                            )
                                        })
                                        .setEphemeral(true)
                                        .await()
                                }
                            }

                            is GiveawayState.Success -> {
                                val giveaway = state.giveaway

                                deferredEdit.sendMessage(
                                    MessageCreate {
                                        styled(
                                            i18nContext.get(GiveawayManager.I18N_PREFIX.JoinGiveaway.YouAreNowParticipating),
                                            Emotes.LoriYay
                                        )
                                    })
                                    .setEphemeral(true)
                                    .await()

                                deferredEdit.editOriginal(
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
        class MissingRoles(val allowedRoles: GiveawayRoles) : GiveawayState()
        class BlockedRoles(val deniedRoles: GiveawayRoles) : GiveawayState()
        class Success(val giveaway: ResultRow, val participants: Long, val allowedRoles: GiveawayRoles?, val deniedRoles: GiveawayRoles?) : GiveawayState()
    }
}