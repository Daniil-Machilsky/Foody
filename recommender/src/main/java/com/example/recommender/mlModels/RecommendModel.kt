package com.example.recommender.mlModels

import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.ndarray.data.D1Array
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.data.set
import org.jetbrains.kotlinx.multik.ndarray.operations.div
import org.jetbrains.kotlinx.multik.ndarray.operations.minus
import org.jetbrains.kotlinx.multik.ndarray.operations.plus
import org.jetbrains.kotlinx.multik.ndarray.operations.sum
import org.jetbrains.kotlinx.multik.ndarray.operations.times
import kotlin.math.sqrt

class RecommendModel(
    private val profile: D2Array<Float>,
    private val regression: Regression
) {

    fun predict(recipes: D2Array<Float>): D2Array<Float> {
        val features = calculateMetrics(profile, recipes)
        return regression.predict(features)
    }

    companion object {
        fun learn(scoredRecipes: D2Array<Float>, scores: D1Array<Float>): RecommendModel {
            val profile = buildProfile(scoredRecipes, scores)
            val features = calculateMetrics(profile, scoredRecipes)
            val regression = RidgeModel.learn(features, scores)
            return RecommendModel(profile, regression)
        }

        private fun buildProfile(recipeMatrix: D2Array<Float>, scores: D1Array<Float>): D2Array<Float> {
            val m = recipeMatrix.shape[1]
            val profile = mk.zeros<Float>(5, m)
            val counts = FloatArray(5) { 1e-6f }

            for (i in 0 until scores.size) {
                val scoreIdx = scores[i].toInt() - 1
                counts[scoreIdx] += 1f
                profile[scoreIdx] = profile[scoreIdx] + recipeMatrix[i]
            }

            for (i in 0 until 5) {
                val divisor = counts[i]
                profile[i] = profile[i] / divisor
            }
            return profile
        }

        private fun calculateMetrics(
            profile: D2Array<Float>,
            recipeMatrix: D2Array<Float>
        ): D2Array<Float> {
            val n = recipeMatrix.shape[0]
            val result = mk.zeros<Float>(n, 5)

            for (i in 0 until n) {
                val recipeVector = recipeMatrix[i]
                for (j in 0 until 5) {
                    val diff = recipeVector - profile[j]
                    result[i, j] = sqrt((diff * diff).sum())
                }
            }
            return result
        }
    }
}