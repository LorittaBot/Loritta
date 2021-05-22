package net.perfectdreams.loritta.platform.discord.legacy.entities

import net.dv8tion.jda.api.entities.Emote
import net.perfectdreams.loritta.api.entities.LorittaEmote

open class DiscordEmote(code: String) : LorittaEmote(code) {
    val id: String
    private val name: String
    val reactionCode: String

    init {
        val split = code.split(":")
        id = split.last()
        name = split[split.size - 2]
        reactionCode = "$name:$id"
    }

    // Before we were using JDA's "getEmoteById" call, if the emote was present in the cache, we would use it.
    // But think about it: We are clustered, most of the times the emote will *not* be present in the cache
    // and calling "getEmoteById" is costly.
    //
    // So, to avoid issues, we just build our own emote from the code, which should be *hopefully* correct and working.
    // If it ain't correct, then you should fix your "emotes.conf" file! ;)
    //
    // And, to avoid recreating the Discord emote code every single call, we keep it loaded in memory.
    private val discordEmoteAsMention: String by lazy {
        val builder = StringBuilder()
        builder.append("<")
        if (!code.startsWith("discord:a:")) {
            builder.append(":")
        }
        builder.append(code.split(":").drop(1).joinToString(":"))
        builder.append(">")
        builder.toString()
    }

    override val asMention: String
        = discordEmoteAsMention

    override fun getName() = name
    override fun toString() = asMention

    class DiscordEmoteBackedByJdaEmote(val jdaEmote: Emote) : DiscordEmote("discord:${jdaEmote.asMention.removePrefix("<").removePrefix(":").removeSuffix(">")}")
}