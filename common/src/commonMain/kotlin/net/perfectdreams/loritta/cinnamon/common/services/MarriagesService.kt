package net.perfectdreams.loritta.cinnamon.common.services

import net.perfectdreams.loritta.cinnamon.common.entities.Marriage

interface MarriagesService {
    suspend fun getMarriageByUser(userId: Long): Marriage?
}