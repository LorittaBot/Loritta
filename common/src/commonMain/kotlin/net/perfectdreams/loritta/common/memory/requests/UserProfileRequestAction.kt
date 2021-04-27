package net.perfectdreams.loritta.common.pudding.requests

import net.perfectdreams.loritta.common.pudding.entities.UserProfile

interface UserProfileRequestAction : RequestAction<UserProfile?>, FindOrCreateRequestAction<UserProfile>