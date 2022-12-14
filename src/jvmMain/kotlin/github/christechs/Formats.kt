package github.christechs

import com.aspose.pdf.SaveFormat

enum class Formats(val extention: String, val saveFormat: SaveFormat) {

    EXCEL("xls", SaveFormat.Excel),
    CSV("csv", SaveFormat.Excel),
    XLSX("xlsx", SaveFormat.Excel),
    HTML("html", SaveFormat.Html),
    WORD("doc", SaveFormat.Doc),
    POWERPOINT("pptx", SaveFormat.Pptx),
    EBOOK("epub", SaveFormat.Epub),

}