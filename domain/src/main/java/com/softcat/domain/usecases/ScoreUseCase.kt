package com.softcat.domain.usecases

import com.softcat.domain.entities.Score
import com.softcat.domain.interfaces.ScoreRepository
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject

class ScoreUseCase @Inject constructor(
    private val repository: ScoreRepository
) {
    suspend fun save(userId: String, recipeId: Int, value: Int): Result<Unit> {
        Timber.i("${this::class.simpleName} save($userId, $recipeId, $value) invoked")
        return repository.saveScore(userId, recipeId, value)
    }

    suspend fun remove(userId: String, recipeId: Int): Result<Unit> {
        Timber.i("${this::class.simpleName} remove($userId, $recipeId) invoked")
        return repository.removeScore(userId, recipeId)
    }

    suspend fun observe(userId: String): StateFlow<List<Score>> {
        Timber.i("${this::class.simpleName} observe($userId) invoked")
        return repository.observeScores(userId)
    }

    suspend fun observeScoreValue(userId: String, recipeId: Int): StateFlow<Int> {
        Timber.i("${this::class.simpleName} observeScoreValue($userId, $recipeId) invoked")
        return repository.observeScoreValue(userId, recipeId)
    }
}