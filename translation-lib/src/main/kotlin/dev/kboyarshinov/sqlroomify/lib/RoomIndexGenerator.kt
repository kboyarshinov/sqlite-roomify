package dev.kboyarshinov.sqlroomify.lib

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock
import net.sf.jsqlparser.statement.create.index.CreateIndex

internal object RoomIndexGenerator {
    fun toAnnotationSpec(createIndex: CreateIndex): AnnotationSpec =
        AnnotationSpec.builder(RoomClasses.indexAnnotation)
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