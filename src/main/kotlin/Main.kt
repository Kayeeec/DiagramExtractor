import java.io.File

fun main() {
    println("Extractor running...")

    val testFilesFolderPath = "test_files/"
    val testFiles = File(testFilesFolderPath).list()
    testFiles?.forEach {
        val filePath = "${testFilesFolderPath}${it}"
        print("   Extracting diagrams from $filePath...")
        val extractor = DiagramExtractor(filePath)
        extractor.extractDiagrams()
        println(" done.")
    }

    println("Extractor finished.")
}

