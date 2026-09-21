package com.softcat.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.softcat.database.DatabaseRules

@Entity(tableName = DatabaseRules.RECIPES_TABLE_NAME)
data class RecipeDbModel(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "steps")
    val steps: String,

    @ColumnInfo(name = "ingredients")
    val ingredients: String,

    @ColumnInfo(name = "tags")
    val tags: String,

    @ColumnInfo(name = "cooked")
    val isCooked: Boolean,

    @ColumnInfo(name = "minutes")
    val minutes: Int,

    @ColumnInfo(name = "calories")
    val calories: Float,

    @ColumnInfo(name = "fat")
    val fat: Float,

    @ColumnInfo(name = "protein")
    val protein: Float,

    @ColumnInfo(name = "carbohydrates")
    val carbohydrates: Float,

    @ColumnInfo(name = "ingredientUnits")
    val ingredientUnits: String,

    @ColumnInfo(name = "ingredientQuantities")
    val ingredientQuantities: String,

    @ColumnInfo(name = "imageUrl")
    val imageUrl: String,

    @ColumnInfo(name = "stepImages")
    val stepImages: String,

    @ColumnInfo(name = "avgScore")
    val avgScore: Float
)