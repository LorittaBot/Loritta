package com.mrpowergamerbr.loritta.modules

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageReaction
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent
import net.perfectdreams.loritta.dao.servers.moduleconfigs.ReactionOption
import net.perfectdreams.loritta.tables.servers.moduleconfigs.ReactionOptions
import org.jetbrains.exposed.sql.and
import java.util.*
import java.util.concurrent.TimeUnit

object ReactionModule {
    private val logger = KotlinLogging.logger {}
    private val removedReactionByLorittaCache = Collections.newSetFromMap(
            Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.SECONDS)
            .build<String, Boolean>()
            .asMap()
    )
    private val mutexes = Caffeine.newBuilder()
                    .expireAfterAccess(60, TimeUnit.SECONDS)
                    .build<Long, Mutex>()
                    .asMap()

    suspend fun onReactionAdd(event: GuildMessageReactionAddEvent) {
        // Ao adicionar uma reação, vamos pegar se existe algum reaction role baseado nesta reação escolhida
        val reaction = if (event.reactionEmote.isEmote) {
            event.reactionEmote.emote.id
        } else {
            event.reactionEmote.name
        }

        val option = loritta.newSuspendedTransaction {
            ReactionOption.find {
                (ReactionOptions.guildId eq event.guild.idLong) and
                        (ReactionOptions.textChannelId eq event.channel.idLong) and
                        (ReactionOptions.messageId eq event.messageIdLong) and
                        (ReactionOptions.reaction eq reaction)
            }.firstOrNull()
        } ?: return

        logger.info { "Opção encontrada: $option" }

        // Verificar locks
        // Existem vários tipos de locks: Locks de opções (via ID), locks de mensagens (via... mensagens), etc.
        // Para ficar mais fácil, vamos verificar TODOS os locks da mensagem
        val locks = mutableListOf<ReactionOption>()

        for (lock in option.locks) {
            if (lock.contains("-")) {
                val split = lock.split("-")
                val channelOptionLock = loritta.newSuspendedTransaction {
                    ReactionOption.find {
                        (ReactionOptions.guildId eq event.guild.idLong) and
                                (ReactionOptions.textChannelId eq split[0].toLong()) and
                                (ReactionOptions.messageId eq split[1].toLong())
                    }.toMutableList()
                }
                locks.addAll(channelOptionLock)
            } else { // Lock por option ID, esse daqui é mais complicado!
                val idOptionLock = loritta.newSuspendedTransaction {
                    ReactionOption.find {
                        (ReactionOptions.id eq lock.toLong())
                    }.toMutableList()
                }
                locks.addAll(idOptionLock)
            }
        }

        logger.info { "Número de locks encontrados: ${locks.size}" }

        // Agora nós já temos a opção desejada, só dar os cargos para o usuário!
        val roles = option.roleIds.mapNotNull { event.guild.getRoleById(it) }

        val mutex = mutexes.getOrPut(event.member.user.idLong) { Mutex() }
        mutex.withLock {
            giveRolesToMember(event.member, event.reaction, option, locks, roles)
        }
    }

    suspend fun onReactionRemove(event: GuildMessageReactionRemoveEvent) {
        val member = event.member ?: return

        // Ao remover uma reação, vamos pegar se existe algum reaction role baseado nesta reação escolhida
        val reaction = if (event.reactionEmote.isEmote) {
            event.reactionEmote.emote.id
        } else {
            event.reactionEmote.name
        }

        val option = loritta.newSuspendedTransaction {
            ReactionOption.find {
                (ReactionOptions.guildId eq event.guild.idLong) and
                        (ReactionOptions.textChannelId eq event.channel.idLong) and
                        (ReactionOptions.messageId eq event.messageIdLong) and
                        (ReactionOptions.reaction eq reaction)
            }.firstOrNull()
        } ?: return

        if (removedReactionByLorittaCache.contains("${member.user.id}-${option.id.value}")) { // Caso tenha sido a própria Lori que tenha removido a reação, só ignore! A gente não liga!
            removedReactionByLorittaCache.remove("${member.user.id}-${option.id.value}")
            return
        }

        // Agora nós já temos a opção desejada, só remover os cargos para o usuário!
        val roles = option.roleIds.mapNotNull { event.guild.getRoleById(it) }

        val mutex = mutexes.getOrPut(member.idLong) { Mutex() }
        mutex.withLock {
            removeRolesFromMember(member, option, roles)
        }
    }

    suspend fun giveRolesToMember(member: Member, reaction: MessageReaction, option: ReactionOption, locks: List<ReactionOption>, roles: List<Role>) {
        val guild = member.guild

        // Verificar locks
        // Existem vários tipos de locks: Locks de opções (via ID), locks de mensagens (via... mensagens), etc.
        // Para ficar mais fácil, vamos verificar se a gente já tem um lock na mesma canal-mensagem
        // Processar locks
        for (lock in locks) {
            if (option.id.value == lock.id.value) // Ignorar caso seja o lock que estamos processando (a gente não liga se o cara já tem o cargo, ou algo assim)
                continue

            val hasRoles = member.roles.any { lock.roleIds.contains(it.id) }
            if (hasRoles) { // Lock!
                removedReactionByLorittaCache.add("${member.user.id}-${option.id.value}")
                reaction.removeReaction(member.user).await()
                return
            }
        }

        val rolesToBeGiven = roles.filter { !member.roles.contains(it) }
        if (rolesToBeGiven.isNotEmpty()) {
            guild.modifyMemberRoles(member, member.roles.toMutableList().apply { this.addAll(rolesToBeGiven) }).await()
        }
    }

    suspend fun removeRolesFromMember(member: Member, option: ReactionOption, roles: List<Role>) {
        val guild = member.guild

        val rolesToBeGiven = roles.filter { member.roles.contains(it) }
        if (rolesToBeGiven.isNotEmpty()) {
            guild.modifyMemberRoles(member, member.roles.toMutableList().apply { this.removeAll(rolesToBeGiven) }).await()
        }
    }
}