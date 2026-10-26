package com.aistudio.fasmovapos.app.data.backup

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.os.Build
import com.aistudio.fasmovapos.app.data.db.FasMovaPOSDatabase
import java.io.File

/**
 * Creates and restores whole-database backups. A backup is the SQLite file itself, encrypted by [BackupCrypto],
 * so it carries every sale, product, customer and staff record exactly as they are on this phone.
 */
class BackupManager(
  private val context: Context,
) {
  private val databaseFile: File get() = context.getDatabasePath(FasMovaPOSDatabase.DATABASE_NAME)

  /** Returns the encrypted backup bytes for the live database. */
  fun createBackup(password: CharArray): ByteArray {
    // Fold the write-ahead log into the main file so the copy below is complete.
    FasMovaPOSDatabase
      .getDatabase(context)
      .openHelper.writableDatabase
      .query("PRAGMA wal_checkpoint(TRUNCATE)")
      .use { it.moveToFirst() }
    return BackupCrypto.encrypt(databaseFile.readBytes(), password, sha256Available = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
  }

  /**
   * Replaces the live database with the one inside [backup]. Throws [BackupFormatException] (and changes nothing) if
   * the password is wrong or the file is damaged, from a newer app, or not a FasMova database. The caller must
   * restart the app afterwards: every open repository still points at the old database.
   */
  fun restore(
    backup: ByteArray,
    password: CharArray,
  ) {
    val candidate = File(context.cacheDir, "restore-candidate.db")
    try {
      candidate.writeBytes(BackupCrypto.decrypt(backup, password))
      verifyDatabase(candidate)
      swapIn(candidate)
    } finally {
      candidate.delete()
    }
  }

  /** Any SQLite error here (not a database, corrupt pages) means the file is not a usable backup. */
  private fun verifyDatabase(file: File) {
    try {
      SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY).use { checkContents(it) }
    } catch (e: SQLiteException) {
      throw BackupFormatException("The backup does not contain a valid database", e)
    }
  }

  private fun checkContents(db: SQLiteDatabase) {
    if (db.version > FasMovaPOSDatabase.VERSION) reject("This backup was made by a newer version of the app")
    if (db.version < 1) reject("The backup does not contain a FasMova database")
    val integrity = db.rawQuery("PRAGMA integrity_check", null).use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else "" }
    if (integrity != "ok") reject("The backup is damaged")
    val hasStaff =
      db.rawQuery("SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = 'staff_users'", null).use { cursor ->
        cursor.moveToFirst()
      }
    if (!hasStaff) reject("The backup does not contain a FasMova database")
  }

  private fun reject(message: String): Nothing = throw BackupFormatException(message)

  /** Swaps [candidate] in, keeping the old database aside so a failed copy can be undone. */
  private fun swapIn(candidate: File) {
    FasMovaPOSDatabase.closeAndReset()
    val live = databaseFile
    val aside = File(live.path + ".before-restore")
    val sidecars = listOf(File(live.path + "-wal"), File(live.path + "-shm"))
    live.copyTo(aside, overwrite = true)
    try {
      sidecars.forEach { it.delete() }
      candidate.copyTo(live, overwrite = true)
    } catch (e: java.io.IOException) {
      aside.copyTo(live, overwrite = true)
      throw e
    }
  }
}
