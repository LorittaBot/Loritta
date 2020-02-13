package com.mrpowergamerbr.loritta.website

import com.mrpowergamerbr.loritta.Loritta
import mu.KotlinLogging
import net.perfectdreams.loritta.website.utils.WebsiteAssetsHashes
import java.io.File

object OptimizeAssets {
	private val logger = KotlinLogging.logger {}
	var cssAssetVersion = 0L

	/**
	 * Optimizes the CSS files and sends the output to /static/assets/css/style.css
	 *
	 * Requires node.js and clean-css-cli
	 *
	 * @param check if the fingerprinting check should be done
	 */
	fun optimizeCss(check: Boolean = true) {
		WebsiteAssetsHashes.websiteFileHashes.clear()
		WebsiteAssetsHashes.legacyWebsiteFileHashes.clear()

		val root = File(Loritta.FRONTEND, "css")
		val output = File(Loritta.FRONTEND, "static/assets/css/style.css")
		val assetOrderFile = File(root, "asset_order")
		val assets = assetOrderFile.readLines().map { File(root, it) }
		val fingerprintFile = File(Loritta.FRONTEND, "css/fingerprints")
		val fingerprints = if (fingerprintFile.exists()) {
			File(Loritta.FRONTEND, "css/fingerprints").readLines()
		} else {
			mutableListOf()
		}

		cssAssetVersion = fingerprints.getOrNull(0)?.toLong() ?: 0L

		if (check) {
			// Ao carregar o path, nós iremos verificar se todos os arquivos não foram editados
			var requiresUpdate = false

			for ((index, asset) in assets.withIndex()) {
				val timestamp = fingerprints.getOrNull(index + 1) // A primeira linha é a versão atual do arquivo CSS

				if (timestamp == null) {
					requiresUpdate = true
					break
				}

				val lastModified = asset.lastModified()

				if (lastModified != timestamp.toLong()) {
					requiresUpdate = true
					break
				}
			}

			if (!requiresUpdate)
				return
		}

		this.cssAssetVersion += 1
		val newFingerprints = mutableListOf(cssAssetVersion.toString())
		for (asset in assets) {
			newFingerprints.add(asset.lastModified().toString())
		}
		fingerprintFile.writeText(newFingerprints.joinToString("\n"))

		// Se sim, vamos otimizar!
		val assetOrderList = assets.map { it.toString() }

		val args = mutableListOf(
				"/usr/lib/node_modules/clean-css-cli/bin/cleancss",
				"-O1",
				"-O2",
				"-o",
				output.toString()
		)

		args.addAll(assetOrderList)

		val myProcess = ProcessBuilder("node", *args.toTypedArray()).start()

		myProcess.waitFor()
		logger.info("Arquivo CSS atualizado! (Versão atual: ${cssAssetVersion}) - ${assetOrderList.size} arquivos CSS foram juntados e otimizados!")
	}
}