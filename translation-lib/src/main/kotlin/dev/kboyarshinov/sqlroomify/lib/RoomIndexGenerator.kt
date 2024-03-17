package dev.kboyarshinov.sqlroomify.lib

import com.squareup.kotlinpoet.AnnotationSpec
import net.sf.jsqlparser.statement.create.index.CreateIndex

internal object RoomIndexGenerator {
    fun toAnnotationSpec(createIndex: CreateIndex): AnnotationSpec =
        AnnotationSpec.builder(RoomClasses.indexAnnotation)
            .addMember("name = %S", createIndex.index.name)
            .addMember(
                arrayCodeBlockMember(
                    "value",
                    createIndex.index.columns.map { it.columnName })
            )
            .addMember("unique = %L", createIndex.isUnique())
            .build()
}