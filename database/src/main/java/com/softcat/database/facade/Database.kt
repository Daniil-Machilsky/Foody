package com.softcat.database.facade

import android.net.Uri
import com.softcat.database.local.dao.AvgScoreDao
import com.softcat.database.local.dao.IngredientDao
import com.softcat.database.models.RecipeDbModel
import com.softcat.database.models.ScoreDbModel
import com.softcat.database.remote.interfaces.AvatarsManager
import com.softcat.database.remote.interfaces.FavouritesManager
import com.softcat.database.local.dao.RecipeDao
import com.softcat.database.local.dao.RecipeVectorDao
import com.softcat.database.local.dao.TagDao
import com.softcat.database.models.RecipeVectorDbModel
import com.softcat.database.models.UserDbModel
import com.softcat.database.remote.interfaces.InitializeManager
import com.softcat.database.remote.interfaces.ScoreManager
import com.softcat.database.remote.interfaces.UsersManager
import javax.inject.Inject

class Database @Inject constructor(
    private val usersManager: UsersManager,
    private val favouritesManager: FavouritesManager,
    private val scoreManager: ScoreManager,
    private val avatarsManager: AvatarsManager,
    private val recipeDao: RecipeDao,
    private val ingredientDao: IngredientDao,
    private val tagDao: TagDao,
    private val avgScoreDao: AvgScoreDao,
    private val recipeVectorDao: RecipeVectorDao,
    private val initializeManager: InitializeManager
): DatabaseFacade {

    override suspend fun addToFavourites(
        userId: String,
        recipeId: Int
    ) = favouritesManager.add(userId, recipeId)

    override suspend fun removeFromFavourites(
        userId: String,
        recipeId: Int
    ) = favouritesManager.remove(userId, recipeId)

    override suspend fun getFavourites(userId: String): Result<List<RecipeDbModel>> {
        val ids = favouritesManager.get(userId).getOrElse {
            return Result.failure(it)
        }
        return try {
            val recipes = ids.mapNotNull { recipeDao.get(it) }
            Result.success(recipes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveScore(
        userId: String,
        score: ScoreDbModel
    ) = scoreManager.add(userId, score)

    override suspend fun removeScore(
        userId: String,
        recipeId: Int
    ) = scoreManager.remove(userId, recipeId)

    override suspend fun getScores(userId: String) = scoreManager.get(userId)

    override suspend fun getScoreValue(userId: String, recipeId: Int) = scoreManager.getScoreValue(userId, recipeId)

    override suspend fun createUser(
        name: String,
        email: String,
        password: String
    ): Result<UserDbModel> {
        val result = usersManager.createUser(name, email, password)
        if (result.isSuccess) {
            val userId = result.getOrNull()?.id
            userId?.let {
                scoreManager.updateScoreCache(it)
            }
        }
        return result
    }

    override suspend fun verifyUser(
        email: String,
        password: String
    ): Result<UserDbModel> {
        val result = usersManager.enter(email, password)
        if (result.isSuccess) {
            val userId = result.getOrNull()?.id
            userId?.let {
                scoreManager.updateScoreCache(it)
            }
        }
        return result
    }

    override suspend fun modifyUser(user: UserDbModel) = usersManager.modify(user)

    override suspend fun saveAvatar(
        userId: String,
        uri: Uri
    ) = avatarsManager.save(userId, uri)

    override suspend fun getAvatar(userId: String) = avatarsManager.get(userId)

    override suspend fun updateAvatar(
        userId: String,
        uri: Uri
    ): Result<String> {
        avatarsManager.delete(userId).getOrElse {
            return Result.failure(it)
        }
        return avatarsManager.save(userId, uri)
    }

    override suspend fun searchRecipe(query: String, limit: Int) = recipeDao.search(query, limit)

    override suspend fun getRecipeSample(limit: Int) = recipeDao.getSample(limit)

    override suspend fun searchIngredient(query: String, limit: Int) = ingredientDao.search(query, limit)

    override suspend fun searchTag(query: String, limit: Int) = tagDao.search(query, limit)

    override suspend fun initialize(requiredCount: Int): Result<Unit> {
        initializeManager.initializeRecipes(requiredCount).getOrElse {
            return Result.failure(it)
        }
        initializeManager.initializeAvgScores().getOrElse {
            return Result.failure(it)
        }
        initializeManager.initializeRecommendationModel().getOrElse {
            return Result.failure(it)
        }
        return Result.success(Unit)
    }

    override suspend fun getIngredients(limit: Int) = ingredientDao.getSample(limit)

    override suspend fun getIngredients(ids: List<Int>) = ids.mapNotNull {
        ingredientDao.get(it)
    }

    override suspend fun getAvailableIngredients() = ingredientDao.getAvailableIngredients()

    override suspend fun addAvailableIngredient(ingredientId: Int) = ingredientDao.setAvailable(ingredientId)

    override suspend fun removeAvailableIngredient(ingredientId: Int) = ingredientDao.setUnavailable(ingredientId)

    override suspend fun resetAvailableIngredients() = ingredientDao.resetAvailableIngredients()

    override suspend fun getTags(limit: Int) = tagDao.getSample(limit)

    override suspend fun getFavouriteRecipeIds(userId: String) = favouritesManager.get(userId)

    override suspend fun getRecipes(recipeIds: List<Int>) = recipeIds.mapNotNull { recipeDao.get(it) }

    override suspend fun getAllRecipes() = recipeDao.getAll()

    override suspend fun setRecipeIsCooked(recipeId: Int, value: Boolean): Result<Unit> {
        return try {
            Result.success(recipeDao.setIsCooked(recipeId, value))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isRecipeCooked(recipeId: Int) = recipeDao.isCooked(recipeId)

    override suspend fun getAvgScores(ids: List<Int>): Map<Int, Float> {
        return mutableMapOf<Int, Float>().apply {
            avgScoreDao.getAll(ids).forEach {
                this[it.recipeId] = it.value
            }
        }
    }

    override suspend fun exit() {
        usersManager.exit()
        scoreManager.updateScoreCache(null)
    }

    override suspend fun getRecipeVectors(recipeIds: List<Int>): List<RecipeVectorDbModel> {
        val models = recipeVectorDao.getByIds(recipeIds)
        val modelsMap = models.associateBy { it.id }
        return recipeIds.mapNotNull { recipeId -> modelsMap[recipeId] }
    }

    override suspend fun updateScoreCache(userId: String?) = scoreManager.updateScoreCache(userId)
}