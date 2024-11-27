package com.example.happyplace.model

import com.example.happyplace.R
import java.util.Date

data class ShoppingListItem (
    val name : String,
    val details : String? = "",
    val bulk : Boolean = false,
    val quantity : ItemQuantity? = null,
    val category : ItemCategory? = null,
    val shop : Shop? = null,
    val urgent : Boolean = false,
    val isInCart: Boolean = false,
    val showDetails : Boolean = false
) {
    val dateCreated : Date = Date()
}

enum class Shop(val shopNameId:Int) {
    CAGETTE(R.string.cagette),
    MARCHE(R.string.marche),
    BIOCOOP(R.string.biocoop),
    BIGRETAIL(R.string.bigretail),
    BRICOLAGE(R.string.bricolage),
    DADISON(R.string.dadison)
}

enum class ItemCategory(val nameResId:Int) {
    FOOD(R.string.food),
    HYGIENE(R.string.hygiene),
    CLEANING(R.string.cleaning),
    APPLIANCE(R.string.appliance),
    DYI(R.string.home_garden_diy)
}

data class ItemQuantity (
    val amountNumber : Int,
    val unit : MeasuringUnit = MeasuringUnit.NONE
)

enum class MeasuringUnit(val nameSingularStringId:Int,
                         val namePluralStringId:Int) {
    NONE(R.string.unit,R.string.units),
    GRAMS(R.string.gram,R.string.grams),
    KG(R.string.kg,R.string.kgs),
    LITER(R.string.liter,R.string.liters),
}

