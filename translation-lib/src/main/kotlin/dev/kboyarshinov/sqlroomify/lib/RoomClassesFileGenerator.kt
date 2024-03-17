package dev.kboyarshinov.sqlroomify.lib

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import net.sf.jsqlparser.statement.Statement
import net.sf.jsqlparser.statement.create.index.CreateIndex
import net.sf.jsqlparser.statement.create.table.CreateTable
import okio.Path

internal class RoomDatabaseAndEntitiesFileGenerator(
    private val outputDir: Path,
    private val outputPackage: String
) {

    fun generate(
        databaseName: String,
        statements: List<Statement>,
        options: SqlRoomify.Options
    ): Result {
        val indices = statements.filterIsInstance<CreateIndex>()
        val tables = statements.filterIsInstance<CreateTable>()
            .map { statement ->
                val tableIndices: List<CreateIndex> =
                    indices.filter { it.table.name == statement.table.name }
                val indicesAnnotations = tableIndices.map(RoomIndexGenerator::toAnnotationSpec)
                RoomEntityGenerator.generateRoomEntity(
                    "$outputPackage.tables",
                    statement,
                    indicesAnnotations,
                    options
                )
            }

        tables.forEach { table ->
            table.typeSpec.writeToFile(table.tableName, packageSuffix = ".tables")
        }

        val databaseSpec =
            RoomDatabaseGenerator.generate(outputPackage, databaseName, tables.map { it.className })
        databaseSpec.writeToFile(databaseName)

        return Result(
            tableNames = tables.map { it.tableName }
        )
    }

    internal data class Result(
        val tableNames: List<String>
    )

    private fun TypeSpec.writeToFile(fileName: String, packageSuffix: String = "") {
        val fileBuilder = FileSpec.builder("$outputPackage$packageSuffix", fileName)
        fileBuilder.addType(this)
        val file = fileBuilder.build()
        file.writeTo(outputDir.toNioPath())
    }
}
