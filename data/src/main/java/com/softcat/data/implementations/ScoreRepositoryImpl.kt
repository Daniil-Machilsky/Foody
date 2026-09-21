package com.softcat.data.implementations

import com.softcat.data.mapper.toDbModel
import com.softcat.data.mapper.toListEntity
import com.softcat.database.exceptions.ScoreIsAbsentException
import com.softcat.database.exceptions.ScoresNodeIsAbsentException
import com.softcat.database.facade.DatabaseFacade
import com.softcat.domain.entities.Score
import com.softcat.domain.interfaces.ScoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar
import javax.inject.Inject

class ScoreRepositoryImpl @Inject constructor(
    private val database: DatabaseFacade
): ScoreRepository {

    private var selectedUserId: String? = null
    private var selectedRecipeId: Int? = null

    private val scoreListFlow = MutableStateFlow<List<Score>>(emptyList())
    private val scoreValueFlow = MutableStateFlow(0)

    override suspend fun saveScore(
        userId: String,
        recipeId: Int,
        value: Int
    ): Result<Unit> {
        val score = Score(recipeId, value, Calendar.getInstance())
        val result = database.saveScore(userId, score.toDbModel()).onSuccess {
            updateFlows()
        }
        return result
    }

    override suspend fun removeScore(
        userId: String,
        recipeId: Int
    ): Result<Unit> {
        val result = database.removeScore(userId, recipeId).onSuccess {
            updateFlows()
        }
        return result
    }

    override suspend fun observeScores(userId: String): StateFlow<List<Score>> {
        selectedUserId = userId
        updateFlows()
        return scoreListFlow
    }

    override suspend fun observeScoreValue(
        userId: String,
        recipeId: Int
    ): StateFlow<Int> {
        selectedUserId = userId
        selectedRecipeId = recipeId
        updateFlows()
        return scoreValueFlow
    }

    private suspend fun updateFlows() {
        val userId = selectedUserId ?: return
        database.getScores(userId).onSuccess {
            scoreListFlow.value = it.toListEntity()
        }.onFailure { e ->
            if (e is ScoresNodeIsAbsentException)
                scoreListFlow.value = emptyList()
        }
        val recipeId = selectedRecipeId ?: return
        database.getScoreValue(userId, recipeId).onSuccess {
            scoreValueFlow.value = it
        }.onFailure { e ->
            if (e is ScoreIsAbsentException)
                scoreValueFlow.value = 0
        }
    }
}