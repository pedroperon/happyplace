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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.happyplace.R
import com.example.happyplace.data.LocalShoppingListDataProvider
import com.example.happyplace.model.EditItemUiState
import com.example.happyplace.model.EditItemViewModel
import com.example.happyplace.model.MeasuringUnit
import com.example.happyplace.model.ShoppingListItem


/*  OK val name : String,
    OK val quantity : ItemQuantity? = null,
    OK val details : String? = "",
    OK val bulk : Boolean? = false,
    >> val category : ItemCategory? = null,
    >> val shop : Shop? = null,
 */

@Composable
fun EditItemInShoppingListDialog(
    onDismissRequest: ()->Unit,
    onDone: (ShoppingListItem)->Unit,
    item: ShoppingListItem?
) {
    val isNewItem = (item==null)

    val viewModel = EditItemViewModel(item ?: ShoppingListItem(""))
    val editItemUiState by viewModel.uiState.collectAsState()

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
                        value = "${editItemUiState.itemBeingEdited.quantity?.amountNumber ?: ""}",
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

@Composable
private fun UnitDropdownMenu(
    viewModel: EditItemViewModel,
    editItemUiState: EditItemUiState,
    modifier : Modifier = Modifier
) {
    Box(modifier = modifier) {
        if(editItemUiState.itemBeingEdited.quantity!=null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = { viewModel.toggleUnitDropDownOpen() })
            ) {
                Text(
                    text = stringResource(
                        editItemUiState.itemBeingEdited.quantity.unit.namePluralStringId
                    )
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
            MeasuringUnit.entries.forEach {
                DropdownMenuItem(
                    text = {
                        Text(text = stringResource(it.nameSingularStringId))
                    },
                    onClick = {
                        viewModel.chosenQuantityUnit(it)
                        viewModel.toggleUnitDropDownOpen(false)
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun EditItemInShoppingListDialogPreview() {
    EditItemInShoppingListDialog(
        {},
        {},
        LocalShoppingListDataProvider.getShoppingList()[3]
    )
}