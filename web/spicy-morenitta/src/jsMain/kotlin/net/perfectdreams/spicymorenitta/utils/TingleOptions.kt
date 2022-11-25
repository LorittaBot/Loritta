package net.perfectdreams.spicymorenitta.utils

external interface TingleOptions {
	var footer: Boolean?
	var stickyFooter: Boolean?
	var closeMethods: Array<String>
	var closeLabel: String?
	var cssClass: Array<String>
	var onOpen: (() -> Unit)?
	var onClose: (() -> Unit)?
	var beforeClose: (() -> Boolean)?
}