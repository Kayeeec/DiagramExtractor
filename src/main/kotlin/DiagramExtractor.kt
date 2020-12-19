import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import types.SearchedPage
import java.io.File
import java.io.InputStream

class DiagramExtractor(val fileName: String = "testPage.pdf") {
    val diagramPages = mutableListOf<DiagramPageResult>()

    fun extractDiagrams() {
        val document = this.loadDocument()
        document.use { pdDocument: PDDocument? ->
            this.collectDiagramPages(document)
            for (dpage: DiagramPageResult in this.diagramPages) {
                this.extractDiagram(dpage)
            }
        }
    }

    private fun loadDocument(): PDDocument {
        return PDDocument.load(File(javaClass.getResource(this.fileName).toURI())) // TODO: 17.12.20 handle IO errors
    }

    private fun extractDiagram(dpage: DiagramPageResult) {
        val contents: InputStream = dpage.pdPage.contents
        val contentsStr = contents.toString()

//        TODO("Not yet implemented")
    }

    private fun collectDiagramPages(document: PDDocument) {
        var pageIndex = 1
        for (page: PDPage in document.pages) {
            val searchedPage = searchPageForDiagramHeadings(document, pageIndex)
            if (searchedPage.isDiagramPage) {
                this.diagramPages.add(DiagramPageResult(pageIndex, page, searchedPage))
            }
            pageIndex += 1
        }
    }
}

data class DiagramPageResult(
    val pageNumber: Int, // 1-based, // todo needed?
    val pdPage: PDPage,
    val searchedPage: SearchedPage,
)
