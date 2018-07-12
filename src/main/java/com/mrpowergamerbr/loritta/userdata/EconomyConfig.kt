package com.mrpowergamerbr.loritta.userdata

class EconomyConfig {
	// Sistema de economia local para servidores
	@AllowReflection
	var isEnabled: Boolean = false
	// Nome da moeda
	@AllowReflection
	var economyName: String? = null
	// Nome da moeda (plural)
	@AllowReflection
	var economyNamePlural: String? = null
	// Taxa de transferência de Sonhos -> Local
	@AllowReflection
	var exchangeRate: Double? = null
}