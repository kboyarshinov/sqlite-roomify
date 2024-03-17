package dev.kboyarshinov.sqlroomify.lib

import okio.FileNotFoundException
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toOkioPath
import okio.buffer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SqlRoomifyTest {

    @TempDir
    lateinit var tempDirectory: File

    @Test
    fun `parse 2 create statements and generate 2 entities`() {
        val result = SqlRoomify.sqlToRoom(
            input = TestUtil.testResourcePath("sql/create-2-tables.sql"),
            outputDir = tempDirectory.toOkioPath(),
            outputPackage = "dev.test",
            databaseName = "TestDatabase"
        )

        assertSuccess(result) {
            assertEquals(2, it.tablesCount)
        }

        val generatedT1 = tempDirectory.resolve("dev/test/tables/T1.kt")
        assertTrue(generatedT1.exists())
        val generatedT2 = tempDirectory.resolve("dev/test/tables/T2.kt")
        assertTrue(generatedT2.exists())
        val generatedDatabase = tempDirectory.resolve("dev/test/TestDatabase.kt")
        assertTrue(generatedDatabase.exists())

        assertEquals(
            """
            package dev.test
            
            import androidx.room.Database
            import androidx.room.RoomDatabase
            import dev.test.tables.T1
            import dev.test.tables.T2
            
            @Database(
              entities = [
                T1::class,
                T2::class
              ],
              version = 1,
            )
            public abstract class TestDatabase : RoomDatabase()

            """.trimIndent(),
            generatedDatabase.read()
        )

        assertEquals(
            """
            package dev.test.tables
            
            import androidx.room.ColumnInfo
            import androidx.room.Entity
            import androidx.room.Ignore
            import androidx.room.Index
            import androidx.room.PrimaryKey
            import java.time.Instant
            import java.util.Date
            import kotlin.Boolean
            import kotlin.ByteArray
            import kotlin.Double
            import kotlin.Float
            import kotlin.Int
            import kotlin.String
            
            @Entity(
              tableName = "t1",
              indices = [
                Index(name = "t1_t_index", value = ["t"], unique = false)
              ],
              ignoredColumns = ["nu","b","date","dt"],
            )
            public data class T1(
              @PrimaryKey
              @ColumnInfo(
                name = "id",
                typeAffinity = ColumnInfo.INTEGER,
              )
              public val id: Int,
              @ColumnInfo(
                name = "t",
                typeAffinity = ColumnInfo.TEXT,
              )
              public val t: String,
              @ColumnInfo(
                name = "i",
                typeAffinity = ColumnInfo.INTEGER,
              )
              public val i: Int?,
              @ColumnInfo(
                name = "i1",
                typeAffinity = ColumnInfo.INTEGER,
              )
              public val i1: Int?,
              @ColumnInfo(
                name = "ti",
                typeAffinity = ColumnInfo.INTEGER,
              )
              public val ti: Int?,
              @ColumnInfo(
                name = "bi",
                typeAffinity = ColumnInfo.INTEGER,
              )
              public val bi: Int?,
              @ColumnInfo(
                name = "r",
                typeAffinity = ColumnInfo.REAL,
              )
              public val r: Double?,
              @ColumnInfo(
                name = "d",
                typeAffinity = ColumnInfo.REAL,
              )
              public val d: Double?,
              @ColumnInfo(
                name = "f",
                typeAffinity = ColumnInfo.REAL,
              )
              public val f: Float?,
              @ColumnInfo(
                name = "blob",
                typeAffinity = ColumnInfo.BLOB,
              )
              public val blob: ByteArray?,
              @Ignore
              public val nu: Double? = null,
              @Ignore
              public val b: Boolean? = null,
              @Ignore
              public val date: Date? = null,
              @Ignore
              public val dt: Instant? = null,
            )
            
            """.trimIndent(), generatedT1.read()
        )

        assertEquals(
            """
            package dev.test.tables
            
            import androidx.room.ColumnInfo
            import androidx.room.Entity
            import androidx.room.Ignore
            import androidx.room.Index
            import java.time.Instant
            import java.util.Date
            import kotlin.Boolean
            import kotlin.ByteArray
            import kotlin.Double
            import kotlin.Float
            import kotlin.Int
            import kotlin.String
            
            @Entity(
              tableName = "t2",
              indices = [
                Index(name = "t2_t_index", value = ["i1","date"], unique = true)
              ],
              ignoredColumns = ["time","ser","nu","b","date","dt"],
            )
            public data class T2(
              @ColumnInfo(
                name = "t",
                typeAffinity = ColumnInfo.TEXT,
              )
              public val t: String,
              @ColumnInfo(
                name = "i",
                typeAffinity = ColumnInfo.INTEGER,
              )
              public val i: Int?,
              @ColumnInfo(
                name = "i1",
                typeAffinity = ColumnInfo.INTEGER,
              )
              public val i1: Int?,
              @ColumnInfo(
                name = "ti",
                typeAffinity = ColumnInfo.INTEGER,
              )
              public val ti: Int?,
              @ColumnInfo(
                name = "bi",
                typeAffinity = ColumnInfo.INTEGER,
              )
              public val bi: Int?,
              @ColumnInfo(
                name = "r",
                typeAffinity = ColumnInfo.REAL,
              )
              public val r: Double?,
              @ColumnInfo(
                name = "d",
                typeAffinity = ColumnInfo.REAL,
              )
              public val d: Double?,
              @ColumnInfo(
                name = "f",
                typeAffinity = ColumnInfo.REAL,
              )
              public val f: Float?,
              @ColumnInfo(
                name = "blob",
                typeAffinity = ColumnInfo.BLOB,
              )
              public val blob: ByteArray?,
              @Ignore
              public val nu: Double? = null,
              @Ignore
              public val b: Boolean? = null,
              @Ignore
              public val date: Date? = null,
              @Ignore
              public val dt: Instant? = null,
            )
            
        """.trimIndent(), generatedT2.read()
        )
    }
}

fun assertSuccess(result: SqlRoomify.Result, func: ((SqlRoomify.Success) -> Unit)? = null) {
    assert(result is SqlRoomify.Success) { (result as SqlRoomify.Error).message }

    func?.invoke(result as SqlRoomify.Success)
}

fun File.read(): String = FileSystem.SYSTEM.source(toOkioPath()).buffer().readUtf8()

object TestUtil {
    @Throws(IOException::class)
    fun testResourcePath(path: String): Path {
        val file =
            TestUtil::javaClass.javaClass.classLoader.getResource(path)?.file?.let { File(it) }
                ?: throw FileNotFoundException("File not found: $path")
        return file.toOkioPath()
    }
}
