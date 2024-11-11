package net.perfectdreams.loritta.morenitta.utils

import net.perfectdreams.loritta.morenitta.LorittaBot
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.File
import java.util.*
import kotlin.concurrent.thread

object AnimatedWebPUtils {
    // TODO: We aren't currently using this because animated webps do NOT play inside embeds
    //  Reason 2: webp images do not look as sharp, this could be remedied by using sharp_yuv, but ffmpeg does not support that libwebp option
    //    ^ And img2webp does not support images from stdin, ffs...
    //  Reason 3: Reason 2 could be remedied by using libwebp directly, but guess what? I have NO IDEA on how to use libwebp with JNI/JNA because libwebp uses ".lib" files (wtf is that)
    //    So to use libwebp.dll you need to compile it manually, for fucks sake https://www.kopf.com.br/kaplof/libwebp-dll-binaries-for-windows-download-missing/
    fun renderAnimatedWebP(
        loritta: LorittaBot,
        resolution: String,
        framerate: Int,
        loopCount: Int,
        frames: List<BufferedImage>
    ): ByteArray {
        val id = UUID.randomUUID()
        val fileOutput = File("${loritta.config.loritta.folders.temp}\\webp-temp-$id.webp")

        val processBuilder = ProcessBuilder(
            loritta.config.loritta.binaries.ffmpeg,
            "-framerate",
            "$framerate",
            "-f",
            "rawvideo",
            "-pixel_format",
            "bgr24", // This is what the "BufferedImage.TYPE_3BYTE_BGR" uses behind the scenes
            "-video_size",
            resolution,
            "-i",
            "-", // We will write to output stream
            "-c:v",
            "libwebp",
            "-pix_fmt",
            "yuv420p", // using "bgra" causes ffmpeg to show a "Using libwebp for RGB-to-YUV conversion. You may want to consider passing in YUV instead for lossy encoding." warning
            // No one fucking documents these presets anywhere
            // You can see what the presets do by looking at the source code
            // https://github.com/webmproject/libwebp/blob/2ddaaf0aa544f615d08cff5e729092d05092d983/src/enc/config_enc.c#L61
            // ffmpeg -h encoder=webp shows all presets
            // but in my experience they don't change anything meaningful
            "-preset",
            "-1",
            "-loop",
            "$loopCount", // always loop
            "-lossless",
            "0",
            "-quality",
            "90", // 75 is the default quality in img2webp
            "-compression_level",
            "4", // less = bigger file size, faster
            "-y",
            // Due to the way WEBP containers work (it goes back after writing all data! like mp4 containers), we need to write directly to a file
            fileOutput.toString()
        ).redirectErrorStream(true)
            .start()

        thread {
            while (true) {
                val r = processBuilder.inputStream.read()
                if (r == -1) // Keep reading until end of input
                    return@thread

                // TODO: I think we should remove this later...
                print(r.toChar())
            }
        }

        for (frame in frames) {
            // println("Writing frame $frame")
            processBuilder.outputStream.write((frame.raster.dataBuffer as DataBufferByte).data)
            processBuilder.outputStream.flush()
        }

        processBuilder.outputStream.close()
        processBuilder.waitFor()

        val bytes = fileOutput.readBytes()
        fileOutput.delete()

        return bytes
    }
}