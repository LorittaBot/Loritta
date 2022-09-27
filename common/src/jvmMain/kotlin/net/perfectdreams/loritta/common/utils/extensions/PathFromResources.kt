package net.perfectdreams.loritta.common.utils.extensions

import java.nio.file.FileSystemNotFoundException
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.reflect.KClass

fun KClass<*>.getPathFromResources(path: String) = this.java.getPathFromResources(path)

fun Class<*>.getPathFromResources(path: String): Path? {
    // https://stackoverflow.com/a/67839914/7271796
    val resource = this.getResource(path) ?: return null
    val uri = resource.toURI()
    val dirPath = try {
        Paths.get(uri)
    } catch (e: FileSystemNotFoundException) {
        // If this is thrown, then it means that we are running the JAR directly (example: not from an IDE)
        val env = mutableMapOf<String, String>()
        FileSystems.newFileSystem(uri, env).getPath(path)
    }
    return dirPath
}