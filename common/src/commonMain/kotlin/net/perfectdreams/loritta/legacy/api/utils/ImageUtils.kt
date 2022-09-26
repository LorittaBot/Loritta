package net.perfectdreams.loritta.legacy.api.utils

import net.perfectdreams.loritta.legacy.api.utils.image.Image

expect fun createImage(width: Int, height: Int, imageType: Image.ImageType = Image.ImageType.ARGB): Image