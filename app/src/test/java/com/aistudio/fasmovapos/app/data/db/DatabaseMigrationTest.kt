package com.aistudio.fasmovapos.app.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Upgrades real databases from every shipped schema version, so an app update can never lose a shop's data. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DatabaseMigrationTest {
  private val dbName = "migration-test"

  @get:Rule
  val helper =
    MigrationTestHelper(
      InstrumentationRegistry.getInstrumentation(),
      FasMovaPOSDatabase::class.java,
    )

  @Test
  fun migrate1To2_keepsRows_andAddsSalePayments() {
    helper.createDatabase(dbName, 1).apply {
      execSQL("INSERT INTO branches (id, name, code, location, isHeadquarters) VALUES ('b-1', 'Shop', 'HQ-01', '', 1)")
      close()
    }

    val db = helper.runMigrationsAndValidate(dbName, 2, true, FasMovaPOSDatabase.MIGRATION_1_2)

    db.query("SELECT name FROM branches WHERE id = 'b-1'").use {
      assertTrue(it.moveToFirst())
      assertEquals("Shop", it.getString(0))
    }
    db.query("SELECT COUNT(*) FROM sale_payments").use {
      assertTrue(it.moveToFirst())
      assertEquals(0, it.getInt(0))
    }
  }

  @Test
  fun migrate2To3_keepsStaff_andExistingPinsDoNotNeedChanging() {
    helper.createDatabase(dbName, 2).apply {
      execSQL(
        "INSERT INTO staff_users (id, username, fullName, role, pinHash, pinSalt, branchId, isActive) " +
          "VALUES ('u-1', 'owner', 'Joseph', 'OWNER', 'hash', 'salt', 'b-1', 1)",
      )
      close()
    }

    val db = helper.runMigrationsAndValidate(dbName, 3, true, FasMovaPOSDatabase.MIGRATION_2_3)

    db.query("SELECT fullName, pinHash, mustChangePin FROM staff_users WHERE id = 'u-1'").use {
      assertTrue(it.moveToFirst())
      assertEquals("Joseph", it.getString(0))
      assertEquals("hash", it.getString(1))
      assertEquals(0, it.getInt(2))
    }
  }

  @Test
  fun migrate3To4_keepsBranches_withEmptyReceiptDetails() {
    helper.createDatabase(dbName, 3).apply {
      execSQL("INSERT INTO branches (id, name, code, location, isHeadquarters) VALUES ('b-1', 'Shop', 'HQ-01', '', 1)")
      close()
    }

    val db = helper.runMigrationsAndValidate(dbName, 4, true, FasMovaPOSDatabase.MIGRATION_3_4)

    db.query("SELECT name, phone, taxPin FROM branches WHERE id = 'b-1'").use {
      assertTrue(it.moveToFirst())
      assertEquals("Shop", it.getString(0))
      assertEquals("", it.getString(1))
      assertEquals("", it.getString(2))
    }
  }

  @Test
  fun migrateFromVersion1ToCurrent_runsTheWholeChain() {
    helper.createDatabase(dbName, 1).close()

    helper.runMigrationsAndValidate(
      dbName,
      4,
      true,
      FasMovaPOSDatabase.MIGRATION_1_2,
      FasMovaPOSDatabase.MIGRATION_2_3,
      FasMovaPOSDatabase.MIGRATION_3_4,
    )
  }
}
