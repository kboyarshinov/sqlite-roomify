package dev.kboyarshinov.sqlroomify.cli.main

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import net.sf.jsqlparser.schema.Table
import net.sf.jsqlparser.statement.Statement
import net.sf.jsqlparser.statement.create.index.CreateIndex
import net.sf.jsqlparser.statement.create.table.ColumnDefinition
import net.sf.jsqlparser.statement.create.table.CreateTable
import okio.Path

internal class RoomEntitiesFileGenerator(
    private val outputDir: Path, private val outputPackage: String
) {

    fun generate(statements: List<Statement>): Result {
        val fileBuilder = FileSpec.builder(outputPackage, "Tables")
        val ignoredColumns = mutableListOf<String>()
        val indices = statements.filterIsInstance<CreateIndex>()
        val tables = statements.filterIsInstance<CreateTable>().map { statement ->
            val tableIndices: List<CreateIndex> =
                indices.filter { it.table.name == statement.table.name }
            generateRoomEntity(statement, tableIndices, fileBuilder, ignoredColumns)
        }

        val file = fileBuilder.build()
        file.writeTo(outputDir.toNioPath()).toFile()

        return Result(
            tableNames = tables, ignoredColumns = ignoredColumns
        )
    }

    private fun generateRoomEntity(
        statement: CreateTable,
        tableIndices: List<CreateIndex>,
        fileBuilder: FileSpec.Builder,
        ignoredColumns: MutableList<String>
    ): String {
        val table = statement.table.generatedTableName()
        val indicesAnnotations = tableIndices.map { createIndex ->
            AnnotationSpec.builder(Room.indexAnnotation)
                .addMember("name = %S", createIndex.index.name)
                .addMember(
                    "value = %L",
                    CodeBlock.builder().apply {
                        add("[")
                        createIndex.index.columns.forEachIndexed { index, columnParams ->
                            if (index == createIndex.index.columns.size - 1) {
                                add("%S", columnParams.columnName)
                            } else {
                                add("%S,", columnParams.columnName)
                            }
                        }
                        add("]")
                    }.build()
                )
                .addMember("unique = %L", createIndex.isUnique())
                .build()
        }
        val entityAnnotation = AnnotationSpec
            .builder(Room.entityAnnotation)
            .addMember("tableName = %S", statement.table.name)

        if (indicesAnnotations.isNotEmpty()) {
            entityAnnotation.addMember(
                "indices = %L", CodeBlock.builder()
                    .apply {
                        add("[\n")
                        indicesAnnotations.forEachIndexed { index, spec ->
                            if (index == indicesAnnotations.size - 1) {
                                add("%L\n", spec)
                            } else {
                                add("%L,\n", spec)
                            }
                        }
                        add("]")
                    }
                    .build()
            )
        }
        val entity = TypeSpec.classBuilder(ClassName(outputPackage, table))
            .addModifiers(KModifier.DATA)
            .addAnnotation(entityAnnotation.build())

        val constructorBuilder = FunSpec.constructorBuilder()
        statement.columnDefinitions.forEach {
            val columnName = it.columnName.lowercase()
            try {
                val parameterBuilder = ParameterSpec.builder(columnName, it.asRoomType())
                if (it.primaryKey()) {
                    parameterBuilder.addAnnotation(Room.primaryKeyAnnotation)
                }
                parameterBuilder.addAnnotation(
                    AnnotationSpec.builder(Room.columnInfoAnnotation)
                        .addMember("name = %S", it.columnName)
                        .build()
                )
                constructorBuilder.addParameter(parameterBuilder.build())
                entity.addProperty(
                    PropertySpec
                        .builder(columnName, it.asRoomType())
                        .initializer(columnName)
                        .build()
                )
            } catch (e: IllegalArgumentException) {
                ignoredColumns.add(columnName)
            }
        }
        entity.primaryConstructor(constructorBuilder.build())

        fileBuilder.addType(entity.build())
        return table
    }

    internal data class Result(
        val tableNames: List<String>, val ignoredColumns: List<String>
    )

}

internal object Room {
    val entityAnnotation = ClassName("androidx.room", "Entity")
    val primaryKeyAnnotation = ClassName("androidx.room", "PrimaryKey")
    val columnInfoAnnotation = ClassName("androidx.room", "ColumnInfo")
    val indexAnnotation = ClassName("androidx.room", "Index")
}

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