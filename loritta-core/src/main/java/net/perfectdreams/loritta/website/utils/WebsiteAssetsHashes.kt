package net.perfectdreams.loritta.website.utils

import net.perfectdreams.loritta.website.LorittaWebsite
import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object WebsiteAssetsHashes {
	val websiteFileHashes = ConcurrentHashMap<String, String>()
	val legacyWebsiteFileHashes = ConcurrentHashMap<String, String>()

	fun getAssetHash(assetName: String): String {
		return if (websiteFileHashes.contains(assetName)) {
			websiteFileHashes[assetName]!!
		} else {
			val md5 = DigestUtils.md5Hex(File("${LorittaWebsite.INSTANCE.config.websiteFolder}/static/$assetName").inputStream())
			websiteFileHashes[assetName] = md5
			md5
		}
	}

	fun getLegacyAssetHash(assetName: String): String {
		return if (legacyWebsiteFileHashes.contains(assetName)) {
			legacyWebsiteFileHashes[assetName]!!
		} else {
			val md5 = DigestUtils.md5Hex(File("${com.mrpowergamerbr.loritta.website.LorittaWebsite.FOLDER}/static/$assetName").inputStream())
			legacyWebsiteFileHashes[assetName] = md5
			md5
		}
	}
}