package com.example.recommender.di

import com.example.recommender.implementations.RecommendationManagerImpl
import com.example.recommender.implementations.ingredientMatcher.IngredientMatcher
import com.example.recommender.implementations.ingredientMatcher.IngredientMatcherImpl
import com.example.recommender.interfaces.RecommendationManager
import dagger.Binds
import dagger.Module

@Module
interface RecommenderModule {

    @Binds
    fun bindRecommendationManager(impl: RecommendationManagerImpl): RecommendationManager

    @Binds
    fun bindIngredientMatcher(impl: IngredientMatcherImpl): IngredientMatcher
}