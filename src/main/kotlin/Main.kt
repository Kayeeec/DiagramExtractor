fun main() {
    println("Extractor running...")
//    val document = utils.DocumentLoader().document
//    document.use { document ->
//        val findLinesContainingWords = searchPageForDiagramHeadings(document, 1)
//        print(findLinesContainingWords)
//    }
    val extractDiagrams = DiagramExtractor()
    extractDiagrams.extractDiagrams()
    println("Extractor finished.")
}


