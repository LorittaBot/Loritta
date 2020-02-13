package net.perfectdreams.loritta.platform.discord.entities

import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.api.entities.Emote
import net.perfectdreams.loritta.api.entities.LorittaEmote

class DiscordEmote(code: String) : LorittaEmote(code) {
    private var jdaEmote: Emote? = null
    private val id: String
    private val name: String

    init {
        val split = code.split(":")
        id = split.last()
        name = split[split.size - 2]
    }

    override val asMention: String
        get() = getJdaEmote()?.asMention ?: run {
            val builder = StringBuilder()
            builder.append("<")
            if (!code.startsWith("discord:a:")) {
                builder.append(":")
            }
            builder.append(code.split(":").drop(1).joinToString(":"))
            builder.append(">")
            builder.toString()
        }

    override fun getName(): String {
        return getJdaEmote()?.name ?: name
    }

    override fun isAvailable(): Boolean {
        return getJdaEmote() != null
    }

    private fun getJdaEmote(): Emote? {
        return jdaEmote ?: run {
            val jdaEmote = lorittaShards.getEmoteById(id)
            if (jdaEmote != null)
                this.jdaEmote = jdaEmote
            jdaEmote
        }
    }

    override fun toString() = asMention
}