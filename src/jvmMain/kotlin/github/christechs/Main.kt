package github.christechs

import androidx.compose.ui.Alignment
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import java.awt.Toolkit

object Main {

    lateinit var composeWindow: ComposeWindow
        private set

    @JvmStatic
    fun main(args: Array<String>) = application {

        val screenSize = Toolkit.getDefaultToolkit().screenSize

        Window(
            onCloseRequest = {
                exitApplication()
            },
            title = "PDF Converter",
            state = WindowState(
                WindowPlacement.Floating,
                false,
                WindowPosition(Alignment.Center),
                DpSize((screenSize.width / 1.25f).dp, (screenSize.height / 1.25f).dp)
            ),
            resizable = true
        ) {

            composeWindow = this.window

            PDFConverter.PDFConverter()

        }

    }

}