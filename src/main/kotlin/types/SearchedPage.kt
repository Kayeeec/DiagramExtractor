package types

import utils.END_HEADER
import utils.END_HEADER_FONT_SIZE
import utils.NEXT_START_HEADER_FONT_SIZE
import utils.START_HEADER
import utils.START_HEADER_FONT_SIZE

class SearchedPage {
    val lines: MutableList<MutableList<TextPositionSequence>> = mutableListOf()
    private val startHeaderLineHits: MutableSet<Int> = mutableSetOf()
    private val endHeaderLineHits: MutableSet<Int> = mutableSetOf()
    /* at most one */
    val startHeaderLineNumbers: List<Int> //at least one for diagram page
        get() = this.startHeaderLineHits.toList().sorted()
    /* at most one */
    val endHeaderLineNumbers: List<Int> //at most one for diagram page
        get() = this.endHeaderLineHits.toList().sorted()

    fun addLine(lineNumber: Int, sequence: TextPositionSequence) {
        if (this.lines.lastIndex < lineNumber) {
            this.lines.add(mutableListOf(sequence))
        } else {
            this.lines[lineNumber].add(sequence)
        }
    }

    fun addLineHit(term: String, lineNumber: Int, fontSize: Float?) {
        if (term == START_HEADER
            && fontSize != null && fontSize.equals(START_HEADER_FONT_SIZE)) this.startHeaderLineHits.add(lineNumber)
        if (term == END_HEADER
            && fontSize != null && fontSize.equals(END_HEADER_FONT_SIZE)) this.endHeaderLineHits.add(lineNumber)
    }

    val startHeaderLineNumber: Int? //there should be only one hit and it also should be always 0
        get() = startHeaderLineNumbers.firstOrNull { lineId ->
            this.lines[lineId].isNotEmpty()
                    && this.lines[lineId + 1].isNotEmpty()
                    && this.lines[lineId + 1][0].fontSize != null
                    && this.lines[lineId + 1][0].fontSize!!.equals(NEXT_START_HEADER_FONT_SIZE)
        }

    val isDiagramPage: Boolean
        get() = this.startHeaderLineNumber != null
}
