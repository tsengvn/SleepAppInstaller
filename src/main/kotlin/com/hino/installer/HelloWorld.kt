package com.hino.installer

import javafx.event.EventType
import javafx.stage.FileChooser
import tornadofx.*

class HelloWorld : View() {
    override val root = vbox {
        label("Hello world")
        label("Hello world2")
        button("APK").setOnAction {
            chooseFile(
                    title = "Open APK file",
                    filters = arrayOf(FileChooser.ExtensionFilter("Android Package files", "*.apk")),
                    op = {

                    }
            )
        }
    }
}