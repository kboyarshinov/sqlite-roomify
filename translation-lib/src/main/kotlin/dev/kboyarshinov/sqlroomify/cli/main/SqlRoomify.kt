package dev.kboyarshinov.sqlroomify.cli.main

import net.sf.jsqlparser.JSQLParserException
import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.statement.create.table.CreateTable
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.buffer
import okio.use
import java.util.concurrent.Executors

public object SqlRoomify {

    public fun sqlToRoom(input: Path, outputDir: Path): Result {
        return try {
            FileSystem.SYSTEM.source(input).use { source ->
                source.buffer().use { bufferedSource ->
                    val stream = bufferedSource.inputStream()
                    val parser = CCJSqlParserUtil.newParser(stream)
                    val statements = CCJSqlParserUtil.parseStatements(parser, Executors.newSingleThreadExecutor())

                    val createTableStatementsCount = statements.count {
                        it is CreateTable
                    }
                    Success(
                        tablesCount = createTableStatementsCount
                    )
                }
            }
        } catch (e: IOException) {
            Error("IO Error: ${e.message}")
        } catch (e: JSQLParserException) {
            Error("Parser error: ${e.message}")
        }
    }

    public sealed interface Result

    public class Success(
        public val tablesCount: Int
    ) : Result
    public class Error(public val message: String) : Result
}