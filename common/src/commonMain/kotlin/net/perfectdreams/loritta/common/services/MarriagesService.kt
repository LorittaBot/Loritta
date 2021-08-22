package net.perfectdreams.loritta.common.services

import net.perfectdreams.loritta.common.entities.Marriage

interface MarriagesService {
    suspend fun getMarriageByUser(userId: Long): Marriage?
}