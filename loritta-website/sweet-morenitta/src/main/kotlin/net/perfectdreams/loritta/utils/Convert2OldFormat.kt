package net.perfectdreams.loritta.utils

import com.fasterxml.jackson.module.kotlin.readValue
import net.perfectdreams.loritta.utils.config.FanArtArtist
import java.io.File

fun main() {
    val folder = File("C:\\Users\\leona\\Documents\\LorittaAssets\\website\\fan_arts")

    var newYaml = "fan-arts:\n"

    val artists = mutableListOf<FanArtArtist>()

    folder.listFiles().forEach {
        if (it.extension == "conf") {
            val hocon = it.readText()

            val mapper = Constants.HOCON_MAPPER
            println("Reading $it")
            val r = mapper.readValue<FanArtArtist>(hocon)
            artists.add(r)
        }
    }

    val fanArts = artists.flatMap { it.fanArts }.sortedBy { it.createdAt }
    var inc = -1L

    fanArts.forEach {
        val artistRoot = artists.first { artist -> artist.fanArts.any { ogArt -> it.fileName == ogArt.fileName } }

        val dInfo = artistRoot.socialNetworks?.firstOrNull { it.type == "discord" }

        val artistId = if (dInfo != null && dInfo is FanArtArtist.SocialNetwork.DiscordSocialNetwork) {
            dInfo.id.toLong()
        } else {
            inc++
        }

        newYaml += "  - artistId: '${artistId}'\n" +
                "    fileName: ${it.fileName}\n"
    }

    println(newYaml)
}