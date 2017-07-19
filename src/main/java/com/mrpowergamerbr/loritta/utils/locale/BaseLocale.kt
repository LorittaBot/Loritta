package com.mrpowergamerbr.loritta.utils.locale

/**
 * Classe de localização base, por padrão em PT-BR
 *
 * Locales diferentes devem extender esta classe
 */
open class BaseLocale {
	// CommandBase.kt
	var HOW_TO_USE = "Como usar"
	var EXAMPLE = "Exemplo"

	// HelloWorldCommand.kt
	var HELLO_WORLD = "Olá mundo! {0}"
	var HELLO_WORLD_DESCRIPTION = "Um simples comando para testar o sistema de localização da Loritta."
}