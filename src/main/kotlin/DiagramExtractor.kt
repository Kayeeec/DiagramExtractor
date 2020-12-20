import org.apache.batik.anim.dom.SVGDOMImplementation
import org.apache.batik.svggen.SVGGeneratorContext
import org.apache.batik.svggen.SVGGraphics2D
import org.apache.pdfbox.contentstream.operator.Operator
import org.apache.pdfbox.cos.COSDocument
import org.apache.pdfbox.cos.COSInteger
import org.apache.pdfbox.pdfparser.PDFStreamParser
import org.apache.pdfbox.pdfwriter.ContentStreamWriter
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.common.PDStream
import org.apache.pdfbox.rendering.PDFRenderer
import types.SearchedPage
import java.awt.Color
import java.io.File
import kotlin.math.max

class DiagramExtractor(val filePath: String = "testPage.pdf") {
    private val diagramPages = mutableListOf<DiagramPageResult>()
    private var maxDiagramPageNumber = 0
    private lateinit var fileName: String
    private lateinit var document: PDDocument

    private val paddingLength: Int
        get() = "${this.maxDiagramPageNumber}".length

    companion object {
        const val TEMPLATE_FILE = "template.pdf"
    }

    init {
        this.loadDocumentAndSetFileName()
    }

    fun extractDiagrams() {
        this.document.use { pdDocument: PDDocument? ->
            this.collectDiagramPages(document)
            for (dpage: DiagramPageResult in this.diagramPages) {
                this.maxDiagramPageNumber = max(maxDiagramPageNumber, dpage.pageNumber)
                this.extractDiagramFromPage(dpage)
            }
        }
    }

    private fun getCosIntegers(vararg nums: Long): Array<COSInteger> {
        return nums.map { COSInteger.get(it) }.toTypedArray()
    }

    private fun getPrependTokens(newHeight: Long?): List<Any> {
        val X = newHeight?.plus(72/2) ?: 842
        val q = Operator.getOperator("q")
        val cm = Operator.getOperator("cm")
        val g = Operator.getOperator("g")
        return listOf(
            q,
            *getCosIntegers(1, 0, 0, -1, 0, X), cm,
            q,
            *getCosIntegers(1, 0, 0, 1, 72, 0), cm,
            *getCosIntegers(0), g
        )
    }

    private fun getAppendTokens(): List<Any> {
        // TODO: 20.12.20 remove after testing on multiple diagrams
        val q = Operator.getOperator("q")
        val cm = Operator.getOperator("cm")
        val Q = Operator.getOperator("Q")
        return listOf(
            Q,
//            q,
//            *getCosIntegers(1, 0, 0, 1, 72, 770), cm,
//            Q,
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
     * crop it
     * convert it to svg
     */
    private fun extractDiagramFromPage(dpage: DiagramPageResult) {
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
                tokenWriter.writeTokens(getPrependTokens(dpage.searchedPage.diagramHeight?.toLong()))
                while (!(token is Operator && (token as Operator).name == "BT")) {
                    tokenWriter.writeTokens(token)
                    token = pdfStreamParser.parseNextToken()
                }
                tokenWriter.writeTokens(getAppendTokens())
            }
            val page = templateDoc.getPage(0)
            cropPage(page, dpage.searchedPage.diagramHeight)
            page.setContents(pdStream)
            convertPageToSvg(templateDoc, dpage.pageNumber)
        }
    }

    private fun convertPageToSvg(tempDoc: PDDocument, pageNumber: Int) {
        val namespace = SVGDOMImplementation.SVG_NAMESPACE_URI
        val impl = SVGDOMImplementation.getDOMImplementation()
        val doc = impl.createDocument(namespace, "svg", null)
        val ctx = SVGGeneratorContext.createDefault(doc)
        ctx.isEmbeddedFontsOn = true
        val svgFilename = "${this.fileName}_${pageNumber.toString().padStart(this.paddingLength, '0')}.svg"
        val svgGraphics2D = SVGGraphics2D(ctx, false)
        val pdfRenderer = PDFRenderer(tempDoc)
        svgGraphics2D.background = Color(255, 255, 255)
        pdfRenderer.renderPageToGraphics(0, svgGraphics2D)
        svgGraphics2D.stream(svgFilename)
    }

    private fun cropPage(page: PDPage, diagramHeight: Float?) {
        if (diagramHeight == null) return
        val newHeight = diagramHeight + (72/2)
        val width = page.mediaBox.width
        val pdRectangle = PDRectangle(width, newHeight)
        page.mediaBox = pdRectangle
        page.bleedBox = pdRectangle
        page.cropBox = pdRectangle
    }

    private fun loadDocumentAndSetFileName() {
        val file = File(javaClass.getResource(this.filePath).toURI())
        this.document = PDDocument.load(file) // TODO: 17.12.20 handle IO errors
        this.fileName = file.name
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
    val pageNumber: Int, // 1-based
    val pdPage: PDPage,
    val searchedPage: SearchedPage,
)
