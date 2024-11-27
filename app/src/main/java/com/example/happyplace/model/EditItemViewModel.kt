package com.example.happyplace.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val QUANTITY_MAX_DIGITS = 6
private const val NAME_MAX_CHARS = 20

class EditItemViewModel(item : ShoppingListItem) : ViewModel() {
    private val _uiState = MutableStateFlow( EditItemUiState(item) )
    val uiState: StateFlow<EditItemUiState> = _uiState.asStateFlow()

    fun updateName(newName: String) {
        if(newName.length> NAME_MAX_CHARS)
            return

        _uiState.update { currentState ->
            val updatedItem = currentState.itemBeingEdited.copy(name = newName)
            currentState.copy(itemBeingEdited = updatedItem)
        }
    }

    fun updateQuantity(newQuantity: String) {
        if(newQuantity.length>QUANTITY_MAX_DIGITS)
            return

        _uiState.update { currentState ->
            val currentUnit = currentState.itemBeingEdited.quantity?.unit ?: MeasuringUnit.NONE

            val updatedItem = currentState.itemBeingEdited.copy(
                quantity = if(newQuantity.isEmpty()) null else ItemQuantity(newQuantity.toInt(), currentUnit)
            )
            currentState.copy(itemBeingEdited = updatedItem)
        }
    }
    fun chosenQuantityUnit(newUnit: MeasuringUnit) {
        if(_uiState.value.itemBeingEdited.quantity==null)
            return

        _uiState.update { currentState ->
            val currentAmount = currentState.itemBeingEdited.quantity!!.amountNumber

            val updatedItem = currentState.itemBeingEdited.copy(
                quantity = ItemQuantity(currentAmount, newUnit)
            )
            currentState.copy(itemBeingEdited = updatedItem)
        }
    }

    fun updateDetails(details: String) {
        _uiState.update { currentState ->
            val updatedItem = currentState.itemBeingEdited.copy(details = details.ifEmpty { null })
            currentState.copy(itemBeingEdited = updatedItem)
        }
    }

    fun toggleUnitDropDownOpen(open:Boolean) {
        _uiState.update { it.copy(unitDropDownExpanded = open) }
    }
    fun toggleUnitDropDownOpen() {
        toggleUnitDropDownOpen(!_uiState.value.unitDropDownExpanded)
    }

    fun setItemAsUrgent(urgent: Boolean) {
        _uiState.update { currentState ->
            val updatedItem = currentState.itemBeingEdited.copy(urgent = urgent)
            currentState.copy(itemBeingEdited = updatedItem)
        }
    }
    fun setItemAsBulk(isBulk: Boolean) {
        _uiState.update { currentState ->
            val updatedItem = currentState.itemBeingEdited.copy(bulk = isBulk)
            currentState.copy(itemBeingEdited = updatedItem)
        }
    }
}

data class EditItemUiState(
    val itemBeingEdited : ShoppingListItem = ShoppingListItem(""),
    val unitDropDownExpanded : Boolean = false,
)