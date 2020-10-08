package net.perfectdreams.spicymorenitta

import ConfigureProfileView

object DoNotRemoveDeadCodeWorkaround {
	val methodRefs = mutableListOf(
			ConfigureProfileView::start,
			ConfigureProfileView::prepareSave
	)
}