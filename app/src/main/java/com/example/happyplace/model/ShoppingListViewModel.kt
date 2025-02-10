package com.example.happyplace.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.happyplace.LocalShoppingList
import com.example.happyplace.ShoppingListFilter
import com.example.happyplace.ShoppingListItem
import com.example.happyplace.data.ShoppingListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PopupDisplayState {
    NONE,
    DELETE_ITEM,
    CLEAR_LIST,
    EDIT_ITEM
}

data class ShoppingListUiState(
    val shoppingList: List<ShoppingListItem> = listOf(),
    val popupDisplayState: PopupDisplayState = PopupDisplayState.NONE,
    val showFilterParams: Boolean = false,
    val itemStagedForEdition: ShoppingListItem? = null,
    val shopsList: List<String> = listOf(),
    val categoriesList: List<String> = listOf(),
    // step to take it out of proto and into preferences data store
    val filterParams: ShoppingListFilter = ShoppingListFilter.newBuilder().build()
)

class ShoppingListViewModel(
    private val shoppingListRepository : ShoppingListRepository
) : ViewModel() {

    val initialSetupEvent = liveData {
        emit(shoppingListRepository.fetchInitialShoppingList())
    }

    val shoppingListUiState = shoppingListRepository.shoppingListFlow.asLiveData()

    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    fun toggleItemBought(item: ShoppingListItem) {
        viewModelScope.launch {
            shoppingListRepository.toggleItemBought(item)
        }
    }

    fun showDeleteConfirmationDialog(item: ShoppingListItem) {
        _uiState.update {
            it.copy(
                itemStagedForEdition = item,
                popupDisplayState = PopupDisplayState.DELETE_ITEM,
            )
        }
    }

    fun showClearAllConfirmationDialog() {
        _uiState.update {
            it.copy(
                popupDisplayState = PopupDisplayState.CLEAR_LIST
            )
        }
    }

    fun dismissDeleteConfirmationDialog() {
        _uiState.update {
            it.copy(
                itemStagedForEdition = null,
                popupDisplayState = PopupDisplayState.NONE
            )
        }
    }

    fun deleteStagedItem() {
        deleteItem(_uiState.value.itemStagedForEdition)
    }

    private fun deleteItem(item: ShoppingListItem?) {
        if(item==null)
            return
        viewModelScope.launch {
            shoppingListRepository.deleteItem(item)
            _uiState.update { it.copy(
                itemStagedForEdition = null,
                popupDisplayState = PopupDisplayState.NONE
            ) }
        }
    }

    fun saveItem(itemIndex: Int, newItem: ShoppingListItem) {
        viewModelScope.launch {
            if(itemIndex>=0)
                shoppingListRepository.updateItem(newItem, itemIndex)
            else
                shoppingListRepository.saveNewItem(newItem)

            shoppingListRepository.updateShopsAndCategoriesLists(newItem.shop, newItem.category)
        }
    }

    fun openNewItemDialog() {
        openEditItemDialog(null)
    }
    fun openEditItemDialog(item:ShoppingListItem?) {
        _uiState.update {
            it.copy(
                itemStagedForEdition = item,
                popupDisplayState = PopupDisplayState.EDIT_ITEM
            )
        }
    }
    fun closeEditItemDialog() {
        _uiState.update {
            it.copy(
                popupDisplayState = PopupDisplayState.NONE,
                itemStagedForEdition = null
            )
        }
    }

    fun setShoppingList(retrievedShoppingList : LocalShoppingList) {
        _uiState.update {
            it.copy(
                shoppingList = retrievedShoppingList.itemsList,
                shopsList = retrievedShoppingList.shopsList,
                categoriesList = retrievedShoppingList.categoriesList,
                filterParams = retrievedShoppingList.filter,

                )
        }
    }

    fun deleteAllItems() {
        viewModelScope.launch {
            shoppingListRepository.clearList()
            _uiState.update { it.copy(
                itemStagedForEdition = null,
                popupDisplayState = PopupDisplayState.NONE,
            ) }
        }
    }

    fun showFilterDialog(show:Boolean) {
        _uiState.update { it.copy(
                showFilterParams = show
            )
        }
    }
    fun toggleShowFilterDialog() {
        _uiState.update { currentState ->
            currentState.copy(
                showFilterParams = !(currentState.showFilterParams)
            )
        }
    }

    fun updateFilter(newFilter: ShoppingListFilter) {
        _uiState.update { it.copy(filterParams = newFilter) }
        viewModelScope.launch {
            shoppingListRepository.updateFilter(newFilter)
        }
    }
}

class ShoppingListViewModelFactory(
    private val shoppingListRepository : ShoppingListRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShoppingListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShoppingListViewModel(shoppingListRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}