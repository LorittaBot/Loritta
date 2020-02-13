package net.perfectdreams.loritta.api.utils

import net.perfectdreams.loritta.api.utils.image.Image

expect fun createImage(width: Int, height: Int, imageType: Image.ImageType = Image.ImageType.ARGB): Image