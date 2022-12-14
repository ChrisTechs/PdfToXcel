package github.christechs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.aspose.pdf.Document
import com.aspose.pdf.ExcelSaveOptions
import kotlinx.coroutines.*
import java.awt.FileDialog
import java.awt.Toolkit
import java.io.File
import java.io.FilenameFilter
import javax.swing.JFrame
import javax.swing.UIManager

object PDFConverter {
    lateinit var files: MutableState<Array<File>>
    lateinit var file: MutableState<String>

    val fileDialog = FileDialog(
        Main.composeWindow
            .also {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
                it.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
                it.toFront()
            }, "Choose PDF File"
    ).also {
        it.isVisible = false
        it.isMultipleMode = true
        it.filenameFilter = FilenameFilter { _, s ->
            s.endsWith("pdf", true)
        }
    }

    val fontFamily = FontFamily(
        Font(
            resource = "Roboto-Medium.ttf",
            weight = FontWeight.Normal,
            style = FontStyle.Normal
        )
    )

    val graySurface = Color(0xFF2A2A2A)
    val lightGray = Color(0xFFD3D3D3)
    val green700 = Color(0xff388e3c)
    val slackBlack = Color(0xff1E2228)
    val darkGray = Color(0xFF565656)

    val DarkColorPalette = darkColors(
        primary = green700,
        primaryVariant = darkGray,
        secondary = graySurface,
        background = slackBlack,
        surface = slackBlack,
        onPrimary = slackBlack,
        onSecondary = lightGray,
        onBackground = Color.White,
        onSurface = Color.White,
        error = Color.Red,
    )

    private var selection = Formats.EXCEL

    @OptIn(DelicateCoroutinesApi::class)
    @Composable
    fun PDFConverter() {

        file = remember { mutableStateOf("") }
        files = remember { mutableStateOf(emptyArray()) }

        val screenSize = Toolkit.getDefaultToolkit().screenSize

        var popup by remember { mutableStateOf(false) }
        var message by remember { mutableStateOf("") }
        var title by remember { mutableStateOf("") }
        var okButton by remember { mutableStateOf(false) }
        var canClose = true

        if (popup) {
            Popup(
                popupPositionProvider = WindowCenterOffsetPositionProvider(),
                onDismissRequest = { popup = false }
            ) {
                Window(
                    onCloseRequest = {
                        if (canClose)
                            popup = false
                    },
                    title = title,
                    state = WindowState(
                        WindowPlacement.Floating,
                        false,
                        WindowPosition(Alignment.Center),
                        DpSize((screenSize.width / 5f).dp, (screenSize.height / 5f).dp)
                    ),
                    resizable = true
                ) {
                    MaterialTheme(colors = DarkColorPalette, typography = Typography(defaultFontFamily = fontFamily)) {

                        Box(modifier = Modifier.background(slackBlack).fillMaxSize()) {

                            Column(
                                modifier = Modifier.fillMaxSize().align(Alignment.Center),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                Text(message, color = Color.White)

                                Spacer(Modifier.fillMaxSize(0.03f))

                                Button(onClick = {
                                    popup = false
                                }, enabled = okButton) { Text("Ok", color = Color.White) }

                            }

                        }

                    }
                }
            }
        }

        MaterialTheme(colors = DarkColorPalette, typography = Typography(defaultFontFamily = fontFamily)) {

            Box(modifier = Modifier.background(slackBlack).fillMaxSize()) {

                Column(
                    modifier = Modifier.fillMaxSize().align(Alignment.Center),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {

                        OutlinedTextField(
                            file.value, {
                                file.value = it
                                files.value = arrayOf(File(file.value))
                            },
                            label = { Text("Files", color = Color.White) }, trailingIcon = {
                                IconButton(onClick = {
                                    fileDialog.isVisible = true
                                    files.value = fileDialog.files ?: emptyArray()
                                    if (files.value.size <= 1) {
                                        file.value = "${fileDialog.directory ?: ""}${fileDialog.file ?: ""}"
                                    } else {
                                        file.value = "Multiple Files Selected"
                                    }
                                }) {
                                    Icon(loadSvg("file-picker.svg", Density(0.8f)), "")
                                }

                            }, singleLine = true
                        )

                    }

                    Spacer(Modifier.fillMaxSize(0.03f))

                    Row {

                        var down by remember { mutableStateOf(false) }

                        var selected by remember { mutableStateOf(selection.name) }

                        TextButton({ down = true }) {

                            Row {

                                Text("Convert To: ", color = Color.White)

                                Spacer(Modifier.width(3f.dp))

                                Text(selected)

                            }

                            DropdownMenu(down, onDismissRequest = {
                                down = !down
                            }, true) {

                                for (format in Formats.values()) {

                                    DropdownMenuItem({

                                        selection = format
                                        selected = selection.name
                                        down = false

                                    }) {
                                        Text(format.name)
                                    }

                                }

                            }

                        }

                    }

                    Row {

                        var converting = false
                        var totalConverted = 0

                        @Synchronized
                        fun add() {
                            totalConverted++
                        }

                        Button({

                            if (converting) return@Button
                            totalConverted = 0

                            for (file in files.value) {
                                if (!file.exists() || file.isDirectory) {
                                    title = "Error"
                                    message = "${file.absoluteFile} does not exist"
                                    okButton = true
                                    canClose = true
                                    popup = true
                                    return@Button
                                }
                            }

                            title = "Converting Files"
                            message = "Converting $totalConverted/${files.value.size}"
                            converting = true
                            okButton = false
                            canClose = false
                            popup = true

                            GlobalScope.launch {

                                val jobs: MutableList<Job> = mutableListOf()

                                for (file in files.value) {
                                    jobs.add(launch(Dispatchers.IO) {

                                        val doc = Document(file.absolutePath)

                                        val savedFile = File(
                                            file.parent,
                                            "${file.nameWithoutExtension}.${selection.extention}"
                                        ).absolutePath

                                        when (selection) {
                                            Formats.EXCEL -> {

                                                val excelSave = ExcelSaveOptions()

                                                doc.save(savedFile, excelSave)

                                            }

                                            Formats.CSV -> {

                                                val excelSave = ExcelSaveOptions()
                                                excelSave.format = ExcelSaveOptions.ExcelFormat.CSV

                                                doc.save(savedFile, excelSave)

                                            }

                                            Formats.XLSX -> {

                                                val excelSave = ExcelSaveOptions()
                                                excelSave.format = ExcelSaveOptions.ExcelFormat.XLSX

                                                doc.save(savedFile, excelSave)

                                            }

                                            else -> {
                                                doc.save(savedFile, selection.saveFormat)
                                            }
                                        }

                                        add()

                                    })

                                }

                                for (job in jobs) {
                                    job.join()
                                }

                                converting = false
                                title = "Finished"
                                message = "Converted $totalConverted/${files.value.size}"
                                converting = true
                                okButton = true
                                canClose = true
                                popup = true

                            }

                        }) {
                            Text("Convert")
                        }

                    }

                }

            }

        }

    }

    fun loadSvg(resource: String, density: Density): Painter =
        ClassLoader.getSystemClassLoader().getResourceAsStream(resource)!!
            .buffered().use { loadSvgPainter(it, density) }
}