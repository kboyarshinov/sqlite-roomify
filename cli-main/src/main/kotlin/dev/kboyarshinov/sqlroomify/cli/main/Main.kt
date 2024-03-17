package dev.kboyarshinov.sqlroomify.cli.main

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.boolean
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.path
import dev.kboyarshinov.sqlroomify.lib.SqlRoomify
import okio.Path.Companion.toOkioPath
import java.io.File
import java.nio.file.Path

class Run : CliktCommand() {
    val input: File by argument(help = "Input SQL Schema file").file(
        mustExist = true, mustBeReadable = true
    )
    val output: Path by argument(help = "Output folder for generated files").path(
        mustExist = false,
        canBeFile = false,
    )
    val packageName: String by argument(
        name = "package-name",
        help = "Package name to use for generated files"
    )
    val databaseName: String by argument(
        name = "database-name",
        help = "Name of generated database class"
    )

    val listAllIgnoredColumns: Boolean by option(
        help = "List all ignored columns in Entity's ignoredColumns parameter. 'true' by default."
    ).boolean().default(true)

    override fun run() {
        echo("Processing input: ${input.path}...")

        val result = SqlRoomify.sqlToRoom(
            input = input.toOkioPath(),
            outputDir = output.toOkioPath(),
            outputPackage = packageName,
            databaseName = databaseName,
            options = SqlRoomify.Options(
                listAllIgnoredColumns = listAllIgnoredColumns ?: true
            )
        )
        when (result) {
            is SqlRoomify.Success -> {
                echo("Success! Generated files to ${output.toFile().path}")
            }

            is SqlRoomify.Error -> {
                echo(result.message, err = true)
            }
        }
    }
}

fun main(args: Array<String>) = Run().main(args)