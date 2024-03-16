package dev.kboyarshinov.sqlroomify.lib

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
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
                    parameterBuilder.addAnnotation(RoomClasses.primaryKeyAnnotation)
                }
                parameterBuilder.addAnnotation(
                    AnnotationSpec.builder(RoomClasses.columnInfoAnnotation)
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
                ignoredColumns.add("$table.$columnName")
            }
        }
        entity.primaryConstructor(constructorBuilder.build())

        return Result(
            tableName = table,
            spec = entity.build(),
            ignoredColumns = ignoredColumns,
        )
    }

    data class Result(
        val tableName: String,
        val spec: TypeSpec,
        val ignoredColumns: List<String>
    )
}