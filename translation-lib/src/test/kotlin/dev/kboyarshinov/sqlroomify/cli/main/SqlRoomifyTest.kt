package dev.kboyarshinov.sqlroomify.cli.main

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
            outputPackage = "dev.test"
        )

        assertSuccess(result) {
            assertEquals(2, it.tablesCount)
        }

        val generatedFile = tempDirectory.resolve("dev/test/Tables.kt")
        assertTrue(generatedFile.exists())

        assertEquals(
            """
            package dev.test
            
            import androidx.room.ColumnInfo
            import androidx.room.Entity
            import androidx.room.Index
            import androidx.room.PrimaryKey
            import kotlin.Boolean
            import kotlin.Double
            import kotlin.Float
            import kotlin.Int
            import kotlin.String
            
            @Entity(
              tableName = "t1",
              indices = {
              Index(name = "t1_t_index", value = "t", unique = false),
              },
            )
            public data class T1(
              @PrimaryKey
              @ColumnInfo(name = "id")
              public val id: Int,
              @ColumnInfo(name = "t")
              public val t: String,
              @ColumnInfo(name = "i")
              public val i: Int?,
              @ColumnInfo(name = "i1")
              public val i1: Int?,
              @ColumnInfo(name = "r")
              public val r: Double?,
              @ColumnInfo(name = "d")
              public val d: Double?,
              @ColumnInfo(name = "f")
              public val f: Float?,
              @ColumnInfo(name = "b")
              public val b: Boolean?,
            )
            
            @Entity(
              tableName = "t2",
              indices = {
              Index(name = "t2_t_index", value = "i1,date", unique = true),
              },
            )
            public data class T2(
              @ColumnInfo(name = "t")
              public val t: String,
              @ColumnInfo(name = "i")
              public val i: Int?,
              @ColumnInfo(name = "i1")
              public val i1: Int?,
              @ColumnInfo(name = "r")
              public val r: Double?,
              @ColumnInfo(name = "d")
              public val d: Double?,
              @ColumnInfo(name = "f")
              public val f: Float?,
              @ColumnInfo(name = "b")
              public val b: Boolean?,
            )
            
        """.trimIndent(), generatedFile.read()
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
