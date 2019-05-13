package com.mrpowergamerbr.loritta.utils.correios

class EncomendaResponse {
	var date = "???"
	var time = "???"
	var state = "???"
	var error = "???"
	var locations: List<PackageUpdate> = listOf()

	class PackageUpdate {
		var state = "???"
		var reason = "???"
		var location = "???"
		var receiver = "???"
		var date = "???"
		var time = "???"
	}
}