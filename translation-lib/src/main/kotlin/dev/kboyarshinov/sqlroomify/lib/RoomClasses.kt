package dev.kboyarshinov.sqlroomify.lib

import com.squareup.kotlinpoet.ClassName

internal object RoomClasses {
    val entityAnnotation = ClassName("androidx.room", "Entity")
    val primaryKeyAnnotation = ClassName("androidx.room", "PrimaryKey")
    val columnInfoAnnotation = ClassName("androidx.room", "ColumnInfo")
    val indexAnnotation = ClassName("androidx.room", "Index")
}
