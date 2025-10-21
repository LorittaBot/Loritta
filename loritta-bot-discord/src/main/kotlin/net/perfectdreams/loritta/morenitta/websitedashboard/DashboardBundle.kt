package net.perfectdreams.loritta.morenitta.websitedashboard

import org.apache.commons.codec.digest.DigestUtils
import java.io.File

sealed class DashboardBundle {
    abstract val hash: String
    abstract val content: String

    class CachedBundle(override val content: String) : DashboardBundle() {
        override val hash = DigestUtils.md5Hex(content)
    }

    class FileSystemBundle(val file: File) : DashboardBundle() {
        override val hash
            get() = System.currentTimeMillis().toString()
        override val content
            get() = this.file.readText(Charsets.UTF_8)
    }
}