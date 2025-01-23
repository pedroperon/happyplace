package com.example.happyplace.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.happyplace.ItemQuantity
import com.example.happyplace.R
import com.example.happyplace.ShoppingListItem
import com.example.happyplace.model.EditItemViewModel
import com.example.happyplace.model.ShoppingListViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.happyplace.model.PopupDisplayState
import java.text.DateFormat
import java.util.Date

@Composable
fun ShoppingListScreen (
    modifier: Modifier = Modifier,
    shoppingListViewModel: ShoppingListViewModel,
    editItemViewModel: EditItemViewModel = viewModel()
) {
    val uiState by shoppingListViewModel.uiState.collectAsState()

    Box(modifier = modifier
        .fillMaxSize()
        .wrapContentSize()
    ) {
        if (uiState.shoppingList.isEmpty()) {
            Text(
                text = "Your shopping list is empty.\nTap \"Add item\" to start",
                textAlign = TextAlign.Center,
                color = Color.DarkGray
            )
        } else {
            Column {
                ShoppingListActionsBar(
                    onClickDeleteAll = { shoppingListViewModel.showClearAllConfirmationDialog() },
                    onClickFilter = { shoppingListViewModel.showFilterDialog() }
                )
                ShoppingList(
                    itemsList = uiState.shoppingList,
                    shoppingListViewModel = shoppingListViewModel,
                    setItemToBeEdited = { editItemViewModel.setItemBeingEdited(it) }
                )
            }
        }
    }

    when (uiState.popupDisplayState) {

        PopupDisplayState.EDIT_ITEM -> {
            // show edit / create item dialog
            val itemIndex = uiState.shoppingList.indexOf(uiState.itemStagedForEdition)
            EditItemInShoppingListDialog(
                onDismissRequest = {
                    shoppingListViewModel.closeEditItemDialog()
                    editItemViewModel.setItemBeingEdited(null)
                },
                onDone = { shoppingListViewModel.saveItem(itemIndex, it) },
                originalItem = uiState.itemStagedForEdition,
                shops = uiState.shopsList,
                categories = uiState.categoriesList,
                viewModel = editItemViewModel
            )
        }

        PopupDisplayState.DELETE_ITEM -> {
            // show delete item confirmation popup
            DeleteWarningPopupDialog(
                titleResId = R.string.delete_item,
                itemName = uiState.itemStagedForEdition?.name,
                onDismissRequest = {
                    shoppingListViewModel.dismissDeleteConfirmationDialog()
                },
                onConfirm = {
                    shoppingListViewModel.deleteStagedItem()
                }
            )
        }

        PopupDisplayState.CLEAR_LIST -> {
            // show clear list confirmation dialog
            DeleteWarningPopupDialog(
                titleResId = R.string.erase_list,
                itemName = stringResource(R.string.everything),
                onDismissRequest = {
                    shoppingListViewModel.dismissDeleteConfirmationDialog()
                },
                onConfirm = {
                    shoppingListViewModel.deleteAllItems()
                }
            )
        }
        PopupDisplayState.FILTER -> {
            FilterListPopupDialog()
        }
        else -> {}
    }
}

@Composable
fun FilterListPopupDialog() {
}

@Composable
private fun ShoppingList(
    itemsList: List<ShoppingListItem>,
    shoppingListViewModel: ShoppingListViewModel,
    setItemToBeEdited:(ShoppingListItem)->Unit
) {
    var expandedItemTimestamp by rememberSaveable { mutableLongStateOf(0L) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        itemsList.forEach { item ->
            key(item.name + item.dateCreated) {
                ShoppingListItemCard(
                    item = item,
                    toggleItemInCart = {
                        shoppingListViewModel.toggleItemBought(it)
                    },
                    expanded = (expandedItemTimestamp != 0L && item.dateCreated == expandedItemTimestamp),
                    toggleExpandCard = {
                        expandedItemTimestamp = when (expandedItemTimestamp) {
                            it.dateCreated -> 0L
                            else -> it.dateCreated
                        }
                    },
                    onClickEditItem = {
                        setItemToBeEdited(it)
                        shoppingListViewModel.openEditItemDialog(it)
                    },
                    onClickDeleteItem = {
                        shoppingListViewModel.showDeleteConfirmationDialog(it)
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun ShoppingListActionsBar(onClickDeleteAll:()->Unit, onClickFilter:()->Unit) {

    Row(horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.LightGray)
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onClickFilter() }
        ) {
            Icon(
                Icons.Filled.Menu,
                stringResource(R.string.filter_options),
                tint = Color.Gray
            )
            Text(text = stringResource(R.string.filter), color = Color.Gray)
        }

        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onClickDeleteAll() }
        ) {
            Icon(
                Icons.Filled.Delete,
                stringResource(R.string.clear_list),
                tint = Color.Gray
            )
            Text(text = stringResource(R.string.clear_list), color = Color.Gray)
        }
    }
}

@Composable
fun DeleteWarningPopupDialog(
    titleResId: Int?,
    itemName: String?,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
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
            Text(text = stringResource(titleResId ?: R.string.delete_item))
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
    onClickEditItem: (ShoppingListItem)->Unit = {},
    onClickDeleteItem: (ShoppingListItem)->Unit = {}
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .background(bgColorForItem(item))
        .combinedClickable(
            onDoubleClick = { toggleItemInCart(item) }
        ) { }
        .clickable { toggleExpandCard(item) }
        .padding(8.dp)
        .animateContentSize(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            painter = painterResource(
                if (item.isInCart) R.drawable.baseline_check_24
                else R.drawable.baseline_arrow_right_24
            ),
            contentDescription = null,
            tint = if(item.isInCart) Color.Gray else LocalContentColor.current
        )
        Column(Modifier.padding(horizontal = 8.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                // NAME + QUANTITY + TAGS
                Column {
                    // name + quantity
                    NameAndQuantityText(item)

                    if (!item.details.isNullOrEmpty())
                        Text(text = item.details, color = Color.Gray)

                    if (expanded) {
                        if (item.bulk)
                            TagBox(text = stringResource(R.string.bulk))

                        TagBox(text = item.category)

                        TagBox(text = item.shop)
                    }
                }
                Spacer(Modifier.weight(1F))
                // EXPAND CARD BUTTON
                Icon(
                    painter = painterResource(
                        if (expanded) R.drawable.baseline_keyboard_arrow_up_24
                        else R.drawable.baseline_keyboard_arrow_down_24
                    ), null,
                    tint = Color.Gray,
                    modifier = Modifier.clickable { toggleExpandCard(item) }
                )
            }
            if (expanded && !item.isInCart) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = stringResource(
                            R.string.added_on_date,
                            DateFormat.getDateInstance().format(Date(item.dateCreated))
                        ),
                        fontSize = 12.sp,
                        color = Color.Gray,
                    )
                    Spacer(modifier = modifier.weight(1F))
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.edit),
                        tint = Color.Gray,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clickable { onClickEditItem(item) }
                    )
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = Color.Gray,
                        modifier = Modifier
                            .clickable { onClickDeleteItem(item) }
                    )
                }
            }
        }
    }
    HorizontalDivider(
        color = Color.LightGray,
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}

@Composable
private fun NameAndQuantityText(item: ShoppingListItem) {
    Row(verticalAlignment = Alignment.Top) {
        val decoration = if (item.isInCart) TextDecoration.LineThrough else null
        val textColor =
            if (item.isInCart) Color.Gray else if (item.urgent) Color(0xFF880000) else Color.Unspecified
        val fontWeight =
            if (item.isInCart) FontWeight.Normal else if (item.urgent) FontWeight.SemiBold else FontWeight.Normal
        Text(
            text = item.name,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            softWrap = false,
            textDecoration = decoration,
            color = textColor,
            fontWeight = fontWeight
        )
        ItemQuantityText(
            itemQuantity = item.quantity,
            textDecoration = decoration,
            color = textColor,
            fontWeight = fontWeight,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

private fun bgColorForItem(item: ShoppingListItem): Color {
    val bgColor = if (item.isInCart) {
        Color(0xFFEFEFEF)
    } else {
//        if (item.urgent)
//            Color(0xFFFFDBDB)
//        else
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
fun ItemQuantityText(
    itemQuantity: ItemQuantity?,
    modifier: Modifier = Modifier,
    textDecoration: TextDecoration?,
    color: Color,
    fontWeight: FontWeight
) {
    if(itemQuantity==null || itemQuantity.amount==0)
        return

    val isPlural = itemQuantity.amount>1
    val t = "${itemQuantity.amount} " +
                if(isPlural) itemQuantity.unit.name.lowercase()//PluralStringId
                else itemQuantity.unit.name.lowercase()//SingularStringId

    Text(
        text = "($t)",
        modifier = modifier,
        textDecoration = textDecoration,
        color = color,
        fontWeight = fontWeight
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListTopBar() {
    TopAppBar(
        title = {
            Column() {
//                Text(
//                    text = stringResource(R.string.app_name),
//                    fontWeight = FontWeight.SemiBold,
//                    fontSize = 12.sp
//                )
                Text(
                    text = stringResource(R.string.my_shopping_List),
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF005500), titleContentColor = Color.White),
    )
}

@Preview(showBackground = true)
@Composable
fun ShoppingListItemCardPreview() {
    ShoppingListItemCard(
        item = ShoppingListItem.newBuilder().apply {
            name = "Arroz integral"
            details = "camargue"
            quantity = ItemQuantity.newBuilder().setAmount(1).setUnitValue(2).build()
            category = "Food"
            shop = "Cagette"
        }.build(),
        expanded = true,
        toggleExpandCard = {},
        toggleItemInCart = {}
    )
}
