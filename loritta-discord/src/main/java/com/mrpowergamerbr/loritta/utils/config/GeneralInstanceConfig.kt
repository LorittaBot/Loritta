package com.mrpowergamerbr.loritta.utils.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeneralInstanceConfig (
		@SerialName("machine-external-ip")
		val machineExternalIp: String,
		val loritta: LorittaInstanceConfig
) {
	@Serializable
	data class LorittaInstanceConfig(
			val folders: FoldersConfig,
			val website: WebsiteConfig,
			@SerialName("current-cluster-id")
			val currentClusterId: Long
	) {
		@Serializable
		data class WebsiteConfig(
				val url: String,
				@SerialName("cluster-url")
				val clusterUrl: String,
				val folder: String,
				val port: Int
		)
		@Serializable
		data class FoldersConfig(
				val root: String,
				val assets: String,
				val temp: String,
				val locales: String,
				val plugins: String,
				@SerialName("fan-arts")
				val fanArts: String
		)
	}
}