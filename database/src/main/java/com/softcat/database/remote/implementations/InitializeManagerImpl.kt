package com.softcat.database.remote.implementations

import com.softcat.database.BuildConfig
import com.softcat.database.facade.readFloat32LE
import com.softcat.database.facade.readInt32LE
import com.softcat.database.facade.readRecipe
import com.softcat.database.facade.readString
import com.softcat.database.local.dao.IngredientDao
import com.softcat.database.local.dao.RecipeDao
import com.softcat.database.local.dao.RecipeVectorDao
import com.softcat.database.local.dao.TagDao
import com.softcat.database.models.IngredientDbModel
import com.softcat.database.models.RecipeVectorDbModel
import com.softcat.database.models.TagDbModel
import com.softcat.database.remote.interfaces.InitializeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import kotlin.math.min

class InitializeManagerImpl @Inject constructor(
    private val recipeDao: RecipeDao,
    private val ingredientDao: IngredientDao,
    private val tagDao: TagDao,
    private val recipeVectorDao: RecipeVectorDao
): InitializeManager {

    private val scope =  CoroutineScope(Dispatchers.IO)

    override suspend fun initializeRecipes(requiredCount: Int): Result<Unit> {
        return try {
            recipeDao.clear()
            tagDao.clear()
            ingredientDao.clear()

            downloadFileAndProcess(TAGS_FILE_URL) { stream ->
                val tags = List(stream.readInt32LE()) {
                    TagDbModel(stream.readInt32LE(), stream.readString())
                }
                tagDao.insertAll(tags)
            }
            downloadFileAndProcess(INGREDIENTS_FILE_URL) { stream ->
                val ingredients = List(stream.readInt32LE()) {
                    val id = stream.readInt32LE()
                    val name = stream.readString()
                    val category = stream.read()
                    val isDefaultAvailable = stream.read() == 1

                    IngredientDbModel(
                        id = id,
                        name = name,
                        category = category,
                        isDefaultAvailable = isDefaultAvailable,
                        isAvailable = isDefaultAvailable
                    )
                }
                ingredientDao.insertAll(ingredients)
            }
            downloadFileAndProcess(RECIPES_FILE_URL) { stream ->
                readRecipes(stream, requiredCount)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun initializeRecommendationModel(): Result<Unit> {
        recipeVectorDao.clear()
        return try {
            downloadFileAndProcess(RECIPE_VECTORS_URL) { stream ->
                readRecipeVectors(stream)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun readRecipes(stream: InputStream, requiredCount: Int) {
        var n = stream.readInt32LE()
        n = min(n, requiredCount)
        val chunkSize = min(1000, n / 5)
        while (n > 0) {
            val count = min(chunkSize, n)
            val recipes = List(count) {
                stream.readRecipe()
            }
            recipeDao.insertAll(recipes)
            n -= count
        }
    }

    private suspend fun readRecipeVectors(stream: InputStream) {
        BufferedInputStream(stream, 64 * 1024).use { buffered ->
            val totalRecipes = stream.readInt32LE()
            val featureCount = stream.readInt32LE()

            var n = totalRecipes
            val chunkSize = 500
            while (n > 0) {
                val count = min(chunkSize, n)
                val recipeVectors = List(count) {
                    RecipeVectorDbModel(
                        id = buffered.readInt32LE(),
                        vector = List(featureCount) { buffered.readFloat32LE() }
                    )
                }
                recipeVectorDao.insertAll(recipeVectors)
                n -= count
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun downloadFileAndProcess(fileUrl: String, onStreamReady: suspend (InputStream) -> Unit) {
        withContext(Dispatchers.IO) {
            val connection = URL(fileUrl).openConnection() as HttpURLConnection
            connection.apply {
                connectTimeout = 50000
                readTimeout = 50000
                requestMethod = "GET"
            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("Server error: ${connection.responseCode}")
            }

            val job = scope.async { onStreamReady(connection.inputStream) }
            job.await()
            connection.inputStream.close()
            connection.disconnect()
        }
    }

    companion object {
        private const val URL_PATTERN = "https://s3.twcstorage.ru/${BuildConfig.BUCKET_NAME}/Foody/%s.foody"
        private val TAGS_FILE_URL = URL_PATTERN.format("tags")
        private val INGREDIENTS_FILE_URL = URL_PATTERN.format("ingredients")
        private val RECIPES_FILE_URL = URL_PATTERN.format("recipes")
        private val RECIPE_VECTORS_URL = URL_PATTERN.format("recipe_vectors")
    }
}