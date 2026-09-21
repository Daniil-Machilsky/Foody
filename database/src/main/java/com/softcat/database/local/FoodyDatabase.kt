package com.softcat.database.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.softcat.database.DatabaseRules
import com.softcat.database.local.dao.IngredientDao
import com.softcat.database.local.dao.RecipeDao
import com.softcat.database.local.dao.RecipeVectorDao
import com.softcat.database.local.dao.ScoreDao
import com.softcat.database.local.dao.TagDao
import com.softcat.database.models.IngredientDbModel
import com.softcat.database.models.RecipeDbModel
import com.softcat.database.models.RecipeVectorDbModel
import com.softcat.database.models.ScoreDbModel
import com.softcat.database.models.TagDbModel
import com.softcat.database.models.VectorConverters

@Database(
    entities = [
        RecipeDbModel::class,
        IngredientDbModel::class,
        TagDbModel::class,
        RecipeVectorDbModel::class,
        ScoreDbModel::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(VectorConverters::class)
abstract class FoodyDatabase : RoomDatabase() {

    abstract fun getRecipeDao(): RecipeDao
    abstract fun getRecipeVectorDao(): RecipeVectorDao
    abstract fun getIngredientDao(): IngredientDao
    abstract fun getScoresDao(): ScoreDao

    abstract fun getTagDao(): TagDao

    companion object {

        private val LOCK = Any()

        @Volatile
        private var INSTANCE: FoodyDatabase? = null

        fun getInstance(context: Context): FoodyDatabase {
            INSTANCE?.let { return it }
            synchronized(LOCK) {
                INSTANCE?.let { return it }
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FoodyDatabase::class.java,
                    DatabaseRules.DATABASE_NAME
                )
                .setJournalMode(JournalMode.TRUNCATE)
                .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}