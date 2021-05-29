// This is needed because it is always showing "inappropriate blocking call", even tho it is in a appropriate spot.
@file:Suppress("BlockingMethodInNonBlockingContext")

package net.perfectdreams.loritta.utils.extensions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.net.URL
import javax.imageio.ImageIO
import javax.imageio.stream.ImageInputStream

suspend fun readImage(file: File) = withContext(Dispatchers.IO) { ImageIO.read(file) }
suspend fun readImage(url: URL) = withContext(Dispatchers.IO) { ImageIO.read(url) }
suspend fun readImage(stream: InputStream) = withContext(Dispatchers.IO) { ImageIO.read(stream) }
suspend fun readImage(stream: ImageInputStream) = withContext(Dispatchers.IO) { ImageIO.read(stream) }