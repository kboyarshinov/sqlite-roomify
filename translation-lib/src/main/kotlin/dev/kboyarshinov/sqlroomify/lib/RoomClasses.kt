package dev.kboyarshinov.sqlroomify.lib

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import net.sf.jsqlparser.schema.Table
import net.sf.jsqlparser.statement.create.index.CreateIndex
import net.sf.jsqlparser.statement.create.table.ColumnDefinition

internal object RoomClasses {
    val entityAnnotation = ClassName("androidx.room", "Entity")
    val primaryKeyAnnotation = ClassName("androidx.room", "PrimaryKey")
    val columnInfoAnnotation = ClassName("androidx.room", "ColumnInfo")
    val indexAnnotation = ClassName("androidx.room", "Index")
}
