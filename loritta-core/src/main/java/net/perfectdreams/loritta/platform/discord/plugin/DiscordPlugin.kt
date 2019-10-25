package net.perfectdreams.loritta.platform.discord.plugin

import com.mrpowergamerbr.loritta.plugin.LorittaPlugin
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.profile.Badge

open class DiscordPlugin : LorittaPlugin() {
    val eventListeners = mutableListOf<ListenerAdapter>()
    val onGuildReadyListeners = mutableListOf<suspend (Guild, MongoServerConfig) -> (Unit)>()
    val onGuildMemberJoinListeners = mutableListOf<suspend (Member, Guild, MongoServerConfig) -> (Unit)>()
    val onGuildMemberLeaveListeners = mutableListOf<suspend (Member, Guild, MongoServerConfig) -> (Unit)>()
    val badges = mutableListOf<Badge>()

    override fun onDisable() {
        super.onDisable()
        eventListeners.forEach {
            lorittaShards.shardManager.removeEventListener(it)
        }
        eventListeners.clear()
        onGuildReadyListeners.clear()
        onGuildMemberLeaveListeners.clear()
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
        badges.add(badge)
    }

    fun onGuildReady(callback: suspend (Guild, MongoServerConfig) -> (Unit)) {
        onGuildReadyListeners.add(callback)
    }

    fun onGuildMemberJoinListeners(callback: suspend (Member, Guild, MongoServerConfig) -> (Unit)) {
        onGuildMemberJoinListeners.add(callback)
    }

    fun onGuildMemberLeaveListeners(callback: suspend (Member, Guild, MongoServerConfig) -> (Unit)) {
        onGuildMemberLeaveListeners.add(callback)
    }
}