package dev.kboyarshinov.sqlroomify.cli.main

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import net.sf.jsqlparser.statement.Statement
import net.sf.jsqlparser.statement.create.table.ColDataType
import net.sf.jsqlparser.statement.create.table.ColumnDefinition
import net.sf.jsqlparser.statement.create.table.CreateTable
import okio.Path

internal class RoomEntitiesFileGenerator(
    private val outputDir: Path,
    private val outputPackage: String
) {

    fun generate(statements: List<Statement>): Result {
        val fileBuilder = FileSpec.builder(outputPackage, "Tables")
        val ignoredColumns = mutableListOf<String>()
        val tables = statements
            .filterIsInstance<CreateTable>()
            .map { statement ->
                val table = statement.table.name.replaceFirstChar { it.uppercase() }
                val entity = TypeSpec.classBuilder(ClassName(outputPackage, table))
                    .addModifiers(KModifier.DATA)
                    .addAnnotation(
                        AnnotationSpec.builder(Room.entityAnnotation)
                            .addMember("tableName = %S", statement.table.name)
                            .build()
                    )

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
                                .builder(
                                    columnName, it.asRoomType()
                                )
                                .initializer(columnName)
                                .build()
                        )
                    } catch (e: IllegalArgumentException) {
                        ignoredColumns.add(columnName)
                    }
                }
                entity.primaryConstructor(constructorBuilder.build())

                fileBuilder.addType(entity.build())
                table
            }

        val file = fileBuilder.build()
        file.writeTo(outputDir.toNioPath()).toFile()

        return Result(
            tableNames = tables,
            ignoredColumns = ignoredColumns
        )
    }

    internal data class Result(
        val tableNames: List<String>,
        val ignoredColumns: List<String>
    )
}


internal object Room {
    val entityAnnotation = ClassName("androidx.room", "Entity")
    val primaryKeyAnnotation = ClassName("androidx.room", "PrimaryKey")
    val columnInfoAnnotation = ClassName("androidx.room", "ColumnInfo")
}

internal fun ColumnDefinition.primaryKey() = columnSpecs
    ?.map { it.uppercase() }
    ?.containsAll(listOf("PRIMARY", "KEY")) ?: false

internal fun ColumnDefinition.notNull() = columnSpecs
    ?.map { it.uppercase() }
    ?.containsAll(listOf("NOT", "NULL")) ?: false

internal fun ColumnDefinition.asRoomType(): TypeName {
    return when (colDataType.dataType) {
        "TEXT" -> String::class.asTypeName()
        "INT", "INTEGER" -> Int::class.asClassName()
        "REAL", "DOUBLE" -> Double::class.asClassName()
        "FLOAT" -> Float::class.asClassName()
        "BOOLEAN" -> Boolean::class.asClassName()
        else -> throw IllegalArgumentException("Unsupported data type: ${colDataType.dataType}")
    }.copy(nullable = !notNull())
}