import org.apache.pdfbox.pdmodel.PDDocument
import java.io.File

class DocumentLoader(val fileName: String = "testPage.pdf") {
    var document: PDDocument
    init {
        this.document = PDDocument.load(File(javaClass.getResource(this.fileName).toURI()))
    }
}
