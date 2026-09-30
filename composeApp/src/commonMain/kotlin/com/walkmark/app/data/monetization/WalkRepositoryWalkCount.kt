package com.walkmark.app.data.monetization

import com.walkmark.app.domain.monetization.PersistedWalkCount
import com.walkmark.app.domain.repository.WalkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class WalkRepositoryWalkCount(
    private val walkRepository: WalkRepository
) : PersistedWalkCount {

    override val walkCount: Flow<Int> = walkRepository.observeAllWalks().map { it.size }

    override suspend fun currentWalkCount(): Int = walkRepository.observeAllWalks().first().size
}
