package net.perfectdreams.loritta.platform.discord.plugin

import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.plugin.LorittaPlugin
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.profile.Badge

open class DiscordPlugin : LorittaPlugin() {
    val eventListeners = mutableListOf<ListenerAdapter>()
    val onGuildReadyListeners = mutableListOf<suspend (Guild, ServerConfig) -> (Unit)>()
    val onGuildMemberJoinListeners = mutableListOf<suspend (Member, Guild, ServerConfig) -> (Unit)>()
    val onGuildMemberLeaveListeners = mutableListOf<suspend (Member, Guild, ServerConfig) -> (Unit)>()
    private val badges = mutableListOf<Badge>()

    override fun onDisable() {
        super.onDisable()
        eventListeners.forEach {
            lorittaShards.shardManager.removeEventListener(it)
        }
        eventListeners.clear()
        onGuildReadyListeners.clear()
        onGuildMemberLeaveListeners.clear()
        badges.forEach {
            loritta.profileDesignManager.unregisterBadge(it)
        }
        badges.clear()
    }

    fun addEventListener(eventListener: ListenerAdapter) {
        eventListeners.add(eventListener)
        lorittaShards.shardManager.addEventListener(eventListener)
    }

    fun removeEventListener(eventListener: ListenerAdapter) {
        eventListeners.remove(eventListener)
        lorittaShards.shardManager.removeEventListener(eventListener)
    }

    fun registerEventListeners(vararg eventListeners: ListenerAdapter) {
        eventListeners.forEach {
            addEventListener(it)
        }
    }

    fun registerBadge(badge: Badge) {
        loritta.profileDesignManager.registerBadge(badge)
        badges.add(badge)
    }

    fun unregisterBadge(badge: Badge) {
        loritta.profileDesignManager.unregisterBadge(badge)
        badges.remove(badge)
    }

    fun onGuildReady(callback: suspend (Guild, ServerConfig) -> (Unit)) {
        onGuildReadyListeners.add(callback)
    }

    fun onGuildMemberJoinListeners(callback: suspend (Member, Guild, ServerConfig) -> (Unit)) {
        onGuildMemberJoinListeners.add(callback)
    }

    fun onGuildMemberLeaveListeners(callback: suspend (Member, Guild, ServerConfig) -> (Unit)) {
        onGuildMemberLeaveListeners.add(callback)
    }
}