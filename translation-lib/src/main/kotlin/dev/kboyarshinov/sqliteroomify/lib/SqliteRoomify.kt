package dev.kboyarshinov.sqliteroomify.lib

import net.sf.jsqlparser.JSQLParserException
import net.sf.jsqlparser.parser.CCJSqlParserUtil
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.buffer
import okio.use
import java.util.concurrent.Executors

public object SqliteRoomify {

    public fun sqliteToRoom(
        input: Path,
        outputDir: Path,
        outputPackage: String,
        databaseName: String,
        options: Options = Options()
    ): Result {
        return try {
            FileSystem.SYSTEM.source(input).use { source ->
                source.buffer().use { bufferedSource ->
                    val stream = bufferedSource.inputStream()
                    val parser = CCJSqlParserUtil.newParser(stream)
                    val singleThreadExecutor = Executors.newSingleThreadExecutor()
                    val statements = CCJSqlParserUtil.parseStatements(
                        parser,
                        singleThreadExecutor
                    )
                    singleThreadExecutor.shutdownNow()

                    val generator = RoomDatabaseAndEntitiesFileGenerator(outputDir, outputPackage)

                    val result = generator.generate(databaseName, statements, options)
                    Success(
                        tablesCount = result.tableNames.count()
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

    public data class Options(
        val listAllIgnoredColumns: Boolean = true
    )
}