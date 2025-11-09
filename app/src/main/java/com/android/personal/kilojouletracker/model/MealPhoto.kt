package com.android.personal.kilojouletracker.model

import androidx.room.Entity

@Entity
class MealPhoto(val mealOwnerId: String, val bitmapByteArray: ByteArray)
{
}