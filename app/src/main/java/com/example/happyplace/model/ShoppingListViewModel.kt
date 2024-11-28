package com.example.happyplace.model

import androidx.lifecycle.ViewModel
import com.example.happyplace.data.LocalShoppingListDataProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ShoppingListViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        ShoppingListUiState(
            shoppingList = LocalShoppingListDataProvider
                .getShoppingList()
                .sortedBy { item -> if(item.isInCart) 1 else 0 }
        )
    )
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    fun toggleExpandItem(item: ShoppingListItem) {
        val list: MutableList<ShoppingListItem> = _uiState.value.shoppingList.toMutableList()

        val expanded = item.showDetails
        val index = list.indexOf(item)
        if (list.remove(item)) {
            list.add(index, item.copy(showDetails = !expanded))

            _uiState.update {
                it.copy(
                    shoppingList = list,
                    itemStagedForEdition = null
                    )
            }
        }
    }

    fun toggleItemBought(item: ShoppingListItem) {
        val list: MutableList<ShoppingListItem> = _uiState.value.shoppingList.toMutableList()

        val inCart = item.isInCart
        if (list.remove(item)) {
            // top of list of moving out of the cart, otherwise on top of items in cart
            val index = if(inCart) 0 else list.filter{ !it.isInCart }.size

            list.add(index, item.copy(isInCart = !inCart))

            _uiState.update {
                it.copy(shoppingList = list)
            }
        }
    }

    fun openNewItemDialog() {
        openEditItemDialog(null)
    }

    fun openEditItemDialog(item:ShoppingListItem?) {
        stageItem(item)
        _uiState.update {
            it.copy(
                showEditItemDialog = true
            )
        }
    }

    fun closeEditItemDialog() {
        _uiState.update {
            it.copy(showEditItemDialog = false)
        }
    }

    fun deleteStagedItem() {
        deleteItem(_uiState.value.itemStagedForEdition)
    }

    private fun deleteItem(item: ShoppingListItem?) {
        if(item==null)
            return

        val list: MutableList<ShoppingListItem> = _uiState.value.shoppingList.toMutableList()

        if(list.remove(item)) {
            _uiState.update {
                it.copy(shoppingList = list)
            }
        }
    }

    fun stageItem(item: ShoppingListItem?) {
        _uiState.update {
            it.copy(itemStagedForEdition = item)
        }
    }

    fun saveNewItem(newItem: ShoppingListItem) {
        val updatedList: MutableList<ShoppingListItem> = _uiState.value.shoppingList.toMutableList()
        val index = updatedList.indexOf(_uiState.value.itemStagedForEdition)

        if(index>=0)
            updatedList.removeAt(index)

        updatedList.add(
            index = if(index<0) 0 else index,
            element = newItem
        )

        _uiState.update {
            it.copy(shoppingList = updatedList)
        }
    }
}

data class ShoppingListUiState(
    val shoppingList : List<ShoppingListItem> = listOf(),
    val showEditItemDialog : Boolean = false,
    val itemStagedForEdition : ShoppingListItem? = null
)
