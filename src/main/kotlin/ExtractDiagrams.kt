import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage

class ExtractDiagrams(val document: PDDocument) {
    companion object {
        const val START_HEADING = "Diagram"
        const val END_HEADING = "Summary"
    }

    fun collectDiagramPages(document: PDDocument): List<Any> {
        val pageNumbers = mutableSetOf<Int>()
        val pages = mutableListOf<PDPage>()
        var pageIndex = 1
        for (page: PDPage in document.pages) {


            pageIndex += 1
        }
    }
}

