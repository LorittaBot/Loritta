package com.mrpowergamerbr.loritta.utils

import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.managers.Presence

/**
 * Guarda todos os shards da Loritta
 */
class LorittaShards {
    var shards: MutableSet<JDA> = mutableSetOf()

    fun getGuildById(id: String): Guild? {
        for (shard in shards) {
            var guild = shard.getGuildById(id);
            if (guild != null) { return guild; }
        }
        return null;
    }

    fun getGuilds(): List<Guild> {
        // Pegar todas as guilds em todos os shards
        var guilds = ArrayList<Guild>();

        for (shard in shards) {
            guilds.addAll(shard.guilds);
        }
        return guilds;
    }

    fun getGuildCount(): Int {
        return shards.sumBy { it.guilds.size }
    }

    fun getUserCount(): Int {
        return getUsers().size
    }

    fun getEmoteCount(): Int {
        return shards.sumBy { it.emoteCache.size().toInt() }
    }

    fun getChannelCount(): Int {
        return getTextChannelCount() + getVoiceChannelCount()
    }

    fun getTextChannelCount(): Int {
        return shards.sumBy { it.textChannelCache.size().toInt() }
    }

    fun getVoiceChannelCount(): Int {
        return shards.sumBy { it.textChannelCache.size().toInt() }
    }

    fun getUsers(): List<User> {
        // Pegar todas os users em todos os shards
        val users = ArrayList<User>()

        for (shard in shards) {
            users.addAll(shard.users)
        }

        val nonDuplicates = users.distinctBy { it.id }

        return nonDuplicates
    }

    fun getUserById(id: String?): User? {
        for (shard in shards) {
            val user = shard.getUserById(id);
            if (user != null) {
                return user
            }
        }
        return null;
    }

    fun retrieveUserById(id: String?): User? {
        return getUserById(id) ?: shards.first().retrieveUserById(id).complete()
    }

    fun getMutualGuilds(user: User): List<Guild> {
        // Pegar todas as mutual guilds em todos os shards
        val guilds = ArrayList<Guild>()
        for (shard in shards) {
            guilds.addAll(shard.getMutualGuilds(user))
        }
        return guilds;
    }

    fun getEmoteById(id: String?): Emote? {
        if (id == null)
            return null

        for (shard in shards) {
            val emote = shard.getEmoteById(id)
            if (emote != null)
                return emote
        }
        return null
    }

    fun getTextChannelById(id: String?): TextChannel? {
        if (id == null)
            return null

        for (shard in shards) {
            val textChannel = shard.getTextChannelById(id)
            if (textChannel != null)
                return textChannel
        }
        return null
    }

    fun getPresence(): Presence {
        // Pegar primeira shard e retornar a presença dela
        return shards.first().presence
    }

    /**
     * Atualiza a presença do bot em todas as shards
     */
    fun setGame(game: Game) {
        for (shard in shards) {
            shard.presence.game = game
        }
    }
}