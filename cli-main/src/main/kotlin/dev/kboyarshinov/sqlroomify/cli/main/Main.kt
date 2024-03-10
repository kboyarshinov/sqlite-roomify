package dev.kboyarshinov.sqlroomify.cli.main

import com.github.ajalt.clikt.core.CliktCommand

class Hello : CliktCommand() {
    override fun run() {
        echo("Hello sqlroomify!")
    }
}

fun main(args: Array<String>) = Hello().main(args)