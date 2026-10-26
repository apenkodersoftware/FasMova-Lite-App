package com.aistudio.fasmovapos.app.data.repository

import com.aistudio.fasmovapos.app.data.db.entities.CustomerEntity
import com.aistudio.fasmovapos.app.data.db.entities.ExpenseEntity
import com.aistudio.fasmovapos.app.data.db.entities.ProductEntity
import com.aistudio.fasmovapos.app.data.db.entities.SaleEntity
import com.aistudio.fasmovapos.app.data.db.entities.SaleItemEntity
import com.aistudio.fasmovapos.app.data.db.entities.SupplierEntity

/** Fictional training records loaded only when the owner opts in during setup. */
internal object SampleData {
  fun products(): List<ProductEntity> =
    listOf(
      product("p-1", "BASIN", "Plastic basin", "Household", 180.0, 250.0, 20),
      product("p-2", "BUCKET", "Bucket with lid", "Household", 250.0, 350.0, 12),
      product("p-3", "BOWL", "Stainless steel bowl", "Kitchenware", 80.0, 120.0, 24),
      product("p-4", "MUG", "Ceramic mug", "Kitchenware", 90.0, 150.0, 18),
      product("p-5", "PLATE", "Dinner plate", "Kitchenware", 110.0, 180.0, 30),
      product("p-6", "BROOM", "Household broom", "Cleaning", 80.0, 120.0, 3),
      product("p-7", "SOAP", "Laundry soap bar", "Cleaning", 50.0, 70.0, 36),
      product("p-8", "PEGS", "Clothes pegs packet", "Household", 40.0, 60.0, 15),
      product("p-9", "BOOK", "Exercise book", "Stationery", 35.0, 50.0, 40),
      product("p-10", "BULB", "LED bulb", "Electrical", 100.0, 160.0, 4),
    )

  private fun product(
    id: String,
    sku: String,
    name: String,
    category: String,
    cost: Double,
    price: Double,
    stock: Int,
  ) = ProductEntity(id, sku, "", name, category, "Pcs", cost, price, stock, 5, HQ_BRANCH_ID)

  fun suppliers(): List<SupplierEntity> = listOf(SupplierEntity("s-1", "Training supplier", "", "", "", ""))

  fun customers(): List<CustomerEntity> = listOf(CustomerEntity("c-1", "Training customer", "", "", creditLimit = 2000.0))

  fun expenses(): List<ExpenseEntity> = emptyList()

  fun firstSale(): Pair<SaleEntity, List<SaleItemEntity>> {
    val saleId = "training-sale-1"
    val items =
      listOf(
        SaleItemEntity(
          saleId = saleId,
          productId = "p-1",
          productName = "Plastic basin",
          quantity = 1,
          unitPrice = 250.0,
          totalPrice = 250.0,
        ),
      )
    val sale =
      SaleEntity(
        id = saleId,
        transactionRef = "TRAINING-001",
        branchId = HQ_BRANCH_ID,
        cashierId = "u-1",
        cashierName = "Training owner",
        totalAmount = 250.0,
        discountAmount = 0.0,
        taxAmount = 0.0,
        netAmount = 250.0,
        paymentMethod = "MPESA",
        paymentReference = "QKJ9821LXM",
        customerId = null,
        customerName = null,
        syncStatus = "LOCAL",
      )
    return sale to items
  }
}
