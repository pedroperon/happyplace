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

data class EditItemUiState(
    val itemBeingEdited : ShoppingListItem,
    val unitDropDownExpanded : Boolean = false,
    val shopDropDownExpanded: Boolean = false,
    val categoryDropDownExpanded: Boolean = false,
)

class EditItemViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        EditItemUiState(getNewBlankShoppingListItem())
    )
    val uiState: StateFlow<EditItemUiState> = _uiState.asStateFlow()

    fun setItemBeingEdited(originalItem: ShoppingListItem?) {
        _uiState.update {
            it.copy(itemBeingEdited = originalItem ?: getNewBlankShoppingListItem())
        }
    }

    fun updateName(newName: String) {
        if (newName.length > NAME_MAX_CHARS)
            return

        _uiState.update { currentState ->
            currentState.copy(
                itemBeingEdited = currentState.itemBeingEdited
                    .toBuilder()
                    .setName(newName)
                    .build())
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
            currentState.copy(itemBeingEdited = currentState.itemBeingEdited
                .toBuilder()
                .setDetails(details)
                .build())
        }
    }
    fun setItemAsUrgent(urgent: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(itemBeingEdited = currentState.itemBeingEdited
                .toBuilder()
                .setUrgent(urgent)
                .build())
        }
    }

    fun setItemAsBulk(isBulk: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                itemBeingEdited = currentState.itemBeingEdited
                    .toBuilder()
                    .setBulk(isBulk)
                    .build()
            )
        }
    }

    fun shopChosen(shop: String) {
        _uiState.update { currentState ->
            currentState.copy(
                itemBeingEdited = currentState.itemBeingEdited
                    .toBuilder()
                    .setShop(shop.trim())
                    .build()
            )
        }
    }

    fun categoryChosen(category: String) {
        _uiState.update { currentState ->
            currentState.copy(
                itemBeingEdited = currentState.itemBeingEdited
                    .toBuilder()
                    .setCategory(category.trim())
                    .build()
            )
        }
    }

    fun toggleUnitDropDownOpen(open: Boolean? = null) {
        _uiState.update { currentState ->
            currentState.copy(unitDropDownExpanded = open ?: !currentState.unitDropDownExpanded)
        }
    }

    fun toggleShopDropDownOpen(open:Boolean? = null) {
        _uiState.update { currentState ->
            currentState.copy(shopDropDownExpanded = open ?: !currentState.shopDropDownExpanded)
        }
    }
    fun toggleCategoryDropDownOpen(open:Boolean? = null) {
        _uiState.update { currentState ->
            currentState.copy(categoryDropDownExpanded = open ?: !currentState.categoryDropDownExpanded)
        }
    }
}

private fun getNewBlankShoppingListItem(): ShoppingListItem {
    return ShoppingListItem.newBuilder().setDateCreated(System.currentTimeMillis()).build()
}
