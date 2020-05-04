package com.mrpowergamerbr.loritta.commands.nashorn

import org.bson.types.ObjectId

/**
 * Comandos usando a Nashorn Engine
 */
class LegacyNashornCommand {
	var id = ObjectId() // Object ID único para cada comando
	var jsLabel = "loritta" // label do comando
	lateinit var javaScript: String // código em JS do comando
	var jsAliases: List<String> = ArrayList() // aliases
	var isEnabled = true // Se o comando está ativado
	// var createdDate = LocalDateTime.now() // Data criada
	// var editedDate = LocalDateTime.now() // Data editada
	var authors: List<String> = ArrayList() // Autores do comando (ou seja, quem mexeu)
	var isPublic = false // Se o comando é público no repositório de comandos
	var isForked = false // Se é uma cópia de outro comando na repo de cmds
	var upstreamId: ObjectId? = null // Caso seja forked, o upstreamId irá ter o Object ID original
	var useNewAPI: Boolean = false
	var description: String? = null
}