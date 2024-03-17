package dev.kboyarshinov.sqlroomify.lib

import com.squareup.kotlinpoet.CodeBlock

internal fun arrayCodeBlockMember(
    name: String,
    data: Collection<Any>,
    itemFormat: String = "%S",
    addLineBreaks: Boolean = false,
): CodeBlock =
    CodeBlock.of(
        "$name = %L",
        CodeBlock.builder().apply {
            add("[⇥")
            if (addLineBreaks) add("\n")
            data.forEachIndexed { index, item ->
                if (index == data.size - 1) {
                    add(itemFormat, item)
                } else {
                    add("$itemFormat,", item)
                }
                if (addLineBreaks) add("\n")
            }
            add("⇤]")
        }.build()
    )