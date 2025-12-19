package net.perfectdreams.dora.assetmanager

import org.apache.commons.codec.digest.DigestUtils
import java.io.File

sealed class FrontendBundle {
    abstract val hash: String
    abstract val content: String

    class CachedBundle(override val content: String) : FrontendBundle() {
        override val hash = DigestUtils.md5Hex(content)
    }

    class FileSystemBundle(val file: File) : FrontendBundle() {
        override val hash
            get() = DigestUtils.md5Hex(content)
        override val content
            get() = this.file.readText(Charsets.UTF_8)
    }
}