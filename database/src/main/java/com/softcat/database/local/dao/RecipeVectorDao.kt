package com.softcat.database.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.softcat.database.DatabaseRules.RECIPE_VECTORS_TABLE_NAME
import com.softcat.database.models.RecipeVectorDbModel

@Dao
interface RecipeVectorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: RecipeVectorDbModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<RecipeVectorDbModel>)

    @Query("select * from $RECIPE_VECTORS_TABLE_NAME")
    suspend fun getAll(): List<RecipeVectorDbModel>

    @Query("select * from $RECIPE_VECTORS_TABLE_NAME where id in (:recipeIds)")
    suspend fun getByIds(recipeIds: List<Int>): List<RecipeVectorDbModel>

    @Query("select * from $RECIPE_VECTORS_TABLE_NAME where id = :id")
    suspend fun getVectorById(id: Int): RecipeVectorDbModel?

    @Query("delete from $RECIPE_VECTORS_TABLE_NAME")
    suspend fun clear()
}