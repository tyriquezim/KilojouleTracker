package com.android.personal.kilojouletracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Meal
{
    @PrimaryKey var mealId: Int = totalMealsCreated
    lateinit var mealName: String
    lateinit var mealType: String
    var servingSize = 0.0
    var numKilojoules = 0.0
    var fatContent = 0.0
    var carbohydrateContent = 0.0
    var proteinContent = 0.0

    constructor(mealName: String, mealType: String, servingSize: Double, numKilojoules: Double, fatContent: Double, carbohydrateContent: Double, proteinContent: Double)
    {
        this.mealName = mealName
        this.mealType = mealType
        this.servingSize = servingSize
        this.numKilojoules = numKilojoules
        this.fatContent = fatContent
        this.carbohydrateContent = carbohydrateContent
        this.proteinContent = proteinContent

        ++totalMealsCreated
    }

    companion object
    {
        var totalMealsCreated: Int = 0
    }
}