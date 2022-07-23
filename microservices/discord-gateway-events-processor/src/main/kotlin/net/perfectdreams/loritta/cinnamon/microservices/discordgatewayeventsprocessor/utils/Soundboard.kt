package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils

import net.perfectdreams.loritta.cinnamon.pudding.data.notifications.SoundboardAudio
import org.gagravarr.ogg.OggFile
import org.gagravarr.opus.OpusFile

class Soundboard {
    private val amongUsRoundStart = loadAndExtract("among_us_round_start.ogg")
    private val rapaiz = loadAndExtract("rapaiz.ogg")
    private val chavesRisadas = loadAndExtract("chaves_risadas.ogg")
    private val danceCatDance = loadAndExtract("dance_cat_dance.ogg")
    private val esseEOMeuPatraoHehe = loadAndExtract("esse_e_o_meu_patrao_hehe.ogg")
    private val irra = loadAndExtract("irra.ogg")
    private val ratinho = loadAndExtract("ratinho.ogg")
    private val uepa = loadAndExtract("uepa.ogg")
    private val ui = loadAndExtract("ui.ogg")
    private val nicelyDoneCheer = loadAndExtract("nicely_done_cheer.ogg")

    fun getAudioClip(audio: SoundboardAudio) = when (audio) {
        SoundboardAudio.AMONG_US_ROUND_START -> amongUsRoundStart
        SoundboardAudio.RAPAIZ -> rapaiz
        SoundboardAudio.CHAVES_RISADAS -> chavesRisadas
        SoundboardAudio.DANCE_CAT_DANCE -> danceCatDance
        SoundboardAudio.ESSE_E_O_MEU_PATRAO_HEHE -> esseEOMeuPatraoHehe
        SoundboardAudio.IRRA -> irra
        SoundboardAudio.RATINHO -> ratinho
        SoundboardAudio.UEPA -> uepa
        SoundboardAudio.UI -> ui
        SoundboardAudio.NICELY_DONE_CHEER -> nicelyDoneCheer
    }

    /**
     * Extracts Opus frames from a OGG file, stored as a [byteArray]
     *
     * @param byteArray the OGG file as a ByteArray
     * @return a list of Opus frames
     */
    fun extractOpusFrames(byteArray: ByteArray): List<ByteArray> {
        // Load the OGG data
        val vf = OpusFile(OggFile(byteArray.inputStream()))

        // Extract the packets
        val packets = mutableListOf<ByteArray>()
        while (true) {
            val audioPacket = vf.nextAudioPacket ?: break
            packets.add(audioPacket.data)
        }

        return packets
    }

    /**
     * Loads the OGG file at [path] within the application resources and extracts the Opus frames of it
     *
     * @param path the path of the audio file
     * @return a list of Opus frames
     */
    private fun loadAndExtract(path: String) = Soundboard::class.java
        .getResourceAsStream("/soundboard/$path")!!
        .readAllBytes()
        .let {
            extractOpusFrames(it)
        }
}