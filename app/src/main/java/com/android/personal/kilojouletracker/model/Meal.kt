package com.android.personal.kilojouletracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity
class Meal
{
    @PrimaryKey var mealId: Int = totalMealsCreated
    var mealName: String
    var servingWeight = 0.0
    var numKilojoules = 0.0
    var fatWeight = 0.0
    var carbohydrateWeight = 0.0
    var proteinWeight = 0.0

    constructor(mealName: String, servingWeight: Double, numKilojoules: Double, fatWeight: Double, carbohydrateWeight: Double, proteinWeight: Double)
    {
        this.mealName = mealName
        this.servingWeight = servingWeight
        this.numKilojoules = numKilojoules
        this.fatWeight = fatWeight
        this.carbohydrateWeight = carbohydrateWeight
        this.proteinWeight = proteinWeight

        ++totalMealsCreated
    }

    companion object
    {
        var totalMealsCreated: Int = 0
    }
}