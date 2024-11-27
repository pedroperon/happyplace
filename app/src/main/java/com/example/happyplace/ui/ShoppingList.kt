package com.example.happyplace.ui

import android.view.RoundedCorner
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.happyplace.R
import com.example.happyplace.data.LocalShoppingListDataProvider
import com.example.happyplace.model.ItemQuantity
import com.example.happyplace.model.ShoppingListItem
import com.example.happyplace.model.ShoppingListViewModel
import java.text.DateFormat

//ShoppingList(
////onEditItem = {/*TODO*/ },
//modifier = Modifier.fillMaxSize(),
//contentPadding = innerPadding
//)

@Composable
fun ShoppingList(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    viewModel: ShoppingListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showDeleteConfirmationDialog by rememberSaveable { mutableStateOf(false)}

    if (uiState.showEditItemDialog) {
        EditItemInShoppingListDialog(
            onDismissRequest = { viewModel.closeEditItemDialog() },
            onDone = { viewModel.saveNewItem(it) },
            item = uiState.itemStagedForEdition
        )
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())) {

            uiState.shoppingList.forEach { item ->
                key(item.name) {
                    ShoppingListItemCard(
                        item = item,
                        toggleItemInCart = {
                            viewModel.toggleItemBought(item)
                        },
                        expanded = item.showDetails,
                        toggleExpandCard = {
                            viewModel.toggleExpandItem(item)
                        },
                        onEditItem = {
                            viewModel.stageItem(item)
                            viewModel.openEditItemDialog(item)
                        },
                        onDeleteItem = {
                            viewModel.stageItem(item)
                            showDeleteConfirmationDialog = true
                        }
                    )
                }
            }
        }
    }

    if(showDeleteConfirmationDialog) {
        DeleteWarningPopupDialog(
            onDismissRequest = {
                viewModel.stageItem(null)
                showDeleteConfirmationDialog = false
            },
            onConfirm = {
                viewModel.deleteStagedItem()
                showDeleteConfirmationDialog = false
            }
        )
    }

}

@Composable
fun DeleteWarningPopupDialog(onDismissRequest: ()->Unit, onConfirm: ()->Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
            }) {
                Text(text = stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismissRequest()
            }) {
                Text(text = stringResource(R.string.cancel))
            }
        },
        title = {
            Text(text = stringResource(R.string.delete_item))
        },
        text = {
            Text(text = stringResource(R.string.confirm_delete_question))
        }

    )
}

@Composable
fun ShoppingListItemCard(item: ShoppingListItem,
                         toggleItemInCart: ()->Unit,
                         expanded: Boolean = false,
                         toggleExpandCard: ()->Unit,
                         modifier: Modifier = Modifier,
                         onEditItem: ()->Unit = {},
                         onDeleteItem: ()->Unit = {}
) {
    /*  OK val name : String,
        OK val quantity : ItemQuantity? = null,
        OK val details : String? = "",
        val bulk : Boolean? = false,
        val category : ItemCategory? = null,
        val shop : Shop? = null,
        OK val dateCreated : Date */

    Row(modifier = Modifier
        .padding(8.dp)
        .fillMaxWidth()
        .clickable { toggleItemInCart() }
//        .pointerInput(Unit) {
//            detectTapGestures(
//                onDoubleTap = {
//                    toggleItemInCart()
//            }
//            )
//        }
        .animateContentSize(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            painter = painterResource(
                if (item.isInCart) R.drawable.baseline_check_24
                else R.drawable.baseline_arrow_right_24
            ), null
        )
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(start = 4.dp)
        ) {
            Row {
                // name + quantity
                Text(
                    text = item.name,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    softWrap = false,
                )
                ItemQuantityText(
                    itemQuantity = item.quantity,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            if (item.details != null)
                Text(text = item.details, color = Color.Gray)

            if (expanded) {
                if (item.bulk)
                    TagBox(text = stringResource(R.string.bulk))

                if (item.category != null)
                    TagBox(text = stringResource(item.category.nameResId))

                if (item.shop != null)
                    TagBox(text = stringResource(item.shop.shopNameId))

                Spacer(modifier = Modifier.weight(1F))
                Text(
                    text = stringResource(
                        R.string.added_on_date,
                        DateFormat.getDateInstance().format(item.dateCreated)
                    ),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
        Spacer(modifier = Modifier.weight(1F))
        // BUTTONS (EXPAND, EDIT, DELETE)
        Column(
            //verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxHeight()
        ) {
            IconButton(onClick = toggleExpandCard) {
                Icon(
                    painter = painterResource(
                        if (expanded) R.drawable.baseline_keyboard_arrow_up_24
                        else R.drawable.baseline_keyboard_arrow_down_24
                    ), null,
                    tint = Color.Gray
                )
            }
            Spacer(Modifier.weight(1F))
            if (expanded && !item.isInCart) {
                Row(horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = onEditItem) {
                        Icon(Icons.Filled.Edit,
                            stringResource(R.string.edit),
                            tint = Color.Gray
                        )
                    }
                    IconButton(onClick = onDeleteItem) {
                        Icon(Icons.Filled.Delete,
                            stringResource(R.string.delete),
                            tint = Color.Gray
                        )
                    }
                }
            }
        }
    }
    HorizontalDivider()
}

@Composable
fun TagBox(text: String) {
    Box(contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(vertical = 4.dp)
            .background(Color.LightGray)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .background(Color.LightGray)
            )

    }
}


@Composable
fun ItemQuantityText(itemQuantity: ItemQuantity?, modifier: Modifier = Modifier) {
    if(itemQuantity==null || itemQuantity.amountNumber==0)
        return

    val isPlural = itemQuantity.amountNumber>1
    val t = "${itemQuantity.amountNumber} " +
            stringResource(
                if(isPlural) itemQuantity.unit.namePluralStringId
                else itemQuantity.unit.nameSingularStringId)

    Text(
        text = "($t)",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun ShoppingListItemCardPreview() {
    ShoppingListItemCard(item = LocalShoppingListDataProvider.getShoppingList()[3],
        expanded = true,
        toggleExpandCard = {},
        toggleItemInCart = {}
    )
}
