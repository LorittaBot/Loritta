import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.gradle.workers.WorkAction
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

abstract class GenerateOptimizedImage : WorkAction<GenerateOptimizedImageParameters> {
    override fun execute() {
        val fileIndex = parameters.fileIndex.get()
        val sourceFile = parameters.sourceFile.get().asFile
        val temporaryFolder = parameters.temporaryFolder.get().asFile
        val targetFile = parameters.targetFile.get().asFile
        val targetFolder = parameters.targetFolder.get().asFile
        val pngQuantPathString = parameters.pngQuantPathString.get()
        val gifsiclePathString = parameters.gifsiclePathString.get()

        try {
            temporaryFolder.mkdirs()
            targetFile.parentFile.mkdirs()

            val targetFileRelativeToTheBaseFolder = targetFile.relativeTo(targetFolder)

            val imageType = sourceFile.extension
            val imagesInfo = mutableListOf<ImageInfo>()

            when (imageType) {
                "png" -> {
                    // Load the image
                    val originalImage = ImageIO.read(sourceFile)

                    println("Copying ${sourceFile}...")

                    // Copy current image as is
                    sourceFile.copyTo(targetFile, true)

                    optimizePNG(pngQuantPathString, targetFile)

                    val variations = mutableListOf<ImageInfo>()

                    // Optimize images based on our width sizes array
                    for (newWidth in ImageOptimizerTask.widthSizes.filter { originalImage.width >= it }) {
                        val newHeight = (newWidth * originalImage.height) / originalImage.width
                        println("Downscaling $sourceFile to ${newWidth}x${newHeight}...")

                        // Scale down to the width
                        val downscaledImage = toBufferedImage(
                            originalImage.getScaledInstance(
                                newWidth,
                                (newWidth * originalImage.height) / originalImage.width,
                                BufferedImage.SCALE_SMOOTH
                            )
                        )
                        val downscaledImageFile = File(
                            targetFile.parentFile,
                            targetFile.nameWithoutExtension + "_${newWidth}w.${targetFile.extension}"
                        )

                        ImageIO.write(
                            downscaledImage,
                            "png",
                            downscaledImageFile
                        )

                        optimizePNG(pngQuantPathString, downscaledImageFile)

                        variations.add(
                            ImageInfo(
                                downscaledImageFile.relativeTo(targetFolder).toString().replace("\\", "/"),
                                downscaledImage.width,
                                downscaledImage.height,
                                downscaledImageFile.length(),
                                null
                            )
                        )
                    }

                    val imageInfo = ImageInfo(
                        targetFileRelativeToTheBaseFolder.toString().replace("\\", "/"),
                        originalImage.width,
                        originalImage.height,
                        sourceFile.length(),
                        variations
                    )

                    imagesInfo.add(imageInfo)
                }
                "gif" -> {
                    // Load the image
                    // https://stackoverflow.com/a/39131371/7271796
                    val originalImage = ImageIO.read(sourceFile)

                    println("Copying ${sourceFile}...")

                    // Copy current image as is
                    sourceFile.copyTo(targetFile, true)

                    optimizeGIF(gifsiclePathString, targetFile, targetFile)

                    val variations = mutableListOf<ImageInfo>()

                    // Optimize images based on our width sizes array
                    for (newWidth in ImageOptimizerTask.widthSizes.filter { originalImage.width >= it }) {
                        val newHeight = (newWidth * originalImage.height) / originalImage.width
                        println("Downscaling $sourceFile to ${newWidth}x${newHeight}...")

                        val downscaledImageFile = File(
                            targetFile.parentFile,
                            targetFile.nameWithoutExtension + "_${newWidth}w.${targetFile.extension}"
                        )

                        resizeGIF(gifsiclePathString, sourceFile, downscaledImageFile, newWidth, newHeight)
                        optimizeGIF(gifsiclePathString, downscaledImageFile, downscaledImageFile)

                        variations.add(
                            ImageInfo(
                                downscaledImageFile.relativeTo(targetFolder).toString().replace("\\", "/"),
                                newWidth,
                                newHeight,
                                downscaledImageFile.length(),
                                null
                            )
                        )
                    }

                    val imageInfo = ImageInfo(
                        targetFileRelativeToTheBaseFolder.toString().replace("\\", "/"),
                        originalImage.width,
                        originalImage.height,
                        sourceFile.length(),
                        variations
                    )

                    imagesInfo.add(imageInfo)
                }
                else -> {
                    println("Copying $sourceFile (no optimizations)...")

                    // Copy current image as is
                    sourceFile.copyTo(targetFile, true)
                }
            }

            // Yes, this is the "correct" way to do this
            // It is weird, but it works... so, umm... yay?
            // https://discuss.gradle.org/t/returning-data-from-a-gradle-workaction/32850
            File(temporaryFolder, "$fileIndex.json")
                .writeText(Json.encodeToString(ListSerializer(ImageInfo.serializer()), imagesInfo))
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun optimizePNG(pngQuantPathString: String, file: File) {
        val originalFileSize = file.length()

        // https://stackoverflow.com/questions/39894913/how-do-i-get-the-best-png-compression-with-gulp-imagemin-plugins
        val proc = ProcessBuilder(
            pngQuantPathString,
            "--quality=70-90",
            "--strip",
            "-f",
            "--ext",
            ".png",
            "--skip-if-larger",
            "--speed",
            "1",
            file.absolutePath
        ).start()

        val result = proc.inputStream.readAllBytes()
        val errorStreamResult = proc.errorStream.readAllBytes()

        val s = proc.waitFor()

        // println("pngquant's input stream: ${result.toString(Charsets.UTF_8)}")
        // println("pngquant's error stream: ${errorStreamResult.toString(Charsets.UTF_8)}")
        // https://manpages.debian.org/testing/pngquant/pngquant.1.en.html
        // 99 = if quality can't match what we want, pngquant exists with exit code 99
        // 98 = If conversion results in a file larger than the original, the image won't be saved and pngquant will exit with status code 98.
        if (s != 0 && s != 98 && s != 99) { // uuuh, this shouldn't happen if this is a PNG image...
            error("Something went wrong while trying to optimize PNG image! Status = $s; Error stream: ${errorStreamResult.toString(Charsets.UTF_8)}")
        }

        val newFileSize = file.length()
        println("Successfully optimized ${file.name}! $originalFileSize -> $newFileSize")
    }

    private fun resizeGIF(gifsiclePathString: String, file: File, output: File, width: Int, height: Int) {
        val proc = ProcessBuilder(
            gifsiclePathString,
            "--resize",
            "${width}x$height",
            "-o",
            "${output.absolutePath}",
            file.absolutePath
        ).start()

        val result = proc.inputStream.readAllBytes()
        val errorStreamResult = proc.errorStream.readAllBytes()

        val s = proc.waitFor()

        if (s != 0) {
            error("Something went wrong while trying to resize GIF image! Status = $s; Error stream: ${errorStreamResult.toString(Charsets.UTF_8)}")
        }

        println("Successfully resized ${file.name}!")
    }

    private fun optimizeGIF(gifsiclePathString: String, file: File, output: File) {
        val proc = ProcessBuilder(
            gifsiclePathString,
            "-O3",
            "--lossy=25", // Optimize GIFs a LOT
            "--colors=64", // who cares about colors right
            "-o",
            "${output.absolutePath}",
            file.absolutePath
        ).start()

        val result = proc.inputStream.readAllBytes()
        val errorStreamResult = proc.errorStream.readAllBytes()

        val s = proc.waitFor()

        if (s != 0) {
            error("Something went wrong while trying to optimize GIF image! Status = $s; Error stream: ${errorStreamResult.toString(Charsets.UTF_8)}")
        }

        println("Successfully optimize ${file.name}!")
    }


    /**
     * Converts a given Image into a BufferedImage
     *
     * @param img The Image to be converted
     * @return The converted BufferedImage
     */
    private fun toBufferedImage(img: Image): BufferedImage {
        if (img is BufferedImage) {
            return img
        }

        // Create a buffered image with transparency
        val bimage = BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB)

        // Draw the image on to the buffered image
        val bGr = bimage.createGraphics()
        bGr.drawImage(img, 0, 0, null)
        bGr.dispose()

        // Return the buffered image
        return bimage
    }
}