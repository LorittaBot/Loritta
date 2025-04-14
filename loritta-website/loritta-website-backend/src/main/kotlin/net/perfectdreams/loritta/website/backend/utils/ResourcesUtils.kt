package net.perfectdreams.loritta.website.backend.utils

import net.perfectdreams.loritta.website.backend.LorittaWebsiteBackend
import java.nio.file.FileSystemNotFoundException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Stream

object ResourcesUtils {
    // https://stackoverflow.com/a/50470554/7271796
    fun listFiles(path: String): Stream<Path> {
        val uri = LorittaWebsiteBackend::class.java.getResource(path).toURI()
        val dirPath = try {
            Paths.get(uri)
        } catch (e: FileSystemNotFoundException) {
            // If this is thrown, then it means that we are running the JAR directly (example: not from an IDE)
            val env = mutableMapOf<String, String>()
            FileSystems.newFileSystem(uri, env).getPath("/locales/")
        }

        return Files.list(dirPath)
    }
}