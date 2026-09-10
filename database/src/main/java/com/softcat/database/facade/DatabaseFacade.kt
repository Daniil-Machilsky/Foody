package com.softcat.database.facade

import android.net.Uri
import com.softcat.database.models.AvatarDbModel
import com.softcat.database.models.IngredientDbModel
import com.softcat.database.models.RecipeDbModel
import com.softcat.database.models.RecipeVectorDbModel
import com.softcat.database.models.ScoreDbModel
import com.softcat.database.models.TagDbModel
import com.softcat.database.models.UserDbModel

interface DatabaseFacade {

    suspend fun addToFavourites(userId: String, recipeId: Int): Result<Unit>

    suspend fun removeFromFavourites(userId: String, recipeId: Int): Result<Unit>

    suspend fun getFavourites(userId: String): Result<List<RecipeDbModel>>

    suspend fun saveScore(userId: String, score: ScoreDbModel): Result<Unit>

    suspend fun removeScore(userId: String, recipeId: Int): Result<Unit>

    suspend fun getScores(userId: String): Result<List<ScoreDbModel>>

    suspend fun getScoreValue(userId: String, recipeId: Int): Result<Int>

    suspend fun createUser(name: String, email: String, password: String): Result<UserDbModel>

    suspend fun verifyUser(email: String, password: String): Result<UserDbModel>

    suspend fun modifyUser(user: UserDbModel): Result<Unit>

    suspend fun saveAvatar(userId: String, uri: Uri): Result<String>

    suspend fun getAvatar(userId: String): Result<AvatarDbModel>

    suspend fun updateAvatar(userId: String, uri: Uri): Result<String>

    suspend fun searchRecipe(query: String, limit: Int): List<RecipeDbModel>

    suspend fun getRecipeSample(limit: Int): List<RecipeDbModel>

    suspend fun getRecipeIdSample(limit: Int): List<Int>

    suspend fun searchIngredient(query: String, limit: Int): List<IngredientDbModel>

    suspend fun searchTag(query: String, limit: Int): List<TagDbModel>

    suspend fun initialize(requiredCount: Int): Result<Unit>

    suspend fun getIngredients(limit: Int): List<IngredientDbModel>

    suspend fun getIngredients(ids: List<Int>): List<IngredientDbModel>

    suspend fun getAvailableIngredients(): List<IngredientDbModel>

    suspend fun addAvailableIngredient(ingredientId: Int)

    suspend fun removeAvailableIngredient(ingredientId: Int)

    suspend fun resetAvailableIngredients()

    suspend fun getTags(limit: Int): List<TagDbModel>

    suspend fun getFavouriteRecipeIds(userId: String): Result<List<Int>>

    suspend fun getRecipes(recipeIds: List<Int>): List<RecipeDbModel>

    suspend fun getAllRecipes(): List<RecipeDbModel>

    suspend fun setRecipeIsCooked(recipeId: Int, value: Boolean): Result<Unit>

    suspend fun isRecipeCooked(recipeId: Int): Boolean

    suspend fun getAvgScores(ids: List<Int>): Map<Int, Float>

    suspend fun exit()

    suspend fun getRecipeVectors(recipeIds: List<Int>): Pair<List<Int>, List<RecipeVectorDbModel>>

    suspend fun updateScoreCache(userId: String?): Result<Unit>
}