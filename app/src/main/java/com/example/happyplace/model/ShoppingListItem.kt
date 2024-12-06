package com.example.happyplace.model

import com.example.happyplace.R

//data class ShoppingListItem_old (
//    val name : String,
//    val details : String? = null,
//    val bulk : Boolean = false,
//    val quantity : ItemQuantity? = null,
//    val category : ItemCategory? = null,
//    val shop : Shop? = null,
//    val urgent : Boolean = false,
//    val isInCart: Boolean = false,
//    // val showDetails : Boolean = false  held locally as state
//) {
//    val dateCreated : Long = Date().time
//}

enum class Shop(val nameId:Int) {
    CAGETTE(R.string.cagette),
    MARCHE(R.string.marche),
    BIOCOOP(R.string.biocoop),
    BIGRETAIL(R.string.bigretail),
    BRICOLAGE(R.string.bricolage),
    DADISON(R.string.dadison)
}

enum class ItemCategory(val nameId:Int) {
    FOOD(R.string.food),
    HYGIENE(R.string.hygiene),
    CLEANING(R.string.cleaning),
    APPLIANCE(R.string.appliance),
    DYI(R.string.home_garden_diy)
}

//data class ItemQuantity (
//    val amountNumber : Int,
//    val unit : MeasuringUnit = MeasuringUnit.NONE
//)
//
//enum class MeasuringUnit(val nameSingularStringId:Int,
//                         val namePluralStringId:Int) {
//    NONE(R.string.unit,R.string.units),
//    GRAMS(R.string.gram,R.string.grams),
//    KG(R.string.kg,R.string.kgs),
//    LITER(R.string.liter,R.string.liters),
//}

