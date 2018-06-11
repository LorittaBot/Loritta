package com.mrpowergamerbr.loritta.parallax

import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject

fun main(args: Array<String>) {
	val input = "{@user} adicionou o cargo {&ABCDEF} agora ele(a) terá acesso a todos os canais de games {role:+ABCDEF} {announce:comandos-log} {require:ABCDEF} {delete}"
	var contentInput = input

	// start parallax processing tool
	val rolePattern = "\\{role:(\\+|-)([^\\}]+)\\}".toPattern().matcher(input)
	val announcePattern = "\\{announce:([^\\}]+)\\}".toPattern().matcher(input)
	val requirePattern = "\\{require:([^\\}]+)\\}".toPattern().matcher(input)
	val blockPattern = "\\{block:([^\\}]+)\\}".toPattern().matcher(input)

	val giveRoles = mutableListOf<String>()
	val removeRoles = mutableListOf<String>()

	val announceIn = mutableListOf<String>()

	val requireRoles = mutableListOf<String>()
	val blockRoles = mutableListOf<String>()

	val autoDelete = input.contains("{delete}")
	contentInput = contentInput.replace("{delete}", "")

	while (rolePattern.find()) {
		val remove = rolePattern.group(1) == "-"
		val roleName = rolePattern.group(2)

		if (remove)
			removeRoles.add(roleName)
		else
			giveRoles.add(roleName)

		contentInput = contentInput.replace(rolePattern.group(), "")
	}

	while (announcePattern.find()) {
		announceIn.add(announcePattern.group(1))
		contentInput = contentInput.replace(announcePattern.group(), "")
	}

	while (requirePattern.find()) {
		requireRoles.add(requirePattern.group(1))
		contentInput = contentInput.replace(requirePattern.group(), "")
	}

	while (blockPattern.find()) {
		blockRoles.add(requirePattern.group(1))
		contentInput = contentInput.replace(blockPattern.group(), "")
	}

	contentInput = contentInput.replace("\"", "\\")
	contentInput = contentInput.replace("{@user}", "\" + author + \"")

	contentInput = contentInput.trim()
	val jsonObject = JsonObject()
	jsonObject["type"] = "xinxyla"
	jsonObject["input"] = input

	// transform to JavaScript
	var javaScript = "// Custom Loritta Metadata - DO NOT EDIT!\n"
	javaScript += "// $jsonObject\n\n"

	var variableIndex = 0
	for (role in requireRoles) {
		javaScript += "var role$variableIndex = guild.getRolesByName(\"${role}\", false)[0];\nif (!member.hasRole(role$variableIndex)) { message.reply(\"Sem Permissão!\"); return; }\n\n"
		variableIndex++
	}

	for (role in blockRoles) {
		javaScript += "var role$variableIndex = guild.getRolesByName(\"${role}\", false)[0];\nif (member.hasRole(role$variableIndex)) { message.reply(\"Sem Permissão!\"); return; }\n\n"
		variableIndex++
	}

	if (announceIn.isEmpty()) {
		javaScript += "message.reply(\"${contentInput}\");\n\n"
	} else {
		for (role in announceIn) {
			javaScript += "var channel$variableIndex = guild.getTextChannelsByName(\"${role}\", false)[0];\nchannel$variableIndex.send(\"${contentInput}\");\n\n"
			variableIndex++
		}
	}

	for (role in giveRoles) {
		javaScript += "var role$variableIndex = guild.getRolesByName(\"${role}\", false)[0];\nmember.addRole(role$variableIndex);\n\n"
		variableIndex++
	}

	for (role in removeRoles) {
		javaScript += "var role$variableIndex = guild.getRolesByName(\"${role}\", false)[0];\nmember.removeRole(role$variableIndex);\n\n"
		variableIndex++
	}

	if (autoDelete) {
		javaScript += "message.delete()\n\n"
	}

	javaScript += "// USE NEW API"
	println(javaScript)
}