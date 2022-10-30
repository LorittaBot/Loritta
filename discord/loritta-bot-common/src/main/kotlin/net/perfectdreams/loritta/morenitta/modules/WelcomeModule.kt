package net.perfectdreams.loritta.morenitta.modules

import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.listeners.EventLogListener
import net.perfectdreams.loritta.morenitta.utils.MessageUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.humanize
import mu.KotlinLogging
import net.perfectdreams.loritta.deviousfun.MessageBuilder
import dev.kord.common.entity.Permission
import kotlinx.coroutines.delay
import net.perfectdreams.loritta.deviousfun.entities.User
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.WelcomerConfig
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.deviousfun.events.guild.member.GuildMemberRemoveEvent
import net.perfectdreams.loritta.deviousfun.events.guild.member.GuildMemberJoinEvent
import net.perfectdreams.loritta.morenitta.LorittaBot
import org.apache.commons.io.IOUtils
import java.nio.charset.Charset
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

class WelcomeModule(val loritta: LorittaBot) {
    private val logger = KotlinLogging.logger {}

    val joinMembersCache = Caffeine.newBuilder()
        .expireAfterAccess(15, TimeUnit.SECONDS)
        .removalListener { k1: Long?, v1: CopyOnWriteArrayList<User>?, removalCause ->
            if (k1 != null && v1 != null) {
                logger.info("Removendo join members cache de $k1... ${v1.size} membros tinham saído durante este período")

                if (v1.size > 20) {
                    logger.info("Mais de 20 membros entraram em menos de 15 segundos em $k1! Que triste, né? Vamos enviar um arquivo com todos que sairam!")

                    val serverConfig = loritta.getOrCreateServerConfig(k1)
                    val welcomerConfig = runBlocking {
                        loritta.pudding.transaction {
                            serverConfig.welcomerConfig
                        }
                    }

                    if (welcomerConfig != null) {
                        val channelJoinId = welcomerConfig.channelJoinId
                        if (welcomerConfig.tellOnJoin && !welcomerConfig.joinMessage.isNullOrEmpty() && channelJoinId != null) {
                            runBlocking {
                                val guild = loritta.lorittaShards.getGuildById(k1) ?: return@runBlocking

                                val textChannel = guild.getTextChannelById(channelJoinId)

                                if (textChannel != null) {
                                    if (textChannel.canTalk()) {
                                        if (guild.selfMemberHasPermission(textChannel, Permission.AttachFiles)) {
                                            val lines = mutableListOf<String>()
                                            for (user in v1) {
                                                lines.add("${user.name}#${user.discriminator} - (${user.id})")
                                            }
                                            val targetStream = IOUtils.toInputStream(
                                                lines.joinToString("\n"),
                                                Charset.defaultCharset()
                                            )

                                            val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)

                                            runCatching {
                                                textChannel.sendMessage(
                                                    MessageBuilder()
                                                        .setContent(locale["modules.welcomer.tooManyUsersJoining", Emotes.LORI_OWO])
                                                        .addFile(targetStream.readAllBytes(), "join-users.log")
                                                        .build()
                                                )
                                            }
                                            logger.info("Enviado arquivo de texto em $k1 com todas as pessoas que entraram, yay!")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        .build<Long, CopyOnWriteArrayList<User>>()
    val leftMembersCache = Caffeine.newBuilder()
        .expireAfterAccess(15, TimeUnit.SECONDS)
        .removalListener { k1: Long?, v1: CopyOnWriteArrayList<User>?, removalCause ->
            if (k1 != null && v1 != null) {
                logger.info("Removendo left members cache de $k1... ${v1.size} membros tinham saído durante este período")

                if (v1.size > 20) {
                    logger.info("Mais de 20 membros sairam em menos de 15 segundos em $k1! Que triste, né? Vamos enviar um arquivo com todos que sairam!")

                    val serverConfig = loritta.getOrCreateServerConfig(k1)
                    val welcomerConfig = runBlocking {
                        loritta.pudding.transaction {
                            serverConfig.welcomerConfig
                        }
                    }

                    if (welcomerConfig != null) {
                        val channelRemoveId = welcomerConfig.channelRemoveId
                        if (welcomerConfig.tellOnRemove && !welcomerConfig.removeMessage.isNullOrEmpty() && channelRemoveId != null) {
                            runBlocking {
                                val guild = loritta.lorittaShards.getGuildById(k1) ?: return@runBlocking

                                val textChannel = guild.getTextChannelById(channelRemoveId)

                                if (textChannel != null) {
                                    if (textChannel.canTalk()) {
                                        if (guild.selfMemberHasPermission(textChannel, Permission.AttachFiles)) {
                                            val lines = mutableListOf<String>()
                                            for (user in v1) {
                                                lines.add("${user.name}#${user.discriminator} - (${user.id})")
                                            }
                                            val targetStream = IOUtils.toInputStream(
                                                lines.joinToString("\n"),
                                                Charset.defaultCharset()
                                            )

                                            val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)

                                            runCatching {
                                                textChannel.sendMessage(
                                                    MessageBuilder()
                                                        .setContent(locale["modules.welcomer.tooManyUsersLeaving", Emotes.LORI_OWO])
                                                        .addFile(targetStream.readAllBytes(), "left-users.log")
                                                        .build()
                                                )
                                            }
                                            logger.info("Enviado arquivo de texto em $k1 com todas as pessoas que sairam, yay!")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        .build<Long, CopyOnWriteArrayList<User>>()

    suspend fun handleJoin(event: GuildMemberJoinEvent, serverConfig: ServerConfig, welcomerConfig: WelcomerConfig) {
        val joinLeaveConfig = welcomerConfig
        val tokens = mapOf(
            "humanized-date" to event.member.timeJoined.humanize(loritta.localeManager.getLocaleById(serverConfig.localeId))
        )

        logger.trace { "Member = ${event.member}, Guild ${event.guild} has tellOnJoin = ${joinLeaveConfig.tellOnJoin} and the joinMessage is ${joinLeaveConfig.joinMessage}, canalJoinId = ${joinLeaveConfig.channelJoinId}" }

        val channelJoinId = welcomerConfig.channelJoinId
        if (joinLeaveConfig.tellOnJoin && !joinLeaveConfig.joinMessage.isNullOrEmpty() && channelJoinId != null) { // E o sistema de avisar ao entrar está ativado?
            logger.trace { "Guild ${event.guild} has tellOnJoin enabled and the joinMessage isn't empty!" }
            val guild = event.guild

            logger.debug { "Member = ${event.member}, Getting ${guild}'s cache list from joinMembersCache..." }

            val list = joinMembersCache.getIfPresent(event.guild.idLong) ?: CopyOnWriteArrayList()
            logger.debug { "Member = ${event.member}, There are ${list.size} entries on the joinMembersCache list for $guild" }
            list.add(event.user)
            joinMembersCache.put(event.guild.idLong, list)

            logger.trace { "Member = ${event.member}, Checking if the joinMembersCache max list entry threshold is > 20 for $guild, currently it is ${list.size}" }

            if (list.size > 20)
                return

            logger.trace { "Member = ${event.member}, canalJoinId is not null for $guild, canalJoinId = ${joinLeaveConfig.channelJoinId}" }

            val textChannel = guild.getTextChannelById(channelJoinId)

            logger.trace { "Member = ${event.member}, canalLeaveId = ${joinLeaveConfig.channelRemoveId}, it is $textChannel for $guild" }
            if (textChannel != null) {
                logger.trace { "Member = ${event.member}, Text channel $textChannel is not null for $guild!" }

                if (textChannel.canTalk()) {
                    val msg = joinLeaveConfig.joinMessage
                    logger.trace { "Member = ${event.member}, Join message is $msg for $guild, it will be sent at $textChannel" }

                    if (!msg.isNullOrEmpty() && event.guild.selfMemberHasPermission(
                            textChannel,
                            Permission.EmbedLinks
                        )
                    ) {
                        val deleteJoinMessagesAfter = welcomerConfig.deleteJoinMessagesAfter
                        logger.debug { "Member = ${event.member}, Sending join message \"$msg\" in $textChannel at $guild" }

                        runCatching {
                            val message = textChannel.sendMessage(
                                MessageUtils.generateMessage(
                                    msg,
                                    listOf(guild, event.member),
                                    guild,
                                    tokens
                                )!!
                            )

                            if (deleteJoinMessagesAfter != null && deleteJoinMessagesAfter != 0L) {
                                delay(deleteJoinMessagesAfter.seconds)
                                message.delete()
                            }
                        }
                    }
                } else {
                    logger.debug { "Member = ${event.member} (Join), I don't have permission to send messages in $textChannel on guild $guild!" }
                }
            }
        }

        logger.trace { "Member = ${event.member}, Guild ${event.guild} has tellOnPrivate = ${joinLeaveConfig.tellOnPrivateJoin} and the joinMessage is ${joinLeaveConfig.joinPrivateMessage}" }

        if (!event.user.isBot && joinLeaveConfig.tellOnPrivateJoin && !joinLeaveConfig.joinPrivateMessage.isNullOrEmpty()) { // Talvez o sistema de avisar no privado esteja ativado!
            val msg = joinLeaveConfig.joinPrivateMessage

            logger.debug { "Member = ${event.member}, sending join message (private channel) \"$msg\" at ${event.guild}" }

            if (!msg.isNullOrEmpty()) {
                val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)

                runCatching {
                    event.user.openPrivateChannel()
                        .sendMessage(
                            MessageUtils.generateMessage(
                                MessageUtils.watermarkModuleMessage(
                                    msg,
                                    locale,
                                    event.guild,
                                    locale["modules.welcomer.moduleDirectMessageJoinType"]
                                ),
                                listOf(event.guild, event.member),
                                event.guild,
                                tokens
                            )!!
                        ) // Pronto!
                }
            }
        }
    }

    suspend fun handleLeave(event: GuildMemberRemoveEvent, serverConfig: ServerConfig, welcomerConfig: WelcomerConfig) {
        val joinLeaveConfig = welcomerConfig

        logger.trace { "User = ${event.user}, Guild ${event.guild} has tellOnLeave = ${joinLeaveConfig.tellOnRemove} and the leaveMessage is ${joinLeaveConfig.removeMessage}, canalLeaveId = ${joinLeaveConfig.channelRemoveId}" }

        val channelRemoveId = welcomerConfig.channelRemoveId
        if (joinLeaveConfig.tellOnRemove && !joinLeaveConfig.removeMessage.isNullOrEmpty() && channelRemoveId != null) {
            logger.trace { "User = ${event.user}, Guild ${event.guild} has tellOnLeave enabled and the leaveMessage isn't empty!" }
            val guild = event.guild

            logger.debug { "User = ${event.user}, Getting ${guild}'s cache list from leftMembersCache..." }

            val list = leftMembersCache.getIfPresent(event.guild.idLong) ?: CopyOnWriteArrayList()
            logger.debug { "User = ${event.user}, There are ${list.size} entries on the leftMembersCache list for $guild" }
            list.add(event.user)
            leftMembersCache.put(event.guild.idLong, list)

            logger.trace { "User = ${event.user}, Checking if the leftMembersCache max list entry threshold is > 20 for $guild, currently it is ${list.size}" }

            if (list.size > 20)
                return

            logger.trace { "User = ${event.user}, canalLeaveId is not null for $guild, canalLeaveId = ${joinLeaveConfig.channelRemoveId}" }

            val textChannel = guild.getTextChannelById(channelRemoveId)

            logger.trace { "User = ${event.user}, canalLeaveId = ${joinLeaveConfig.channelRemoveId}, it is $textChannel for $guild" }
            if (textChannel != null) {
                logger.trace { "User = ${event.user}, Text channel $textChannel is not null for $guild!" }

                if (textChannel.canTalk()) {
                    var msg = joinLeaveConfig.removeMessage
                    logger.trace { "User = ${event.user}, Leave message is $msg for $guild, it will be sent at $textChannel" }

                    val customTokens = mutableMapOf<String, String>()

                    // Verificar se o usuário foi banido e, se sim, mudar a mensagem caso necessário
                    val bannedUserKey = "${event.guild.id}#${event.user.id}"

                    if (joinLeaveConfig.tellOnBan && EventLogListener.bannedUsers.getIfPresent(bannedUserKey) == true) {
                        if (!joinLeaveConfig.bannedMessage.isNullOrEmpty())
                            msg = joinLeaveConfig.bannedMessage
                    }
                    // Invalidar, já que a Loritta faz cache mesmo que o servidor não use a função
                    EventLogListener.bannedUsers.invalidate(bannedUserKey)

                    if (!msg.isNullOrEmpty() && event.guild.selfMemberHasPermission(
                            textChannel,
                            Permission.EmbedLinks
                        )
                    ) {
                        val deleteRemoveMessagesAfter = welcomerConfig.deleteRemoveMessagesAfter
                        logger.debug { "User = ${event.user}, Sending quit message \"$msg\" in $textChannel at $guild" }

                        runCatching {
                            val message = textChannel.sendMessage(
                                MessageUtils.generateMessage(
                                    msg,
                                    listOf(event.guild, event.user),
                                    guild,
                                    customTokens
                                )!!
                            )

                            if (deleteRemoveMessagesAfter != null && deleteRemoveMessagesAfter != 0L) {
                                delay(deleteRemoveMessagesAfter.seconds)
                                message.delete()
                            }
                        }
                    }
                } else {
                    logger.debug { "I don't have permission to send messages in $textChannel on guild $guild!" }
                }
            }
        }
    }
}