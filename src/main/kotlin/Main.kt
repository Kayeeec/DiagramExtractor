import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.pdfbox.text.TextPosition
import java.io.File
import java.io.IOException
import java.util.*


fun main() {
    println("Extractor running...")
    val document = DocumentLoader().document
    document.use { document ->
        val wordSearcher = WordSearcher()
        val findSubwords = wordSearcher.findSubwords(document, 1, listOf("Diagram", "Summary"))
        println(findSubwords)
    }
    println("Extractor finished.")
}


