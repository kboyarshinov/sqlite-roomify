package dev.kboyarshinov.sqliteroomify.lib

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
        options: SqliteRoomify.Options
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
        val className = ClassName(outputPackage, table)
        val entityBuilder = TypeSpec.classBuilder(className)
            .addModifiers(KModifier.DATA)

        val constructorBuilder = FunSpec.constructorBuilder()
        statement.columnDefinitions
            .map(SqliteToRoomColumnDataTypeConverter::toRoomColumn)
            // sort to put ignored types at the end
            .sortedWith { t1, t2 ->
                when {
                    t1::class == t2::class -> 0
                    t2::class.java == SqliteToRoomColumnDataTypeConverter.IgnoredType::class.java -> -1
                    else -> 1
                }
            }
            .forEach { type ->
                when (type) {
                    is SqliteToRoomColumnDataTypeConverter.SupportedType -> {
                        buildSupportedColumnSpec(type, constructorBuilder, entityBuilder)
                    }

                    is SqliteToRoomColumnDataTypeConverter.UnsupportedType -> {
                        ignoredColumns.add(type.columnName)
                    }

                    is SqliteToRoomColumnDataTypeConverter.IgnoredType -> {
                        buildIgnoredColumnSpec(type, constructorBuilder, entityBuilder)
                        if (options.listAllIgnoredColumns) {
                            ignoredColumns.add(type.columnName)
                        }
                    }
                }
            }
        if (ignoredColumns.isNotEmpty()) {
            entityAnnotation.addMember(
                arrayCodeBlockMember("ignoredColumns", ignoredColumns)
            )
        }
        entityBuilder.addAnnotation(entityAnnotation.build())
        entityBuilder.primaryConstructor(constructorBuilder.build())

        return Result(
            tableName = table,
            typeSpec = entityBuilder.build(),
            className = className,
        )
    }

    private fun buildSupportedColumnSpec(
        type: SqliteToRoomColumnDataTypeConverter.SupportedType,
        constructorBuilder: FunSpec.Builder,
        entity: TypeSpec.Builder
    ) {
        val columnName = type.columnName.lowercase()
        val parameterBuilder = ParameterSpec.builder(columnName, type.typeName)
        if (type.primaryKey) {
            parameterBuilder.addAnnotation(RoomClasses.primaryKeyAnnotation)
        }
        parameterBuilder.addAnnotation(
            AnnotationSpec.builder(RoomClasses.columnInfoAnnotation)
                .addMember("name = %S", type.columnName)
                .addMember("typeAffinity = %T", type.typeAffinity.roomClassName())
                .build()
        )
        constructorBuilder.addParameter(parameterBuilder.build())
        entity.addProperty(
            PropertySpec
                .builder(columnName, type.typeName)
                .initializer(columnName)
                .build()
        )
    }

    private fun buildIgnoredColumnSpec(
        type: SqliteToRoomColumnDataTypeConverter.IgnoredType,
        constructorBuilder: FunSpec.Builder,
        entity: TypeSpec.Builder
    ) {
        val columnName = type.columnName.lowercase()
        // ignored types should always be nullable
        val typeName = type.typeName.copy(nullable = true)
        val parameterBuilder = ParameterSpec.builder(columnName, typeName)
            .defaultValue("null")
            .addAnnotation(
                AnnotationSpec.builder(RoomClasses.ignoreAnnotation)
                    .build()
            )
        constructorBuilder.addParameter(parameterBuilder.build())
        entity.addProperty(
            PropertySpec
                .builder(columnName, typeName)
                .initializer(columnName)
                .build()
        )
    }

    data class Result(
        val tableName: String,
        val typeSpec: TypeSpec,
        val className: ClassName
    )
}

private fun Table.generatedTableName(): String =
    name.lowercase().replaceFirstChar { it.uppercase() }

private fun TypeAffinity.roomClassName(): ClassName = when (this) {
    TypeAffinity.INTEGER -> RoomClasses.columnInfoTypeAffinityInteger
    TypeAffinity.TEXT -> RoomClasses.columnInfoTypeAffinityText
    TypeAffinity.BLOB -> RoomClasses.columnInfoTypeAffinityBlob
    TypeAffinity.REAL -> RoomClasses.columnInfoTypeAffinityReal
    // Room doesn't support NUMERIC affinity
    TypeAffinity.NUMERIC,
    TypeAffinity.UNKNOWN -> RoomClasses.columnInfoTypeAffinityUndefined
}