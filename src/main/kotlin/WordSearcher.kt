import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.pdfbox.text.TextPosition
import java.io.IOException

/* not working */

/**
 * from this https://stackoverflow.com/a/35987635
 */

// TODO: 11.12.20 rework into DiagramPageHeadingFinder
class WordSearcher() {

    fun findSubwords(document: PDDocument, page: Int, searchTerm: String): List<TextPositionSequence> {
        val hits: MutableList<TextPositionSequence> = mutableListOf()

        val stripper: PDFTextStripper = object : PDFTextStripper() {
            @Throws(IOException::class)
            override fun writeString(text: String, textPositions: List<TextPosition>) {
                val word = TextPositionSequence(textPositions)
                val string = word.toString()
                println("word: $word, string: $string")
                var fromIndex = 0
                var index = string.indexOf(searchTerm, fromIndex)
                while (index > -1) {
                    hits.add(word.subSequence(index, index + searchTerm.length))
                    fromIndex = index + 1
                    index = string.indexOf(searchTerm, fromIndex)
                }
                super.writeString(text, textPositions)
            }
        }

        stripper.sortByPosition = true
        stripper.startPage = page
        stripper.endPage = page
        stripper.getText(document)
        return hits
    }

    fun findSubwords(document: PDDocument, page: Int, searchTerms: List<String>)
    : HashMap<String, MutableList<TextPositionSequence>> {
        val hits = hashMapOf<String, MutableList<TextPositionSequence>>()

        val stripper: PDFTextStripper = object : PDFTextStripper() {
            @Throws(IOException::class)
            override fun writeString(text: String, textPositions: List<TextPosition>) {
                val word = TextPositionSequence(textPositions)
                val string = word.toString()

                for (searchTerm in searchTerms) {
                    if (!hits.containsKey(searchTerm)) hits[searchTerm] = mutableListOf()
                    var fromIndex = 0
                    var index = string.indexOf(searchTerm, fromIndex)
                    while (index > -1) {
                        hits[searchTerm]!!.add(word.subSequence(index, index + searchTerm.length))
                        fromIndex = index + 1
                        index = string.indexOf(searchTerm, fromIndex)
                    }
                }
                super.writeString(text, textPositions)
            }
        }

        stripper.sortByPosition = true
        stripper.startPage = page
        stripper.endPage = page
        stripper.getText(document)
        return hits
    }
}

