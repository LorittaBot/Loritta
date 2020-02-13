package net.perfectdreams.loritta.commands.vanilla.audio

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import lavalink.client.io.LavalinkSocket
import lavalink.client.io.Link
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import org.json.JSONArray
import org.json.JSONObject

class VolumeCommand : LorittaCommand(arrayOf("volume"), CommandCategory.MUSIC) {
    override fun getDescription(locale: BaseLocale): String? {
        return locale["commands.audio.volume.description"]
    }

    override val lorittaPermissions = listOf(LorittaPermission.DJ)

    fun sendFilters(link: Link, vararg jsonObject: JSONObject) {
        // Send WS message
        val node: LavalinkSocket = link.getNode(false) ?: return

        val json = JSONObject()
        json.put("op", "filter")
        json.put("guildId", link.guildId)

        val array = JSONArray()
        for (obj in jsonObject) {
            array.put(obj)
        }
        json.put("filters", array)

        node.send(json.toString())
    }

    @Subcommand
    suspend fun run(context: DiscordCommandContext, locale: BaseLocale) {
        val audioManager = LorittaLauncher.loritta.audioManager ?: return

        val musicManager = audioManager.getMusicManager(context.discordGuild!!) ?: return

        val link = musicManager.link
        if (context.args[0] == "nightcore") {
            sendFilters(
                    link,
                    JSONObject()
                            .put("type", "timescale")
                            .put("speed", 1.25)
                            .put("pitch", 1.25)
            )

            context.reply("Alterado, yay!")
            return
        } else if (context.args[0] == "vaporwave") {
            sendFilters(
                    link,
                    JSONObject()
                            .put("type", "timescale")
                            .put("speed", 0.7)
                            .put("pitch", 0.8)
            )

            context.reply("Alterado, yay!")
            return
        } else if (context.args[0] == "reset") {
            sendFilters(
                    link
            )
            return
        }

        // Limpar lista de qualquer música que tenha
        musicManager.scheduler.player.volume = context.args[0].toInt()

        musicManager.scheduler.player
        context.reply(
                "Volume alterado!"
        )
    }
}