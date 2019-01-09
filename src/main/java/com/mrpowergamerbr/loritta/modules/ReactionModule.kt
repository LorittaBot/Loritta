package com.mrpowergamerbr.loritta.modules

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.extensions.await
import mu.KotlinLogging
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.MessageReaction
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveEvent
import net.perfectdreams.loritta.dao.ReactionOption
import net.perfectdreams.loritta.tables.ReactionOptions
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

object ReactionModule {
    private val logger = KotlinLogging.logger {}

    suspend fun onReactionAdd(event: GuildMessageReactionAddEvent) {
        // Ao adicionar uma reação, vamos pegar se existe algum reaction role baseado nesta reação escolhida
        val reaction = if (event.reactionEmote.isEmote) {
            event.reactionEmote.emote.id
        } else {
            event.reactionEmote.name
        }

        logger.info { "Usuário ${event.user.idLong} reagiu com $reaction em ${event.messageIdLong} @ ${event.channel.idLong}" }

        val option = transaction(Databases.loritta) {
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
                val channelOptionLock = transaction(Databases.loritta) {
                    ReactionOption.find {
                        (ReactionOptions.guildId eq event.guild.idLong) and
                                (ReactionOptions.textChannelId eq split[0].toLong()) and
                                (ReactionOptions.messageId eq split[1].toLong())
                    }.firstOrNull()
                } ?: continue
                locks.add(channelOptionLock)
            } else { // Lock por option ID, esse daqui é mais complicado!
                val idOptionLock = transaction(Databases.loritta) {
                    ReactionOption.find {
                        (ReactionOptions.id eq lock.toLong())
                    }.firstOrNull()
                } ?: continue
                locks.add(idOptionLock)
            }
        }

        logger.info { "Número de locks encontrados: ${locks.size}" }

        // Agora nós já temos a opção desejada, só dar os cargos para o usuário!
        val roles = option.roleIds.mapNotNull { event.guild.getRoleById(it) }
        giveRolesToMember(event.member, event.reaction, option, locks, roles)

        // E é claro, dê os cargos para o resto do povo (vai se a Lori caiu!)
        event.reaction.users.await().forEach {
            val member = event.guild.getMember(it)

            if (member != null)
                giveRolesToMember(member, event.reaction, option, locks, roles)
        }
    }

    suspend fun onReactionRemove(event: GuildMessageReactionRemoveEvent) {
        // Ao remover uma reação, vamos pegar se existe algum reaction role baseado nesta reação escolhida
        val reaction = if (event.reactionEmote.isEmote) {
            event.reactionEmote.emote.id
        } else {
            event.reactionEmote.name
        }

        val option = transaction(Databases.loritta) {
            ReactionOption.find {
                (ReactionOptions.guildId eq event.guild.idLong) and
                        (ReactionOptions.textChannelId eq event.channel.idLong) and
                        (ReactionOptions.messageId eq event.messageIdLong) and
                        (ReactionOptions.reaction eq reaction)
            }.firstOrNull()
        } ?: return

        // Agora nós já temos a opção desejada, só remover os cargos para o usuário!
        val roles = option.roleIds.mapNotNull { event.guild.getRoleById(it) }
        removeRolesFromMember(event.member, option, roles)

        // E é claro, dê os cargos para o resto do povo (vai se a Lori caiu!)
        event.reaction.users.await().forEach {
            val member = event.guild.getMember(it)

            if (member != null)
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
            val hasRoles = member.roles.any { lock.roleIds.contains(it.id) }
            if (hasRoles) { // Lock!
                reaction.removeReaction(member.user).await()
                return
            }
        }

        logger.info { "Dando cargos para $member!" }

        val rolesToBeGiven = roles.filter { !member.roles.contains(it) }
        if (rolesToBeGiven.isNotEmpty()) {
            guild.controller.addRolesToMember(member, rolesToBeGiven).await()
        }
    }

    suspend fun removeRolesFromMember(member: Member, option: ReactionOption, roles: List<Role>) {
        val guild = member.guild

        val rolesToBeGiven = roles.filter { member.roles.contains(it) }
        if (rolesToBeGiven.isNotEmpty()) {
            guild.controller.removeRolesFromMember(member, rolesToBeGiven).await()
        }
    }
}