package net.perfectdreams.loritta.cinnamon.common.requests

import net.perfectdreams.loritta.cinnamon.common.entities.UserProfile

interface UserProfileRequestAction : RequestAction<UserProfile?>, FindOrCreateRequestAction<UserProfile>