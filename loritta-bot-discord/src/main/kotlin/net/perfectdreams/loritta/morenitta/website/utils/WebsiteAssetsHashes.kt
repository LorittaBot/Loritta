package net.perfectdreams.loritta.morenitta.website.utils

import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object WebsiteAssetsHashes {
	val websiteFileHashes = ConcurrentHashMap<String, String>()
	val legacyWebsiteFileHashes = ConcurrentHashMap<String, String>()

	fun getAssetHash(assetName: String): String {
		return if (websiteFileHashes.containsKey(assetName)) {
			websiteFileHashes[assetName]!!
		} else {
			val md5 = DigestUtils.md5Hex(File("${net.perfectdreams.loritta.morenitta.website.LorittaWebsite.FOLDER}/static/v2/$assetName").inputStream())
			websiteFileHashes[assetName] = md5
			md5
		}
	}

	/**
	 * Generates a MD5 hexadecimal hash for the file in the [assetName] path, useful for cache busting
	 *
	 * After the first hash generation, the hash will be cached
	 *
	 * @param assetName the file path
	 * @return the asset content hashed in MD5
	 */
	fun getAssetHashFromResources(assetName: String): String {
		return if (websiteFileHashes.containsKey(assetName)) {
			websiteFileHashes[assetName]!!
		} else {
			val md5 = DigestUtils.md5Hex(WebsiteAssetsHashes::class.java.getResourceAsStream("/static/${assetName.removePrefix("/")}"))
			websiteFileHashes[assetName] = md5
			md5
		}
	}

	fun getLegacyAssetHash(assetName: String): String {
		return if (legacyWebsiteFileHashes.containsKey(assetName)) {
			legacyWebsiteFileHashes[assetName]!!
		} else {
			val md5 = DigestUtils.md5Hex(File("${net.perfectdreams.loritta.morenitta.website.LorittaWebsite.FOLDER}/static/$assetName").inputStream())
			legacyWebsiteFileHashes[assetName] = md5
			md5
		}
	}
}