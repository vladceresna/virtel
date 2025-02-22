package dev.snipme.highlights.internal.locator

import dev.snipme.highlights.internal.SyntaxTokens.COMMENT_DELIMITERS
import dev.snipme.highlights.internal.indicesOf
import dev.snipme.highlights.internal.lengthToEOF
import dev.snipme.highlights.model.PhraseLocation

internal object CommentLocator {

    fun locate(code: String): Set<PhraseLocation> {
        val locations = mutableListOf<PhraseLocation>()
        val indices = mutableListOf<Int>()
        COMMENT_DELIMITERS.forEach { delimiter ->
            indices.addAll(code.indicesOf(delimiter))
        }

        indices.forEach { start ->
            val end = start + code.lengthToEOF(start)
            locations.add(PhraseLocation(start, end))
        }

        return locations.toSet()
    }
}