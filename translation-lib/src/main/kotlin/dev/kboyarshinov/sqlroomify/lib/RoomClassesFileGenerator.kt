package dev.kboyarshinov.sqlroomify.lib

import com.squareup.kotlinpoet.FileSpec
import net.sf.jsqlparser.statement.Statement
import net.sf.jsqlparser.statement.create.index.CreateIndex
import net.sf.jsqlparser.statement.create.table.CreateTable
import okio.Path

internal class RoomEntitiesFileGenerator(
    private val outputDir: Path,
    private val outputPackage: String
) {

    fun generate(statements: List<Statement>): Result {
        val fileBuilder = FileSpec.builder(outputPackage, "Tables")
        val indices = statements.filterIsInstance<CreateIndex>()
        val tables = statements.filterIsInstance<CreateTable>().map { statement ->
            val tableIndices: List<CreateIndex> =
                indices.filter { it.table.name == statement.table.name }
            val indicesAnnotations = tableIndices.map(RoomIndexGenerator::toAnnotationSpec)
            val entityResult =
                RoomEntityGenerator.generateRoomEntity(outputPackage, statement, indicesAnnotations)
            fileBuilder.addType(entityResult.spec)
            entityResult.tableName
        }

        val file = fileBuilder.build()
        file.writeTo(outputDir.toNioPath()).toFile()

        return Result(
            tableNames = tables
        )
    }

    internal data class Result(
        val tableNames: List<String>
    )
}
