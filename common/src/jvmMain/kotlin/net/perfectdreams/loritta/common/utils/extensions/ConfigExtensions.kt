package net.perfectdreams.loritta.common.utils.extensions

import net.perfectdreams.loritta.common.utils.config.LorittaConfig
import java.io.File

inline val LorittaConfig.locales: String
    get() = "$repositoryFolder/locales"

inline val LorittaConfig.localesFolder: File
    get() = File(locales)