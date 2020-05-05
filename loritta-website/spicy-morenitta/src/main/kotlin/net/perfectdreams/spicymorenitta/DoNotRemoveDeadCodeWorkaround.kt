package net.perfectdreams.spicymorenitta

import ConfigureProfileView
import kotlinx.serialization.ImplicitReflectionSerializer

object DoNotRemoveDeadCodeWorkaround {
	@ImplicitReflectionSerializer
	val methodRefs = mutableListOf(
			ConfigureProfileView::start,
			ConfigureProfileView::prepareSave
	)
}