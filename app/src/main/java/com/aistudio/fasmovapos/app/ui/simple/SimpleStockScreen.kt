package com.aistudio.fasmovapos.app.ui.simple

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.aistudio.fasmovapos.app.R
import com.aistudio.fasmovapos.app.data.db.entities.ProductEntity
import com.aistudio.fasmovapos.app.ui.language.localizedProductName
import com.aistudio.fasmovapos.app.ui.language.productLabel
import com.aistudio.fasmovapos.app.ui.theme.FasMovaStatus

data class StockState(
  val products: List<ProductEntity>,
)

data class StockItemInput(
  val name: String,
  val price: Double,
  val quantity: Int,
  val sku: String,
  val category: String,
  val cost: Double,
  val threshold: Int,
)

fun sortedStock(products: List<ProductEntity>): List<ProductEntity> =
  products.sortedWith(
    compareBy<ProductEntity> {
      it.stockQuantity > it.minStockThreshold
    }.thenBy { it.stockQuantity }.thenBy { it.name.lowercase() },
  )

@Composable
fun SimpleStockScreen(
  state: StockState,
  onAdd: (StockItemInput) -> Unit,
  onAdjust: (String, Int, Double?, Double?) -> Unit,
  modifier: Modifier = Modifier,
) {
  val products = state.products
  var query by rememberSaveable { mutableStateOf("") }
  var lowOnly by rememberSaveable { mutableStateOf(false) }
  var adding by rememberSaveable { mutableStateOf(false) }
  var editingId by rememberSaveable { mutableStateOf<String?>(null) }
  val sorted = remember(products) { sortedStock(products) }
  val context = LocalContext.current
  val visible =
    sorted.filter {
      (it.name.contains(query, true) || localizedProductName(context, it.id, it.name).contains(query, true)) &&
        (!lowOnly || it.stockQuantity <= it.minStockThreshold)
    }
  Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    OutlinedTextField(value = query, onValueChange = {
      query = it
    }, label = { Text(stringResource(R.string.simple_search_items)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
    FilterChip(selected = lowOnly, onClick = {
      lowOnly = !lowOnly
    }, label = { Text(stringResource(R.string.simple_low_stock), fontSize = 16.sp) }, modifier = Modifier.heightIn(min = 48.dp))
    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      if (visible.isEmpty()) {
        item { Text(stringResource(if (products.isEmpty()) R.string.simple_empty_shop else R.string.simple_no_matches), fontSize = 16.sp) }
      }
      items(visible, key = { it.id }) { product ->
        Card(onClick = { editingId = product.id }, modifier = Modifier.fillMaxWidth().testTag("simple_stock_${product.id}")) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              productLabel(product.id, product.name),
              modifier = Modifier.fillMaxWidth(),
              fontSize = 18.sp,
              lineHeight = 26.sp,
              fontWeight = FontWeight.Medium,
            )
            Text(
              simpleMoney(product.sellingPrice),
              modifier = Modifier.fillMaxWidth(),
              fontSize = 20.sp,
              lineHeight = 28.sp,
              fontWeight = FontWeight.Bold,
            )
            Text(
              pluralStringResource(R.plurals.simple_stock_left, product.stockQuantity, product.stockQuantity),
              modifier = Modifier.fillMaxWidth(),
              fontSize = 16.sp,
              lineHeight = 24.sp,
              color =
                when {
                  product.stockQuantity == 0 -> FasMovaStatus.danger
                  product.stockQuantity <= product.minStockThreshold -> FasMovaStatus.warning
                  else -> MaterialTheme.colorScheme.onSurface
                },
            )
          }
        }
      }
    }
    Button(onClick = { adding = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("simple_add_item")) {
      Text(stringResource(if (products.isEmpty()) R.string.simple_first_item else R.string.simple_add_item), fontSize = 18.sp)
    }
  }
  if (adding) {
    SimpleAddItemDialog(onDismiss = { adding = false }, onSave = {
      onAdd(it)
      adding = false
    })
  }
  products.firstOrNull { it.id == editingId }?.let { product ->
    SimpleEditStockDialog(product, onDismiss = { editingId = null }, onSave = {
      delta,
      cost,
      price,
      ->
      onAdjust(product.id, delta, cost, price)
      editingId =
        null
    })
  }
}

@Composable
private fun SimpleAddItemDialog(
  onDismiss: () -> Unit,
  onSave: (StockItemInput) -> Unit,
) {
  var name by rememberSaveable { mutableStateOf("") }
  var price by rememberSaveable { mutableStateOf("") }
  var quantity by rememberSaveable { mutableStateOf("0") }
  var sku by rememberSaveable { mutableStateOf("") }
  val defaultCategory = stringResource(R.string.simple_default_category)
  var category by rememberSaveable { mutableStateOf(defaultCategory) }
  var cost by rememberSaveable { mutableStateOf("0") }
  var threshold by rememberSaveable { mutableStateOf("5") }
  var details by rememberSaveable { mutableStateOf(false) }
  val valid =
    name.isNotBlank() &&
      validPrice(price) &&
      validCost(cost) &&
      (quantity.toIntOrNull() ?: -1) >= 0 &&
      (threshold.toIntOrNull() ?: -1) >= 0 &&
      category.isNotBlank()
  AlertDialog(
    properties = DialogProperties(usePlatformDefaultWidth = false),
    modifier = Modifier.padding(horizontal = 24.dp),
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.simple_add_item)) },
    text = {
      Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SimpleItemField(name, { name = it }, R.string.simple_item_name, "simple_item_name")
        SimpleItemField(price, { price = it }, R.string.simple_price, "simple_item_price", KeyboardType.Decimal)
        SimpleItemField(quantity, { quantity = it }, R.string.simple_quantity, "simple_item_quantity", KeyboardType.Number)
        TextButton(onClick = { details = !details }) { Text(stringResource(R.string.simple_more_details), fontSize = 16.sp) }
        if (details) {
          SimpleItemField(sku, { sku = it }, R.string.simple_sku_optional, "simple_item_sku")
          SimpleItemField(category, { category = it }, R.string.simple_category, "simple_item_category")
          SimpleItemField(cost, { cost = it }, R.string.simple_cost, "simple_item_cost", KeyboardType.Decimal)
          SimpleItemField(threshold, { threshold = it }, R.string.simple_warning_level, "simple_item_threshold", KeyboardType.Number)
          Text(stringResource(R.string.simple_suppliers_hint), fontSize = 16.sp)
        }
      }
    },
    confirmButton = {
      Button(onClick = {
        onSave(
          StockItemInput(name.trim(), price.toDouble(), quantity.toInt(), sku.trim(), category.trim(), cost.toDouble(), threshold.toInt()),
        )
      }, enabled = valid, modifier = Modifier.testTag("simple_save_item")) {
        Text(stringResource(R.string.simple_save), fontSize = 16.sp)
      }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.simple_cancel), fontSize = 16.sp) } },
  )
}

@Composable
private fun SimpleEditStockDialog(
  product: ProductEntity,
  onDismiss: () -> Unit,
  onSave: (Int, Double?, Double?) -> Unit,
) {
  var quantity by rememberSaveable(product.id) { mutableStateOf("0") }
  var price by rememberSaveable(product.id) { mutableStateOf(displayAmount(product.sellingPrice)) }
  var cost by rememberSaveable(product.id) { mutableStateOf(displayAmount(product.costPrice)) }
  var details by rememberSaveable { mutableStateOf(false) }
  var remove by rememberSaveable { mutableStateOf(false) }
  val delta = (quantity.toIntOrNull() ?: 0) * if (remove) -1 else 1
  val newStock = product.stockQuantity.toLong() + delta
  val valid = (quantity.toIntOrNull() ?: -1) >= 0 && newStock in 0..Int.MAX_VALUE.toLong() && validPrice(price) && validCost(cost)
  AlertDialog(
    properties = DialogProperties(usePlatformDefaultWidth = false),
    modifier = Modifier.padding(horizontal = 24.dp),
    onDismissRequest = onDismiss,
    title = { Text(productLabel(product.id, product.name)) },
    text = {
      Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(pluralStringResource(R.plurals.simple_stock_left, product.stockQuantity, product.stockQuantity), fontSize = 16.sp)
        SimpleItemField(price, { price = it }, R.string.simple_price, "simple_edit_price", KeyboardType.Decimal)
        SimpleItemField(quantity, {
          quantity = it
        }, if (remove) R.string.simple_remove_quantity else R.string.simple_add_quantity, "simple_edit_quantity", KeyboardType.Number)
        TextButton(onClick = { details = !details }) { Text(stringResource(R.string.simple_more_details), fontSize = 16.sp) }
        if (details) {
          SimpleItemField(cost, { cost = it }, R.string.simple_cost, "simple_edit_cost", KeyboardType.Decimal)
          Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = remove, onCheckedChange = { remove = it })
            Text(stringResource(R.string.simple_remove_stock), fontSize = 16.sp)
          }
        }
      }
    },
    confirmButton = {
      Button(onClick = {
        onSave(
          delta,
          cost.toDouble().takeIf {
            it != product.costPrice
          },
          price.toDouble(),
        )
      }, enabled = valid) { Text(stringResource(R.string.simple_save), fontSize = 16.sp) }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.simple_cancel), fontSize = 16.sp) } },
  )
}

@Composable
private fun SimpleItemField(
  value: String,
  onValueChange: (String) -> Unit,
  @StringRes label: Int,
  tag: String,
  keyboard: KeyboardType = KeyboardType.Text,
) {
  OutlinedTextField(value = value, onValueChange = onValueChange, label = {
    Text(stringResource(label))
  }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = keyboard), modifier = Modifier.fillMaxWidth().testTag(tag))
}

private fun validPrice(value: String): Boolean =
  value.toBigDecimalOrNull()?.let { it.signum() > 0 && it.stripTrailingZeros().scale() <= 2 && it.toDouble().isFinite() } == true

private fun validCost(value: String): Boolean =
  value.toBigDecimalOrNull()?.let { it.signum() >= 0 && it.stripTrailingZeros().scale() <= 2 && it.toDouble().isFinite() } == true
