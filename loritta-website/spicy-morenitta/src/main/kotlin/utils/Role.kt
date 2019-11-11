package utils

class Role(
		val id: String,
		val name: String,
		val isPublicRole: Boolean,
		val isManaged: Boolean,
		val canInteract: Boolean,
		val color: LoriColor?
)