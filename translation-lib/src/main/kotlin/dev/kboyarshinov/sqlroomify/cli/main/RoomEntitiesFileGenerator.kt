package dev.kboyarshinov.sqlroomify.cli.main

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import net.sf.jsqlparser.statement.Statement
import net.sf.jsqlparser.statement.create.table.ColDataType
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
                val table = statement.table.name
                val entity = TypeSpec.classBuilder(ClassName(outputPackage, table))
                    .addModifiers(KModifier.DATA)
                    .addAnnotation(
                        AnnotationSpec.builder(Room.entityAnnotation)
                            .build()
                    )

                val constructorBuilder = FunSpec.constructorBuilder()
                statement.columnDefinitions.forEach {
                    try {
                        constructorBuilder.addParameter(it.columnName, it.colDataType.asRoomType())
                    } catch (e: IllegalArgumentException) {
                        ignoredColumns.add(it.columnName)
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
}

internal fun ColDataType.asRoomType(): TypeName {
    return when (dataType) {
        "TEXT" -> String::class.asClassName()
        "INT", "INTEGER" -> Int::class.asClassName()
        "REAL", "DOUBLE" -> Double::class.asClassName()
        "FLOAT" -> Float::class.asClassName()
        "BOOLEAN" -> Boolean::class.asClassName()
        else -> throw IllegalArgumentException("Unsupported data type: $dataType")
    }
}