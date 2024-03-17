package dev.kboyarshinov.sqliteroomify.lib

import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import net.sf.jsqlparser.statement.create.table.ColumnDefinition
import java.time.Instant
import java.util.Date

/**
 * Converts SQLite data types to Room compatible types according to
 * https://sqlite.org/datatype3.html
 */
internal object SqliteToRoomColumnDataTypeConverter {

    fun toRoomColumn(column: ColumnDefinition): Result {
        val columnName = column.columnName
        val columnType = column.colDataType.dataType.uppercase()
        val roomColumnType = column.roomType()
        val affinity = column.typeAffinity()
        return when {
            roomColumnType != null && affinity == TypeAffinity.NUMERIC ->
                IgnoredType(
                    columnName = columnName,
                    typeName = roomColumnType,
                )

            roomColumnType == null || affinity == null ->
                UnsupportedType(
                    columnName = columnName,
                    columnType = columnType,
                )

            else ->
                SupportedType(
                    columnName = columnName,
                    typeName = roomColumnType,
                    typeAffinity = affinity,
                    primaryKey = column.primaryKey()
                )

        }
    }

    sealed interface Result

    data class SupportedType(
        val columnName: String,
        val typeName: TypeName,
        val typeAffinity: TypeAffinity,
        val primaryKey: Boolean,
    ) : Result

    data class IgnoredType(
        val columnName: String,
        val typeName: TypeName,
    ) : Result

    data class UnsupportedType(
        val columnName: String,
        val columnType: String,
    ) : Result
}

internal enum class TypeAffinity {
    INTEGER, TEXT, BLOB, REAL, NUMERIC, UNKNOWN
}

private fun ColumnDefinition.primaryKey() =
    columnSpecs?.map { it.uppercase() }
        ?.containsAll(listOf("PRIMARY", "KEY")) ?: false

private fun ColumnDefinition.notNull() =
    columnSpecs?.map { it.uppercase() }
        ?.containsAll(listOf("NOT", "NULL")) ?: false

private fun ColumnDefinition.roomType(): TypeName? {
    val columnType = colDataType.dataType.uppercase()
    return when (columnType) {
        "INT",
        "INTEGER",
        "TINYINT",
        "SMALLINT",
        "MEDIUMINT",
        "BIGINT",
        "UNSIGNED BIG INT",
        "INT2",
        "INT8" -> Int::class.asClassName()

        "CHARACTER",
        "VARCHAR",
        "TEXT",
        "VARYING CHARACTER",
        "NCHAR",
        "NATIVE CHARACTER",
        "NVARCHAR",
        "CLOB" -> String::class.asTypeName()

        "REAL",
        "DOUBLE",
        "DOUBLE PRECISION" -> Double::class.asClassName()

        "FLOAT" -> Float::class.asClassName()
        "BLOB" -> ByteArray::class.asClassName()
        "BOOLEAN" -> Boolean::class.asClassName()
        "DATE" -> Date::class.asClassName()
        "DATETIME" -> Instant::class.asClassName()
        "NUMERIC" -> Double::class.asClassName()
        else -> null
    }?.copy(nullable = !notNull())
}

private fun ColumnDefinition.typeAffinity(): TypeAffinity? {
    val columnType = colDataType.dataType.uppercase()
    return when (columnType) {
        "INT",
        "INTEGER",
        "TINYINT",
        "SMALLINT",
        "MEDIUMINT",
        "BIGINT",
        "UNSIGNED BIG INT",
        "INT2",
        "INT8" -> TypeAffinity.INTEGER

        "CHARACTER",
        "VARCHAR",
        "TEXT",
        "VARYING CHARACTER",
        "NCHAR",
        "NATIVE CHARACTER",
        "NVARCHAR",
        "CLOB" -> TypeAffinity.TEXT

        "REAL",
        "DOUBLE",
        "DOUBLE PRECISION",
        "FLOAT" -> TypeAffinity.REAL

        "BLOB" -> TypeAffinity.BLOB
        "BOOLEAN",
        "DECIMAL",
        "DATE",
        "DATETIME",
        "NUMERIC" -> TypeAffinity.NUMERIC

        else -> null
    }
}
