package com.aistudio.fasmovapos.app.ui.language

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.aistudio.fasmovapos.app.R

/** Display aliases only: never rename persisted products, categories or historical sale items. */
@StringRes
fun productNameResource(
  id: String,
  name: String,
): Int? =
  when (id to name) {
    "p-1" to "Plastic basin" -> R.string.catalog_basin
    "p-2" to "Bucket with lid" -> R.string.catalog_bucket
    "p-3" to "Stainless steel bowl" -> R.string.catalog_bowl
    "p-4" to "Ceramic mug" -> R.string.catalog_mug
    "p-5" to "Dinner plate" -> R.string.catalog_plate
    "p-6" to "Household broom" -> R.string.catalog_broom
    "p-7" to "Laundry soap bar" -> R.string.catalog_soap
    "p-8" to "Clothes pegs packet" -> R.string.catalog_pegs
    "p-9" to "Exercise book" -> R.string.catalog_book
    "p-10" to "LED bulb" -> R.string.catalog_bulb
    else -> null
  }

fun localizedProductName(
  context: Context,
  id: String,
  name: String,
): String = productNameResource(id, name)?.let { context.getString(it) } ?: name

@Composable
fun productLabel(
  id: String,
  name: String,
): String = productNameResource(id, name)?.let { stringResource(it) } ?: name

@StringRes
private fun categoryResource(category: String): Int? =
  when (category) {
    "Household", "Vifaa vya nyumbani" -> R.string.category_household
    "Kitchenware", "Vyombo vya jikoni" -> R.string.category_kitchen
    "Cleaning", "Vifaa vya usafi" -> R.string.category_cleaning
    "Electrical", "Vifaa vya umeme" -> R.string.category_electrical
    "Stationery", "Vifaa vya kuandikia" -> R.string.category_stationery
    "Bedding", "Vifaa vya kulalia" -> R.string.category_bedding
    "General", "Jumla" -> R.string.category_general
    "Other", "Nyingine" -> R.string.category_other
    else -> null
  }

/** UI grouping only: equivalent built-in category labels share one filter without rewriting records. */
fun categoryKey(category: String): String =
  when (categoryResource(category)) {
    R.string.category_household -> "Household"
    R.string.category_kitchen -> "Kitchenware"
    R.string.category_cleaning -> "Cleaning"
    R.string.category_electrical -> "Electrical"
    R.string.category_stationery -> "Stationery"
    R.string.category_bedding -> "Bedding"
    R.string.category_general -> "General"
    R.string.category_other -> "Other"
    else -> category
  }

@Composable
fun categoryLabel(category: String): String = categoryResource(category)?.let { stringResource(it) } ?: category

@StringRes
private fun personResource(
  id: String?,
  name: String,
): Int? =
  when (id to name) {
    "c-1" to "Training customer" -> R.string.training_customer
    "s-1" to "Training supplier" -> R.string.training_supplier
    "u-1" to "Training owner" -> R.string.training_owner
    else -> null
  }

fun localizedPersonName(
  context: Context,
  id: String?,
  name: String,
): String = personResource(id, name)?.let { context.getString(it) } ?: name

@Composable
fun personLabel(
  id: String?,
  name: String,
): String = personResource(id, name)?.let { stringResource(it) } ?: name

@Composable
fun unitLabel(unit: String): String =
  when (unit.lowercase()) {
    "pcs", "pieces" -> stringResource(R.string.unit_pieces)
    "packets" -> stringResource(R.string.unit_packets)
    "bottles" -> stringResource(R.string.unit_bottles)
    "boxes" -> stringResource(R.string.unit_boxes)
    else -> unit
  }

@Composable
fun orderStatusLabel(status: String): String =
  when (status) {
    "PENDING" -> stringResource(R.string.order_pending)
    "RECEIVED" -> stringResource(R.string.order_received)
    "CANCELLED" -> stringResource(R.string.order_cancelled)
    else -> status
  }

@Composable
fun expenseCategoryLabel(category: String): String =
  when (category) {
    "Rent", "Kodi ya pango" -> stringResource(R.string.expense_rent)
    "Wages", "Mishahara" -> stringResource(R.string.expense_wages)
    "Transport", "Usafiri" -> stringResource(R.string.expense_transport)
    "Utilities", "Huduma za msingi" -> stringResource(R.string.expense_utilities)
    "Taxes", "Kodi za serikali" -> stringResource(R.string.expense_taxes)
    "Miscellaneous", "Mengineyo" -> stringResource(R.string.expense_miscellaneous)
    "Freight", "Stock Freight", "Usafirishaji" -> stringResource(R.string.expense_freight)
    "Electricity", "Umeme" -> stringResource(R.string.expense_electricity)
    "Water", "Maji" -> stringResource(R.string.expense_water)
    "Supplies", "Vifaa" -> stringResource(R.string.expense_supplies)
    else -> categoryLabel(category)
  }
