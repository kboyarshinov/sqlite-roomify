package dev.kboyarshinov.sqlroomify.lib

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec

internal object RoomDatabaseGenerator {
    fun generate(
        outputPackage: String,
        databaseName: String,
        entities: List<ClassName>
    ): TypeSpec {
        val databaseBuilder = TypeSpec.classBuilder(ClassName(outputPackage, databaseName))
            .addModifiers(KModifier.ABSTRACT)
            .superclass(RoomClasses.roomDatabase)
        databaseBuilder.addAnnotation(
            AnnotationSpec.builder(RoomClasses.databaseAnnotation)
                .addMember(
                    arrayCodeBlockMember(
                        "entities",
                        entities.map { it },
                        itemFormat = "%T::class",
                        addLineBreaks = true
                    )
                )
                .addMember("version = %L", 1)
                .build()
        )

        return databaseBuilder.build()
    }
}