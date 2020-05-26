package com.sundaydavid.fastBite.repository

import androidx.lifecycle.LiveData
import com.sundaydavid.fastBite.model.Category
import com.sundaydavid.fastBite.model.CategoryModel

interface LocalRepository {
    fun getMealCategory(): LiveData<CategoryModel>
    suspend fun setMealCategory(meals: CategoryModel)
}