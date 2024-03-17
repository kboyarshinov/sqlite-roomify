package dev.kboyarshinov.sqlroomify.lib

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import net.sf.jsqlparser.schema.Table
import net.sf.jsqlparser.statement.create.table.CreateTable

internal object RoomEntityGenerator {

    internal fun generateRoomEntity(
        outputPackage: String,
        statement: CreateTable,
        indicesAnnotations: List<AnnotationSpec>,
    ): Result {
        val ignoredColumns = mutableListOf<String>()

        val table = statement.table.generatedTableName()
        val entityAnnotation = AnnotationSpec
            .builder(RoomClasses.entityAnnotation)
            .addMember("tableName = %S", statement.table.name)

        if (indicesAnnotations.isNotEmpty()) {
            entityAnnotation.addMember(
                arrayCodeBlockMember(
                    "indices",
                    indicesAnnotations,
                    itemFormat = "%L",
                    addLineBreaks = true
                )
            )
        }
        val entityBuilder = TypeSpec.classBuilder(ClassName(outputPackage, table))
            .addModifiers(KModifier.DATA)

        val constructorBuilder = FunSpec.constructorBuilder()
        statement.columnDefinitions.forEach { columnDef ->
            when (val result = SqliteToRoomColumnDataTypeConverter.toRoomColumn(columnDef)) {
                is SqliteToRoomColumnDataTypeConverter.SupportedType -> {
                    buildSupportedColumnSpec(result, constructorBuilder, entityBuilder)
                }

                is SqliteToRoomColumnDataTypeConverter.UnsupportedType -> {
                    ignoredColumns.add(result.columnName)
                }
            }
        }
        entityBuilder.addAnnotation(entityAnnotation.build())
        entityBuilder.primaryConstructor(constructorBuilder.build())

        return Result(
            tableName = table,
            spec = entityBuilder.build(),
        )
    }

    private fun buildSupportedColumnSpec(
        result: SqliteToRoomColumnDataTypeConverter.SupportedType,
        constructorBuilder: FunSpec.Builder,
        entity: TypeSpec.Builder
    ) {
        val columnName = result.columnName.lowercase()
        val parameterBuilder = ParameterSpec.builder(columnName, result.typeName)
        if (result.primaryKey) {
            parameterBuilder.addAnnotation(RoomClasses.primaryKeyAnnotation)
        }
        parameterBuilder.addAnnotation(
            AnnotationSpec.builder(RoomClasses.columnInfoAnnotation)
                .addMember("name = %S", result.columnName)
//                .addMember("affinity = %T")
                .build()
        )
        constructorBuilder.addParameter(parameterBuilder.build())
        entity.addProperty(
            PropertySpec
                .builder(columnName, result.typeName)
                .initializer(columnName)
                .build()
        )
    }

    data class Result(
        val tableName: String,
        val spec: TypeSpec,
    )
}

private fun Table.generatedTableName(): String =
    name.lowercase().replaceFirstChar { it.uppercase() }
