package net.perfectdreams.loritta.platform.discord.plugin

import com.mrpowergamerbr.loritta.plugin.LorittaPlugin
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.api.hooks.ListenerAdapter

open class DiscordPlugin : LorittaPlugin() {
    val eventListeners = mutableListOf<ListenerAdapter>()

    override fun onDisable() {
        super.onDisable()
        eventListeners.forEach {
            lorittaShards.shardManager.removeEventListener(it)
        }
        eventListeners.clear()
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
}