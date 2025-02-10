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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
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
import com.example.happyplace.ShoppingListFilter
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
        val displayedItems = applyFilters(uiState.filterParams, uiState.shoppingList)

        if (uiState.shoppingList.isEmpty()) {
            Text(
                text = stringResource(
                    R.string.shopping_list_is_empty_tap_to_start,
                    stringResource(R.string.add_item)
                ),
                textAlign = TextAlign.Center,
                color = Color.DarkGray
            )
        }
        else {
            Column(verticalArrangement = Arrangement.Top, modifier = Modifier.fillMaxSize()) {
                ShoppingListActionsBar(
                    showFilterParams = uiState.showFilterParams,
                    onClickDeleteAll = { shoppingListViewModel.showClearAllConfirmationDialog() },
                    onClickFilter = { shoppingListViewModel.toggleShowFilterDialog() },
                    filterParams = uiState.filterParams,
                    onChangeFilter = { shoppingListViewModel.updateFilter(it) },
                    itemsList = uiState.shoppingList
                )
                if(displayedItems.isNotEmpty()) {
                    ShoppingList(
                        itemsList = displayedItems,
                        shoppingListViewModel = shoppingListViewModel,
                        setItemToBeEdited = { editItemViewModel.setItemBeingEdited(it) }
                    )
                }
                else {
                    Text(
                        text = stringResource(
                            R.string.no_item_matches_filter
                        ),
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray
                    )
                }
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
                bodyText = stringResource(R.string.confirm_delete_question_with_name,
                    uiState.itemStagedForEdition?.name ?: R.string.this_item),
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
                bodyText = stringResource(R.string.confirm_delete_all_question),
                onDismissRequest = {
                    shoppingListViewModel.dismissDeleteConfirmationDialog()
                },
                onConfirm = {
                    shoppingListViewModel.deleteAllItems()
                }
            )
        }
        else -> {}
    }
}

fun applyFilters(filterParams: ShoppingListFilter, shoppingList: List<ShoppingListItem>): List<ShoppingListItem> {
    return shoppingList
        .filter {
            // show bulk only
            (if (filterParams.bulk) it.bulk else true)
                    &&
                    // hide already bought
                    (if (filterParams.hideAlreadyInCart) !(it.isInCart) else true)
                    &&
                    // category match
                    (it.category.isEmpty() || filterParams.category.isEmpty() ||
                            it.category.trim() == filterParams.category.trim())
                    &&
                    // shop name match
                    (it.shop.isEmpty() || filterParams.shop.isEmpty() ||
                            it.shop.trim() == filterParams.shop.trim())
        }
        .sortedBy { "${when(filterParams.sortOrder) {
            ShoppingListFilter.SortOrder.NAME -> it.name
            ShoppingListFilter.SortOrder.DATE -> it.dateCreated
            else -> 1
        }}" }
        .sortedBy { if (filterParams.urgent) !(it.urgent && !it.isInCart) else true }

}

@Composable
fun FilterParametersBox(
    filterParams: ShoppingListFilter,
    onChangeFilter: (ShoppingListFilter) -> Unit,
    modifier: Modifier = Modifier,
    itemsList: List<ShoppingListItem>
) {
    val shopsList = mutableSetOf<String>()
    for(item in itemsList) shopsList.add(item.shop.trim())

    val categoriesList = mutableSetOf<String>()
    for(item in itemsList) categoriesList.add(item.category.trim())

    var shopDropdownExpanded by rememberSaveable { mutableStateOf(false) }
    var categoryDropdownExpanded by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier) {
        Column(Modifier.fillMaxWidth()) {
            // CATEGORY
            OptionsDropdownMenu(
                title = stringResource(R.string.category),
                options = categoriesList.toList(),
                currentOptionName = filterParams.category,
                onChooseOption = { onChangeFilter(filterParams.toBuilder().setCategory(it).build()) },
                expanded = categoryDropdownExpanded,
                onToggleExpanded = { categoryDropdownExpanded = !categoryDropdownExpanded },
                //modifier = TODO()
            )
            // SHOP
            OptionsDropdownMenu(
                title = stringResource(R.string.shop),
                options = shopsList.toList(),
                currentOptionName = filterParams.shop,
                onChooseOption = { onChangeFilter(filterParams.toBuilder().setShop(it).build()) },
                expanded = shopDropdownExpanded,
                onToggleExpanded = { shopDropdownExpanded = !shopDropdownExpanded },
                //modifier = TODO()
            )
            // BULK?
            FilterCheckBox(
                checked = filterParams.bulk,
                onCheckedChangeFilter = { onChangeFilter(filterParams.toBuilder()
                    .setBulk(it).build()) },
                textId = R.string.bulk_only
            )
            // URGENT?
            FilterCheckBox(
                checked = filterParams.urgent,
                onCheckedChangeFilter = { onChangeFilter(filterParams.toBuilder()
                    .setUrgent(it).build()) },
                textId = R.string.urgent_on_top
            )
            // HIDE ITEMS IN CART?
            FilterCheckBox(
                checked = filterParams.hideAlreadyInCart,
                onCheckedChangeFilter = { onChangeFilter(filterParams.toBuilder()
                    .setHideAlreadyInCart(it).build()) },
                textId = R.string.hide_already_bought
            )
        }
    }

//    message ShoppingListFilter {
//        bool hideAlreadyInCart = 1;
//        bool bulk = 5;
//        bool urgent = 4;

//        string category = 2;
//        string shop = 3;
//
//        enum SortOrder {
//            NONE = 0;
//            DATE = 1;
//            NAME = 2;
//        }
//        SortOrder sortOrder = 6;
//    }
}

@Composable
fun FilterCheckBox(checked: Boolean, onCheckedChangeFilter: (Boolean) -> Unit, textId: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onCheckedChangeFilter(it) }
        )
        Text(text = stringResource(textId))
    }
}

@Composable
private fun ShoppingList(
    itemsList: List<ShoppingListItem>,
    shoppingListViewModel: ShoppingListViewModel,
    setItemToBeEdited: (ShoppingListItem) -> Unit,
) {
    var expandedItemTimestamp by rememberSaveable { mutableLongStateOf(0L) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .clickable {
                expandedItemTimestamp = 0L
                shoppingListViewModel.showFilterDialog(false)
            }
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
fun ShoppingListActionsBar(
    showFilterParams: Boolean,
    onClickDeleteAll: () -> Unit,
    onClickFilter: () -> Unit,
    filterParams: ShoppingListFilter,
    onChangeFilter: (ShoppingListFilter) -> Unit,
    itemsList: List<ShoppingListItem>
) {
    val numActiveFilters = filterParams.numberOfActiveFilters()

    Column(modifier = Modifier
        .fillMaxWidth()
        .background(color = Color.LightGray)
        .padding(8.dp)
        .animateContentSize()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()

        ) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.clickable { onClickFilter() }
            ) {
                val color = if (showFilterParams) {
                    Color.Black
                } else {
                    if(numActiveFilters>0)
                        Color.DarkGray
                    else
                        Color.Gray
                }

                Box(contentAlignment = Alignment.TopEnd) {
                    Icon(
                        Icons.Filled.Menu,
                        stringResource(R.string.filter_options),
                        tint = color
                    )
                    if(numActiveFilters>0) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(12.dp).clip(CircleShape)
                                .background(color)
                        ) {
                            Text(
                                text = "$numActiveFilters",
                                color = Color.LightGray,
                                fontSize = 10.sp,
                                lineHeight = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
                Text(
                    text = stringResource(R.string.filter),
                    fontWeight = if (showFilterParams) FontWeight.SemiBold else FontWeight.Normal,
                    color = color
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
        if(showFilterParams) {
            FilterParametersBox(
                itemsList = itemsList,
                filterParams = filterParams,
                onChangeFilter = onChangeFilter,
                modifier = Modifier.fillMaxWidth().padding(16.dp))
        }
    }
}

fun ShoppingListFilter.numberOfActiveFilters() : Int {
    var n = 0
    if(bulk) n++
    if(urgent) n++
    if(hideAlreadyInCart) n++
    if(this.category.isNotEmpty()) n++
    if(this.shop.isNotEmpty()) n++
    if(this.sortOrder!=ShoppingListFilter.SortOrder.NONE &&
        this.sortOrder!=ShoppingListFilter.SortOrder.UNRECOGNIZED ) n++
    return n
}

@Composable
fun DeleteWarningPopupDialog(
    titleResId: Int?,
    bodyText: String,
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
            Text(text = bodyText)
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
        ) { toggleExpandCard(item) }
        //.clickable { toggleExpandCard(item) }
        .padding(8.dp)
        //.horizontalScroll(rememberScrollState())
        .animateContentSize(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            painter = painterResource(
                if (item.isInCart) R.drawable.baseline_check_24
                else R.drawable.baseline_arrow_right_24
            ),
            contentDescription = null,
            tint = if (item.isInCart) Color.Gray else LocalContentColor.current
        )
        Column(Modifier.padding(horizontal = 8.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                // NAME + QUANTITY + TAGS
                Column(Modifier.weight(1F)) {
                    // name + quantity
                    NameAndQuantityText(item)

                    if (!item.details.isNullOrEmpty())
                        Text(text = item.details, color = Color.Gray)

                    Row {
                        if (expanded) {
                            if (item.bulk)
                                TagBox(text = stringResource(R.string.bulk))

                            TagBox(text = item.category)

                            TagBox(text = item.shop)
                        }
                    }
                }
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
            .padding(top = 4.dp, end = 4.dp)
            .clip(RoundedCornerShape(33))
            .background(Color.LightGray)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
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

    val t = "${itemQuantity.amount} " +
            getUnitName(unit = itemQuantity.unit, plural = itemQuantity.amount>1)

    Text(
        text = "($t)",
        modifier = modifier,
        textDecoration = textDecoration,
        color = color,
        fontWeight = fontWeight
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
