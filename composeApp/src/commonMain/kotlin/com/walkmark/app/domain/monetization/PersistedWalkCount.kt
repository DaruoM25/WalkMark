package com.walkmark.app.domain.monetization

import kotlinx.coroutines.flow.Flow

interface PersistedWalkCount {

    val walkCount: Flow<Int>

    suspend fun currentWalkCount(): Int
}
