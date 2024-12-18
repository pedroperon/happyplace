package com.example.happyplace.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.happyplace.ItemQuantity
import com.example.happyplace.R
import com.example.happyplace.ShoppingListItem
import com.example.happyplace.model.EditItemViewModel
import com.example.happyplace.model.ShoppingListViewModel
import java.text.DateFormat
import java.util.Date


@Composable
fun ShoppingList(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    viewModel: ShoppingListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    var showDeleteConfirmationDialog by rememberSaveable { mutableStateOf(false)}
    var expandedItemTimestamp by rememberSaveable { mutableLongStateOf(0) }


    if (uiState.showEditItemDialog) {
        val itemIndex = uiState.shoppingList.indexOf(uiState.itemStagedForEdition)
        EditItemInShoppingListDialog(
            onDismissRequest = { viewModel.closeEditItemDialog() },
            onDone = { viewModel.saveItem(itemIndex, it) },
            originalItem = uiState.itemStagedForEdition,
            shops = uiState.shopsList,
            categories = uiState.categoriesList
        )
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())) {

            uiState.shoppingList.forEach { item ->
                key(item.name + item.dateCreated) {
                    ShoppingListItemCard(
                        item = item,
                        toggleItemInCart = {
                            viewModel.toggleItemBought(it)
                        },
                        expanded = (expandedItemTimestamp!=0L && item.dateCreated==expandedItemTimestamp),
                        toggleExpandCard = {
                            expandedItemTimestamp = when(expandedItemTimestamp) {
                                item.dateCreated -> 0
                                else -> item.dateCreated
                            }
                        },
                        onEditItem = {
                            viewModel.openEditItemDialog(it)
                        },
                        onDeleteItem = {
                            viewModel.stageItem(it)
                            showDeleteConfirmationDialog = true
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    if(showDeleteConfirmationDialog) {
        DeleteWarningPopupDialog(
            itemName = uiState.itemStagedForEdition?.name,
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
fun DeleteWarningPopupDialog(itemName: String?,
                             onDismissRequest: ()->Unit,
                             onConfirm: ()->Unit) {
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
            Text(text =
            if (itemName == null)
                stringResource(R.string.confirm_delete_question)
            else
                stringResource(R.string.confirm_delete_question_with_name, itemName)
            )
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShoppingListItemCard(
    item: ShoppingListItem,
    toggleItemInCart: (ShoppingListItem)->Unit,
    expanded: Boolean = false,
    toggleExpandCard: (ShoppingListItem)->Unit,
    modifier: Modifier = Modifier,
    onEditItem: (ShoppingListItem)->Unit = {},
    onDeleteItem: (ShoppingListItem)->Unit = {}
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .background(bgColorForItem(item))
        .combinedClickable(
            onDoubleClick = { toggleItemInCart(item) }
        ) { /*TODO: show vanishing msg saying double-tap-to-move-in-out-of-cart*/ }
        .padding(8.dp)
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
//            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(start = 4.dp)
        ) {
            Row {
                // name + quantity
                val decoration = if(item.isInCart) TextDecoration.LineThrough else null
                val textColor = if(item.isInCart) Color.Gray else Color.Unspecified
                Text(
                    text = item.name,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    softWrap = false,
                    textDecoration = decoration,
                    color = textColor
                )
                ItemQuantityText(
                    itemQuantity = item.quantity,
                    textDecoration = decoration,
                    color = textColor,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            if (!item.details.isNullOrEmpty())
                Text(text = item.details, color = Color.Gray)

            if (expanded) {
                if (item.bulk)
                    TagBox(text = stringResource(R.string.bulk))

                TagBox(text = item.category)

                TagBox(text = item.shop)

                Spacer(modifier = Modifier.weight(1F))
                Text(
                    text = stringResource(
                        R.string.added_on_date,
                        DateFormat.getDateInstance().format(Date(item.dateCreated))
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
            IconButton(onClick = { toggleExpandCard(item) }) {
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
                    IconButton(onClick = { onEditItem(item) }) {
                        Icon(Icons.Filled.Edit,
                            stringResource(R.string.edit),
                            tint = Color.Gray
                        )
                    }
                    IconButton(onClick = { onDeleteItem(item) }) {
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

private fun bgColorForItem(item: ShoppingListItem): Color {
    val bgColor = if (item.isInCart) {
        Color(0xFFEEEEEE)
    } else {
        if (item.urgent)
            Color(0xFFFFDBDB)
        else
            Color(0xFFF8F8F8)
    }
    return bgColor
}

@Composable
fun TagBox(text: String?) {
    if(text.isNullOrEmpty())
        return

    Box(contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(33))
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
fun ItemQuantityText(itemQuantity: ItemQuantity?,
                     modifier: Modifier = Modifier,
                     textDecoration: TextDecoration?,
                     color: Color) {
    if(itemQuantity==null || itemQuantity.amount==0)
        return

    val isPlural = itemQuantity.amount>1
    val t = "${itemQuantity.amount} " +
//            stringResource(
                if(isPlural) itemQuantity.unit.name.lowercase()//PluralStringId
                else itemQuantity.unit.name.lowercase()//SingularStringId
//            )

    Text(
        text = "($t)",
        modifier = modifier,
        textDecoration = textDecoration,
        color = color
    )
}

//@Preview(showBackground = true)
//@Composable
//fun ShoppingListItemCardPreview() {
//    ShoppingListItemCard(item = LocalShoppingListDataProvider.shoppingList.collectLatest {  }
//        expanded = true,
//        toggleExpandCard = {},
//        toggleItemInCart = {}
//    )
//}
