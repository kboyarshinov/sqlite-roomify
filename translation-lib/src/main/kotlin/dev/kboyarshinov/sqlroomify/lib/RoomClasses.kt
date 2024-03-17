package dev.kboyarshinov.sqlroomify.lib

import com.squareup.kotlinpoet.ClassName

internal object RoomClasses {
    val entityAnnotation = ClassName("androidx.room", "Entity")
    val primaryKeyAnnotation = ClassName("androidx.room", "PrimaryKey")
    val ignoreAnnotation = ClassName("androidx.room", "Ignore")
    val columnInfoAnnotation = ClassName("androidx.room", "ColumnInfo")
    val columnInfoTypeAffinityUndefined = ClassName("androidx.room", "ColumnInfo", "UNDEFINED")
    val columnInfoTypeAffinityText = ClassName("androidx.room", "ColumnInfo", "TEXT")
    val columnInfoTypeAffinityInteger = ClassName("androidx.room", "ColumnInfo", "INTEGER")
    val columnInfoTypeAffinityReal = ClassName("androidx.room", "ColumnInfo", "REAL")
    val columnInfoTypeAffinityBlob = ClassName("androidx.room", "ColumnInfo", "BLOB")
    val indexAnnotation = ClassName("androidx.room", "Index")
    val databaseAnnotation = ClassName("androidx.room", "Database")
    val roomDatabase = ClassName("androidx.room", "RoomDatabase")
}
