package com.example.happyplace.data

import com.example.happyplace.LocalShoppingList
import com.example.happyplace.ShoppingListItem
import com.example.happyplace.ItemQuantity
import com.example.happyplace.ItemQuantity.MeasurementUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

object LocalShoppingListDataProvider {

    val shoppingList = flowOf(
        LocalShoppingList.newBuilder().addAllItems(
            listOf(
                ShoppingListItem.newBuilder().apply {
                    name = "Arroz"
                    details = "semi-complet"
                }.build(),
                ShoppingListItem.newBuilder().apply {
                    name = "Café"
                    bulk = true
//                    category = ItemCategory.FOOD
//                    shop = Shop.CAGETTE
                    isInCart = true
                }.build(),
                ShoppingListItem.newBuilder().apply {
                    name = "Fruits"
                    details = "petit dèj"
//                    category = ItemCategory.FOOD
//                    shop = Shop.MARCHE
                }.build(),
                ShoppingListItem.newBuilder().apply {
                    name = "Vinaigre"
                    details = "vrac, ménage"
                    bulk = true
                    isInCart = true
                    quantity = ItemQuantity.newBuilder().apply {
                        amount = 1
                        unit = MeasurementUnit.LITER
                    }.build()
//                    category = ItemCategory.CLEANING,
//                    shop = Shop.BIOCOOP,
                }.build(),
                ShoppingListItem.newBuilder().apply {
                    name = "Déodorant"
                    details = "Lavande, Bamboo"
                    bulk = true
                    quantity = ItemQuantity.newBuilder().apply {
                        amount = 2
                        unit = MeasurementUnit.UNIT
                    }.build()
//                    category = ItemCategory.HYGIENE
//                    shop = Shop.BIOCOOP
                }.build(),
                ShoppingListItem.newBuilder().apply {
                    name = "Sel fin"
                    details = "Cisne de préférence"
                    quantity = ItemQuantity.newBuilder().apply {
                        amount = 500
                        unit = MeasurementUnit.GRAM
                    }.build()
//                    category = ItemCategory.FOOD
//                    shop = Shop.BIGRETAIL
                }.build(),
                ShoppingListItem.newBuilder().apply {
                    name = "Haricots noirs"
                    quantity = ItemQuantity.newBuilder().apply {
                        amount = 2
                        unit = MeasurementUnit.KG
                    }.build()
//                    category = ItemCategory.FOOD
//                    shop = Shop.DADISON
                }.build(),
                ShoppingListItem.newBuilder().apply {
                    name = "Acide citrique"
//                    category = ItemCategory.CLEANING
//                    shop = Shop.BRICOLAGE
                }.build(),

            )
//        )
//        listOf(
//            ShoppingListItem(
//                name = "Arroz",
//                details = "semi-complet"
//            ),
//            ShoppingListItem(
//                name = "Café",
//                bulk = true,
//                category = ItemCategory.FOOD,
//                shop = Shop.CAGETTE,
//                isInCart = true
//            ),
//            ShoppingListItem(
//                name = "Fruits",
//                details = "petit dèj",
//                category = ItemCategory.FOOD,
//                shop = Shop.MARCHE
//            ),
//            ShoppingListItem(
//                name = "Vinaigre",
//                details = "vrac, ménage",
//                quantity = ItemQuantity(1, MeasuringUnit.LITER),
//                bulk = true,
//                category = ItemCategory.CLEANING,
//                shop = Shop.BIOCOOP,
//                isInCart = true
//            ),
//            ShoppingListItem(
//                name = "Déodorant",
//                details = "Lavande, Bamboo",
//                bulk = true,
//                quantity = ItemQuantity(2),
//                category = ItemCategory.HYGIENE,
//                shop = Shop.BIOCOOP
//            ),
//            ShoppingListItem(
//                name = "Sel fin",
//                details = "Cisne de préférence",
//                quantity = ItemQuantity(1, MeasuringUnit.KG),
//                category = ItemCategory.FOOD,
//                shop = Shop.BIGRETAIL
//            ),
//            ShoppingListItem(
//                name = "Haricots noirs",
//                quantity = ItemQuantity(500, MeasuringUnit.GRAMS),
//                category = ItemCategory.FOOD,
//                shop = Shop.DADISON
//            ),
//            ShoppingListItem(
//                name = "Acide citrique",
//                category = ItemCategory.CLEANING,
//                shop = Shop.BRICOLAGE
//            ),
        )
    )
}

    /* TODO: implement local persistence / rertrieval */
    private fun getLocallyPersistedShoppingList(): Flow<ShoppingListItem>? {
        return null
    }

