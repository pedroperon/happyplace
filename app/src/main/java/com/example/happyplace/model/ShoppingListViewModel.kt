package com.example.happyplace.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.happyplace.LocalShoppingList
import com.example.happyplace.ShoppingListItem
import com.example.happyplace.data.ShoppingListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShoppingListUiState(
    val shoppingList: List<ShoppingListItem> = listOf(),
    val showEditItemDialog: Boolean = false,
    val itemStagedForEdition: ShoppingListItem? = null
)

class ShoppingListViewModel(
    private val shoppingListRepository : ShoppingListRepository,
) : ViewModel() {

    val initialSetupEvent = liveData {
        emit(shoppingListRepository.fetchInitialShoppingList())
    }

    private val shoppingListFlow = shoppingListRepository.shoppingListFlow

    private val shoppingListUiStateFlow = combine(
        shoppingListFlow,
        flowOf(1..3)
    ) { shoppingList: LocalShoppingList, _ ->
        return@combine ShoppingListUiState(
            shoppingList = shoppingList.itemsList
        )
    }
    val shoppingListUiState = shoppingListUiStateFlow.asLiveData()

    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    fun toggleItemBought(item: ShoppingListItem) {
        viewModelScope.launch {
            shoppingListRepository.toggleItemBought(item)
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
            _uiState.update { it.copy(itemStagedForEdition = null) }
        }
    }

    fun stageItem(item: ShoppingListItem?) {
        _uiState.update {
            it.copy(itemStagedForEdition = item)
        }
    }

    fun saveItem(itemIndex: Int, newItem: ShoppingListItem) {
        viewModelScope.launch {
            if(itemIndex>=0)
                shoppingListRepository.updateItem(newItem, itemIndex)
            else
                shoppingListRepository.saveNewItem(newItem)
        }
    }

    fun openNewItemDialog() {
        openEditItemDialog(null)
    }

    fun openEditItemDialog(item:ShoppingListItem?) {
        stageItem(item)
        _uiState.update {
            it.copy(showEditItemDialog = true)
        }
    }

    fun closeEditItemDialog() {
        _uiState.update {
            it.copy(showEditItemDialog = false)
        }
    }

    fun setShoppingList(itemsList: List<ShoppingListItem>) {  //SERÁ???
        _uiState.update {
            it.copy(shoppingList = itemsList)
        }
    }
}

