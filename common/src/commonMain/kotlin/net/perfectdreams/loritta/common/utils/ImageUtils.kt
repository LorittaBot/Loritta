package net.perfectdreams.loritta.common.utils

import net.perfectdreams.loritta.common.utils.image.Image

expect fun createImage(width: Int, height: Int, imageType: Image.ImageType = Image.ImageType.ARGB): Image