package com.softcat.database.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.softcat.database.DatabaseRules.RECIPES_TABLE_NAME
import com.softcat.database.models.RecipeDbModel

@Dao
interface RecipeDao {

    @Query("""
        select * from $RECIPES_TABLE_NAME
        where lower(name) LIKE '%' || LOWER(:query) || '%'
        limit :limit
    """)
    suspend fun search(query: String, limit: Int): List<RecipeDbModel>

    @Query("select * from $RECIPES_TABLE_NAME limit :limit")
    suspend fun getSample(limit: Int): List<RecipeDbModel>

    @Query("select id from $RECIPES_TABLE_NAME limit :limit")
    suspend fun getIdSample(limit: Int): List<Int>

    @Query("select * from $RECIPES_TABLE_NAME where id = :recipeId")
    suspend fun get(recipeId: Int): RecipeDbModel?

    @Query("select * from $RECIPES_TABLE_NAME")
    suspend fun getAll(): List<RecipeDbModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<RecipeDbModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: RecipeDbModel)

    @Query("delete from $RECIPES_TABLE_NAME where id = :id")
    suspend fun remove(id: Int)

    @Query("delete from $RECIPES_TABLE_NAME")
    suspend fun clear()

    @Query("select count(*) > 0 from $RECIPES_TABLE_NAME")
    suspend fun isNotEmpty(): Boolean

    @Query("update $RECIPES_TABLE_NAME set cooked = :value where id = :recipeId")
    suspend fun setIsCooked(recipeId: Int, value: Boolean)

    @Query("select cooked from $RECIPES_TABLE_NAME where id = :recipeId")
    suspend fun isCooked(recipeId: Int): Boolean

    @Query("select id from $RECIPES_TABLE_NAME")
    suspend fun getRecipesIds(): List<Int>
}