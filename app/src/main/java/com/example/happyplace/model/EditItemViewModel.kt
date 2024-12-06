package com.example.happyplace.model

import androidx.lifecycle.ViewModel
import com.example.happyplace.ItemQuantity
import com.example.happyplace.ItemQuantity.MeasurementUnit
import com.example.happyplace.ShoppingListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val QUANTITY_MAX_DIGITS = 6
private const val NAME_MAX_CHARS = 20

class EditItemViewModel(item : ShoppingListItem) : ViewModel() {
    private val _uiState = MutableStateFlow(EditItemUiState(item))
    val uiState: StateFlow<EditItemUiState> = _uiState.asStateFlow()

    fun updateName(newName: String) {
        if (newName.length > NAME_MAX_CHARS)
            return

        _uiState.update { currentState ->
            val updatedItem = currentState.itemBeingEdited.toBuilder().setName(newName).build()
            currentState.copy(itemBeingEdited = updatedItem)
        }
    }

    fun updateQuantity(newQuantity: String) {
        if((newQuantity.isNotEmpty() && newQuantity.toIntOrNull()==null)
            || newQuantity.length > QUANTITY_MAX_DIGITS)
            return

        _uiState.update { currentState ->
            val currentUnit = currentState.itemBeingEdited.quantity?.unit ?: MeasurementUnit.UNIT

            val updatedItem = currentState.itemBeingEdited.toBuilder().apply {
                quantity = ItemQuantity.newBuilder().apply {
                    amount = newQuantity.ifEmpty { "0" }.toInt()
                    unit = currentUnit
                }.build()
            }.build()
            currentState.copy(itemBeingEdited = updatedItem)
        }
    }

    fun quantityUnitChosen(newUnit: MeasurementUnit) {
        if (_uiState.value.itemBeingEdited.quantity == null)
            return

        _uiState.update { currentState ->
            val currentAmount = currentState.itemBeingEdited.quantity!!.amount

            val updatedItem = currentState.itemBeingEdited.toBuilder().apply {
                quantity = ItemQuantity.newBuilder().apply {
                    amount = currentAmount
                    unit = newUnit
                }.build()
            }.build()
            currentState.copy(itemBeingEdited = updatedItem)
        }
    }

    fun updateDetails(details: String) {
        _uiState.update { currentState ->
            val updatedItem = currentState.itemBeingEdited
                .toBuilder()
                .setDetails(details)
                .build()
            currentState.copy(itemBeingEdited = updatedItem)
        }
    }
    fun setItemAsUrgent(urgent: Boolean) {
        _uiState.update { currentState ->
            val updatedItem = currentState.itemBeingEdited
                .toBuilder()
                .setUrgent(urgent)
                .build()
            currentState.copy(itemBeingEdited = updatedItem)
        }
    }

    fun setItemAsBulk(isBulk: Boolean) {
        _uiState.update { currentState ->
            val updatedItem = currentState.itemBeingEdited
                .toBuilder()
                .setBulk(isBulk)
                .build()
            currentState.copy(itemBeingEdited = updatedItem)
        }
    }

//    fun shopChosen(shop: Shop?) {
//        toggleShopDropDownOpen(false)
//        _uiState.update { currentState ->
//            val updatedItem = currentState.itemBeingEdited.copy(
//                shop = shop
//            )
//            currentState.copy(itemBeingEdited = updatedItem)
//        }
//    }
//
//    fun categoryChosen(category: ItemCategory?) {
//        toggleCategoryDropDownOpen(false)
//        _uiState.update { currentState ->
//            val updatedItem = currentState.itemBeingEdited.copy(category = category)
//            currentState.copy(itemBeingEdited = updatedItem)
//        }
//    }

    fun toggleUnitDropDownOpen(open: Boolean) {
        _uiState.update { it.copy(unitDropDownExpanded = open) }
    }
    fun toggleUnitDropDownOpen() {
        toggleUnitDropDownOpen(!_uiState.value.unitDropDownExpanded)
    }

    fun toggleShopDropDownOpen(open:Boolean) {
        _uiState.update { it.copy(shopDropDownExpanded = open) }
    }
    fun toggleShopDropDownOpen() {
        toggleShopDropDownOpen(!_uiState.value.shopDropDownExpanded)
    }

    fun toggleCategoryDropDownOpen(open:Boolean) {
        _uiState.update { it.copy(categoryDropDownExpanded = open) }
    }
    fun toggleCategoryDropDownOpen() {
        toggleCategoryDropDownOpen(!_uiState.value.categoryDropDownExpanded)
    }
}

data class EditItemUiState(
    val itemBeingEdited : ShoppingListItem,
    val unitDropDownExpanded : Boolean = false,
    val shopDropDownExpanded: Boolean = false,
    val categoryDropDownExpanded: Boolean = false,
)