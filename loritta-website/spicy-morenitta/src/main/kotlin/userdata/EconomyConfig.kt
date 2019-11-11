package userdata

class EconomyConfig {
	// Sistema de economia local para servidores
	var isEnabled: Boolean = false
	// Nome da moeda
	var economyName: String? = null
	// Nome da moeda (plural)
	var economyNamePlural: String? = null
	// Taxa de transferência de Sonhos -> Local
	var exchangeRate: Double? = null
}