package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class GeneralInstanceConfig @JsonCreator constructor(
		val loritta: LorittaInstanceConfig
) {
	class LorittaInstanceConfig @JsonCreator constructor(
			val folders: FoldersConfig,
			val website: WebsiteConfig,
			val currentClusterId: Long
	) {
		class WebsiteConfig @JsonCreator constructor(
				val url: String,
				val clusterUrl: String,
				val folder: String,
				val port: Int
		)

		class FoldersConfig @JsonCreator constructor(
				val root: String,
				val assets: String,
				val temp: String,
				val locales: String,
				val plugins: String,
				val fanArts: String
		)
	}
}