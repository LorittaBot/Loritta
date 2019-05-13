package com.mrpowergamerbr.loritta.commands

open class CommandOptions {
	// Todas as próximas opções são "command overrides", isto permite fazer overrides nas opções globais da Loritta
	var enableCustomAliases: Boolean = false
	var aliases = mutableListOf<String>()
	var enableCustomPrefix: Boolean = false
	var customPrefix: String = "+"

	var override: Boolean = false // Os comandos a seguir só serão ativados CASO override esteja ativo!
	var explainOnCommandRun = true // Explicar quando rodar *comando*? (Ou quando usar *comando* :shrug:)
	var explainInPrivate = false // Caso explainOnCommandRun estiver ativado, é para explicar APENAS no privado ou mandar no global?
	var commandOutputInPrivate = false // É para mandar o output (ou seja, tudo do comando) no privado em vez de mandar no global?
	var mentionOnCommandOutput = true // Caso esteja ativado, a Loritta irá marcar quem executou na mensagem resposta
	var deleteMessageAfterCommand = false // Deletar mensagem do comando após executar ele?
	var blacklistedChannels: List<String> = ArrayList() // Canais em que este comando é bloqueado
	// Comandos podem extender a classe CommandOptions para colocar novas opções
}
