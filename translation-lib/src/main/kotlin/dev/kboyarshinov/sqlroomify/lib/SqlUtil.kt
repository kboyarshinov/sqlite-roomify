package dev.kboyarshinov.sqlroomify.lib

import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import net.sf.jsqlparser.schema.Table
import net.sf.jsqlparser.statement.create.index.CreateIndex
import net.sf.jsqlparser.statement.create.table.ColumnDefinition

internal fun CreateIndex.isUnique(): Boolean = index.type == "UNIQUE"

internal fun ColumnDefinition.primaryKey() =
    columnSpecs?.map { it.uppercase() }
        ?.containsAll(listOf("PRIMARY", "KEY")) ?: false

internal fun ColumnDefinition.notNull() =
    columnSpecs?.map { it.uppercase() }
        ?.containsAll(listOf("NOT", "NULL")) ?: false

internal fun Table.generatedTableName(): String =
    name.lowercase().replaceFirstChar { it.uppercase() }

internal fun ColumnDefinition.asRoomType(): TypeName {
    val columnType = colDataType.dataType.uppercase()
    return when (columnType) {
        "TEXT" -> String::class.asTypeName()
        "INT", "INTEGER" -> Int::class.asClassName()
        "REAL", "DOUBLE" -> Double::class.asClassName()
        "FLOAT" -> Float::class.asClassName()
        "BOOLEAN" -> Boolean::class.asClassName()
        else -> throw IllegalArgumentException("Unsupported data type: $columnType")
    }.copy(nullable = !notNull())
}
