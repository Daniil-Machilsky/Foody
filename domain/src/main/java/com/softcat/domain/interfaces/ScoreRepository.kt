package com.softcat.domain.interfaces

import com.softcat.domain.entities.Score
import kotlinx.coroutines.flow.StateFlow

interface ScoreRepository {

    suspend fun saveScore(userId: String, recipeId: Int, value: Int): Result<Unit>

    suspend fun removeScore(userId: String, recipeId: Int): Result<Unit>

    suspend fun observeScores(userId: String): StateFlow<List<Score>>

    suspend fun observeScoreValue(userId: String, recipeId: Int): StateFlow<Int>
}