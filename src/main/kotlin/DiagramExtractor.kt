import org.apache.pdfbox.contentstream.operator.Operator
import org.apache.pdfbox.cos.COSDocument
import org.apache.pdfbox.cos.COSInteger
import org.apache.pdfbox.pdfparser.PDFStreamParser
import org.apache.pdfbox.pdfwriter.ContentStreamWriter
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.common.PDStream
import types.SearchedPage
import java.io.File

class DiagramExtractor(val fileName: String = "testPage.pdf") {
    val diagramPages = mutableListOf<DiagramPageResult>()

    companion object {
        const val TEMPLATE_FILE = "template.pdf"
    }

    fun extractDiagrams() {
        val document = this.loadDocument()
        document.use { pdDocument: PDDocument? ->
            this.collectDiagramPages(document)
            for (dpage: DiagramPageResult in this.diagramPages) {
                this.extractDiagram(dpage)
            }
        }
    }

    private fun getCosIntegers(vararg nums: Long): Array<COSInteger> {
        return nums.map { COSInteger.get(it) }.toTypedArray()
    }

    private fun getPrependTokens(): List<Any> {
        val q = Operator.getOperator("q")
        val cm = Operator.getOperator("cm")
        val g = Operator.getOperator("g")
        return listOf(
            q,
            *getCosIntegers(1, 0, 0, -1, 0, 842), cm,
            q,
            *getCosIntegers(1, 0, 0, 1, 72, 0), cm,
            *getCosIntegers(0), g
        )
    }

    private fun getAppendTokens(): List<Any> {
        val q = Operator.getOperator("q")
        val cm = Operator.getOperator("cm")
        val Q = Operator.getOperator("Q")
        return listOf(
            Q,
            q,
            *getCosIntegers(1, 0, 0, 1, 72, 770), cm,
            Q,
            Q
        )
    }

    /**
     * todo make more effective? (lots of lines with lots of operators, can be done later)
     *
     * getting diagram stream:
     * read until first ET
     * then buffer diagram operators until next BT
     * create pdf page with the diagram operators
     * crop it // todo
     * convert it to svg/png //todo
     */
    private fun extractDiagram(dpage: DiagramPageResult) {
        // collect diagram tokens
        val pdfStreamParser = PDFStreamParser(dpage.pdPage)
        var token = pdfStreamParser.parseNextToken()
        while (!(token is Operator && token.name == "ET")) {
            token = pdfStreamParser.parseNextToken()
        }
        token = pdfStreamParser.parseNextToken()

        // using pdf template
        val tmpDoc = PDDocument(this.loadTemplateDocument())
        tmpDoc.use { templateDoc ->
            // write diagram tokens to page stream
            val pdStream = PDStream(templateDoc)
            val out = pdStream.createOutputStream()
            out.use {
                val tokenWriter = ContentStreamWriter(it)
                tokenWriter.writeTokens(getPrependTokens())
                while (!(token is Operator && (token as Operator).name == "BT")) {
                    tokenWriter.writeTokens(token)
                    token = pdfStreamParser.parseNextToken()
                }
                tokenWriter.writeTokens(getAppendTokens())
            }
            val page = templateDoc.getPage(0)
            page.setContents(pdStream)

            // for now just save into a new doc and check //todo remove
            templateDoc.save("${this.fileName}_page_${dpage.pageNumber}.pdf")



            // crop page
            // TODO: 19.12.20
            //convert page into svg/png
            // TODO: 19.12.20
        }
    }

    private fun loadDocument(): PDDocument {
        return PDDocument.load(File(javaClass.getResource(this.fileName).toURI())) // TODO: 17.12.20 handle IO errors
    }

    private fun loadTemplateDocument(): COSDocument {
        return PDDocument.load(File(javaClass.getResource(TEMPLATE_FILE).toURI())).document // TODO: 17.12.20 handle IO errors
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
