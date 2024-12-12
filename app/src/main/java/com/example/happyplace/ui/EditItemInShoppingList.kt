package com.example.happyplace.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.happyplace.ItemQuantity.MeasurementUnit
import com.example.happyplace.R
import com.example.happyplace.ShoppingListItem
import com.example.happyplace.model.EditItemUiState
import com.example.happyplace.model.EditItemViewModel
import com.example.happyplace.model.ShoppingListUiState

@Composable
fun EditItemInShoppingListDialog(
    onDismissRequest: () -> Unit,
    onDone: (ShoppingListItem) -> Unit,
    shops: List<String>,
    categories: List<String>,
    originalItem: ShoppingListItem?,
) {
    val viewModel = EditItemViewModel(originalItem)
    val editItemUiState by viewModel.uiState.collectAsState()

    val isNewItem = originalItem?.name.isNullOrEmpty()

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = true
        )
    ) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(if(isNewItem) R.string.add_item else R.string.edit_item),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
                OutlinedTextField(
                    //NAME
                    value = editItemUiState.itemBeingEdited.name,
                    onValueChange = { viewModel.updateName(it) },
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    label = { Text(text = stringResource(R.string.name)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                // QUANTITY + UNIT
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField( // QUANTITY
                        value = toStringEmptyIfZero(editItemUiState.itemBeingEdited.quantity.amount),
                        onValueChange = { viewModel.updateQuantity(it) },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next,
                        ),
                        keyboardActions = KeyboardActions(),
                        label = { Text(text = stringResource(R.string.quantity)) },
                        modifier = Modifier.fillMaxWidth(0.35F)
                    )
                    // UNIT
                    UnitDropdownMenu(
                        viewModel,
                        editItemUiState,
                        modifier = Modifier
                            .fillMaxWidth(0.5F)
                            .padding(8.dp))
                    // BULK?
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = editItemUiState.itemBeingEdited.bulk,
                            onCheckedChange = { viewModel.setItemAsBulk(it) })
                        Text(text = "Bulk")
                    }
                }
                // DETAILS
                OutlinedTextField(
                    value = editItemUiState.itemBeingEdited.details ?: "",
                    onValueChange = { viewModel.updateDetails(it) },
                    label = { Text(text = stringResource(R.string.details)) },
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done,
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                // URGENT
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = editItemUiState.itemBeingEdited.urgent,
                        onCheckedChange = { viewModel.setItemAsUrgent(it) })
                    Text(text = "Urgent")
                }

                // CATEGORY
                OptionsDropdownMenu(
                    title = stringResource(R.string.category),
                    options = categories,
                    onChooseOption = { viewModel.categoryChosen(it) },
                    currentOptionName = editItemUiState.itemBeingEdited.category,
                    expanded = editItemUiState.categoryDropDownExpanded,
                    onToggleExpanded = { viewModel.toggleCategoryDropDownOpen(it) }
                )

                // SHOP
                OptionsDropdownMenu(
                    title = stringResource(R.string.shop),
                    options = shops,
                    onChooseOption = { viewModel.shopChosen(it) },
                    currentOptionName = editItemUiState.itemBeingEdited.shop,
                    expanded = editItemUiState.shopDropDownExpanded,
                    onToggleExpanded = { viewModel.toggleShopDropDownOpen(it) }
                )
                
                // BUTTONS CANCEL / DONE
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.weight(1.0F))
                    Text(text = stringResource(R.string.cancel),
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clickable { onDismissRequest() })
                    Button(
                        onClick = {
                            onDone(editItemUiState.itemBeingEdited)
                            onDismissRequest()
                        },
                        enabled = editItemUiState.itemBeingEdited.name.isNotEmpty()
                    ) {
                        Text(text = stringResource(R.string.done))
                    }
                }
            }
        }
    }
}

fun toStringEmptyIfZero(amount: Number): String {
    return if(amount==0) ""
    else "$amount"
}

@Composable
fun OptionsDropdownMenu(
    title: String,
    options: List<String>,
    currentOptionName: String?,
    onChooseOption: (String?)->Unit,
    expanded: Boolean,
    onToggleExpanded: (Boolean?)->Unit,
    modifier: Modifier = Modifier
    ) {
    var newOptionName by rememberSaveable { mutableStateOf("") }

    val dismiss = {
        onToggleExpanded(false)
        newOptionName = ""
    }

    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.clickable(onClick = {onToggleExpanded(null)})
        ) {
            Text(text = title)
            Text(text = currentOptionName ?: stringResource(R.string.none))
            Image(
                painter = painterResource(R.drawable.baseline_arrow_right_24),
                contentDescription = stringResource(R.string.expand_list),
                modifier = Modifier.rotate(if (expanded) 270F else 90F)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = dismiss
        ) {
            DropdownMenuItem(
                text = {
                    Text(text = stringResource(R.string.none))
                },
                onClick = {
                    onChooseOption(null)
                }
            )
            options.forEach {
                DropdownMenuItem(
                    text = {
                        Text(text = it)
                    },
                    onClick = {
                        onChooseOption(it)
                        dismiss()
                    }
                )
            }
            DropdownMenuItem(
                onClick = {},
                text = {
                    TextField( // add new shop
                        value = newOptionName,
                        onValueChange = { newOptionName = it },
                        //shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done,
                            capitalization = KeyboardCapitalization.Words
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                onChooseOption(newOptionName)
                                dismiss() //onToggleExpanded(false)
                            }
                        ),
                        label = { Text(text = stringResource(R.string.create_new)) },
                        //modifier = Modifier.fillMaxWidth(0.5F)
                    )
                }
            )
        }
    }
}

@Composable
private fun UnitDropdownMenu(
    viewModel: EditItemViewModel,
    editItemUiState: EditItemUiState,
    modifier : Modifier = Modifier
) {
    Box(modifier = modifier) {
        if(editItemUiState.itemBeingEdited.quantity!=null &&
            editItemUiState.itemBeingEdited.quantity.amount!=0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = { viewModel.toggleUnitDropDownOpen() })
            ) {
                Text(
                    text = editItemUiState.itemBeingEdited.quantity.unit.name.lowercase()
                )
                Image(
                    painter = painterResource(R.drawable.baseline_arrow_right_24),
                    contentDescription = stringResource(R.string.expand_list),
                    modifier = Modifier.rotate(if (editItemUiState.unitDropDownExpanded) 270F else 90F)
                )
            }
        }
        DropdownMenu(
            expanded = editItemUiState.unitDropDownExpanded,
            onDismissRequest = { viewModel.toggleUnitDropDownOpen(false) }
        ) {
            MeasurementUnit.entries.forEach {
                DropdownMenuItem(
                    text = {
                        Text(text = it.name.lowercase())//stringResource(it.nameSingularStringId))
                    },
                    onClick = {
                        viewModel.quantityUnitChosen(it)
                        viewModel.toggleUnitDropDownOpen(false)
                    }
                )
            }
        }
    }
}

//@Preview
//@Composable
//fun EditItemInShoppingListDialogPreview() {
//    EditItemInShoppingListDialog(
//        {},
//        {},
//        LocalShoppingListDataProvider.shoppingList[3]
//    )
//}