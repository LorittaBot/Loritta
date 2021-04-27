package net.perfectdreams.loritta.common.requests

import net.perfectdreams.loritta.common.entities.UserProfile

interface UserProfileRequestAction : RequestAction<UserProfile?>, FindOrCreateRequestAction<UserProfile>