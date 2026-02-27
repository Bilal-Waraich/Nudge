package bwa.nla.up

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Untitledproject",
    ) {
        App()
    }
}