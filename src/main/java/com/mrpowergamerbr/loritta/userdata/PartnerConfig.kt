package com.mrpowergamerbr.loritta.userdata

import com.mrpowergamerbr.loritta.utils.LorittaPartner

class PartnerConfig {
	var isPartner = false
	var vanityUrl: String? = null
	var tagline: String? = null
	var keywords = mutableListOf<LorittaPartner.Keyword>()
	val languages = mutableListOf<LorittaPartner.Language>()
}