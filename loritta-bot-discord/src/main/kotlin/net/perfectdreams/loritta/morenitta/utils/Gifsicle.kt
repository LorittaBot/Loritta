package net.perfectdreams.loritta.morenitta.utils

import java.io.File
import java.util.concurrent.TimeUnit

class Gifsicle(val path: String) {
    fun optimizeGIF(file: File, lossy: Int = 200) {
        val processBuilder = ProcessBuilder(
            path, // https://github.com/kohler/gifsicle
            "-i",
            file.toString(),
            "-O3",
            "--lossy=$lossy",
            "--colors",
            "256",
            "-o",
            file.toString()
        )

        val process = processBuilder.start()
        process.waitFor(10, TimeUnit.SECONDS)
    }
}