package com.softcat.data.di

import android.app.Application
import com.softcat.database.local.FoodyDatabase
import com.softcat.database.local.dao.IngredientDao
import com.softcat.database.local.dao.RecipeDao
import com.softcat.database.local.dao.RecipeVectorDao
import com.softcat.database.local.dao.ScoreDao
import com.softcat.database.local.dao.TagDao
import dagger.Module
import dagger.Provides

@Module
class DaoModule {

    @ApplicationScope
    @Provides
    fun provideRecipeDao(application: Application): RecipeDao {
        return FoodyDatabase.getInstance(application).getRecipeDao()
    }

    @ApplicationScope
    @Provides
    fun provideIngredientDao(application: Application): IngredientDao {
        return FoodyDatabase.getInstance(application).getIngredientDao()
    }

    @ApplicationScope
    @Provides
    fun provideTagDao(application: Application): TagDao {
        return FoodyDatabase.getInstance(application).getTagDao()
    }

    @ApplicationScope
    @Provides
    fun provideRecipeVectorDao(application: Application): RecipeVectorDao {
        return FoodyDatabase.getInstance(application).getRecipeVectorDao()
    }

    @ApplicationScope
    @Provides
    fun provideScoreDao(application: Application): ScoreDao {
        return FoodyDatabase.getInstance(application).getScoresDao()
    }
}