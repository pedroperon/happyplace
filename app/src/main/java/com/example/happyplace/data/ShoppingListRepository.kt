package com.example.happyplace.data

import android.util.Log
import androidx.datastore.core.DataStore
import com.example.happyplace.LocalShoppingList
import com.example.happyplace.ShoppingListFilter
import com.example.happyplace.ShoppingListItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException

class ShoppingListRepository (private val shoppingListStore: DataStore<LocalShoppingList>) {

    val shoppingListFlow: Flow<LocalShoppingList> = shoppingListStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Log.e("ShoppingListRepo",
                    "Error reading shopping list.", exception)
                emit(LocalShoppingList.getDefaultInstance())
            } else {
                throw exception
            }
        }

    suspend fun fetchInitialShoppingList() = shoppingListStore.data.first()

    suspend fun toggleItemBought(item: ShoppingListItem) {
        shoppingListStore.updateData { currentData ->
            val index = currentData.itemsList.indexOf(item)
            if(index<0)
                currentData //item not found in list; does not update
            else {
                val isInCart = item.isInCart

                // new position: on TOP if moving OUT of the cart,
                //               on TOP OF CART if moving INTO the cart
                var newIndex =
                    if(isInCart) 0
                    else currentData.itemsList.filter{!it.isInCart}.size

                if(newIndex>index) // account for to-be-removed item at index
                    newIndex--

                currentData
                    .toBuilder()
                    .removeItems(index)
                    .addItems(
                        newIndex,
                        item.toBuilder().setIsInCart(!isInCart).build()
                    )
                    .build()
            }
        }
    }

    suspend fun deleteItem(item: ShoppingListItem) {
        shoppingListStore.updateData { currentData ->
            val index = currentData.itemsList.indexOf(item)
            if(index<0)
                currentData
            else
                currentData.toBuilder().removeItems(index).build()
        }
    }

    suspend fun saveNewItem(newItem: ShoppingListItem) {
        shoppingListStore.updateData { currentData ->
            currentData
                .toBuilder()
                .addItems(
                    0, // new items added to top of list
                    newItem.toBuilder().setDateCreated(System.currentTimeMillis()))
                .build()
        }
    }

    suspend fun updateItem(item: ShoppingListItem, itemIndex: Int) {
        shoppingListStore.updateData {
            it.toBuilder()
                .setItems(itemIndex,item)
                .build()
        }
    }

    suspend fun updateShopsAndCategoriesLists(shopName: String, categoryName: String) {
        val trimmedCategoryName = categoryName.trim()
        val trimmedShopName = shopName.trim()

        shoppingListStore.updateData { currentData ->
            currentData.toBuilder().apply {
                if (trimmedCategoryName.isNotEmpty() &&
                    !currentData.categoriesList.contains(trimmedCategoryName)) {
                    addCategories(trimmedCategoryName)
                }
                if (trimmedShopName.isNotEmpty() &&
                    !currentData.shopsList.contains(trimmedShopName)) {
                    addShops(trimmedShopName)
                }
            }.build()
        }
    }

    suspend fun clearList() {
        shoppingListStore.updateData { currentData ->
            currentData.toBuilder().clearItems().build()
        }
    }

    suspend fun updateFilter(newFilter: ShoppingListFilter) {
        shoppingListStore.updateData {
            it.toBuilder().setFilter(newFilter).build()
        }
    }
}