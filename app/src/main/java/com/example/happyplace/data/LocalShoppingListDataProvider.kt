package com.example.happyplace.data

import com.example.happyplace.model.ItemCategory
import com.example.happyplace.model.ItemQuantity
import com.example.happyplace.model.MeasuringUnit
import com.example.happyplace.model.Shop
import com.example.happyplace.model.ShoppingListItem

object LocalShoppingListDataProvider {

    fun getShoppingList() : List<ShoppingListItem> {
        return getLocallyPersistedShoppingList() ?: listOf(
            ShoppingListItem(
                name = "Arroz",
                details = "semi-complet"
            ),
            ShoppingListItem(
                name = "Café",
                bulk = true,
                category = ItemCategory.FOOD,
                shop = Shop.CAGETTE,
                isInCart = true
            ),
            ShoppingListItem(
                name = "Fruits",
                details = "petit dèj",
                category = ItemCategory.FOOD,
                shop = Shop.MARCHE
            ),
            ShoppingListItem(
                name = "Vinaigre",
                details = "vrac, ménage",
                quantity = ItemQuantity(1, MeasuringUnit.LITER),
                bulk = true,
                category = ItemCategory.CLEANING,
                shop = Shop.BIOCOOP,
                isInCart = true
            ),
            ShoppingListItem(
                name = "Déodorant",
                details = "Lavande, Bamboo",
                bulk = true,
                quantity = ItemQuantity(2),
                category = ItemCategory.HYGIENE,
                shop = Shop.BIOCOOP
            ),
            ShoppingListItem(
                name = "Sel fin",
                details = "Cisne de préférence",
                quantity = ItemQuantity(1, MeasuringUnit.KG),
                category = ItemCategory.FOOD,
                shop = Shop.BIGRETAIL
            ),
            ShoppingListItem(
                name = "Haricots noirs",
                quantity = ItemQuantity(500, MeasuringUnit.GRAMS),
                category = ItemCategory.FOOD,
                shop = Shop.DADISON
            ),
            ShoppingListItem(
                name = "Acide citrique",
                category = ItemCategory.CLEANING,
                shop = Shop.BRICOLAGE
            ),
        )
    }

    /* TODO: implement local persistence / rertrieval */
    private fun getLocallyPersistedShoppingList(): List<ShoppingListItem>? {
        return null
    }
}
