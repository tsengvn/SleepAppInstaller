package com.hino.installer

import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.scene.control.TextField
import tornadofx.View
import tornadofx.chooseFile
import java.io.File



class MainView : View() {
    override val root : VBox by fxml()
    val adbPathInput : TextField by fxid("input_adb_path")
    val apkPathInput : TextField by fxid("input_apk_path")
    val apkPathButton : Label by fxid("btn_apk_path")
    val installButton : Button by fxid("btn_install")
    val logText : TextArea by fxid("input_log")

    init {
        adbPathInput.text = getDefaultADBFolder()
        apkPathButton.setOnMouseClicked {
            val files = chooseFile(
                    title = "Open APK file",
                    filters = arrayOf(FileChooser.ExtensionFilter("Android Package files", "*.apk"))
            )

            if (!files.isEmpty()) {
                apkPathInput.text = files[0].absolutePath.toString()
            }
        }

        installButton.setOnAction {

        }
    }

    fun getCurrentFolder() : String {
        return File(MainView::class.java.protectionDomain.codeSource.location.toURI()).path
    }

    fun getDefaultADBFolder() : String {
        val os = System.getProperty("os.name").toLowerCase()
        if (os.contains("mac")) {
            return "${getCurrentFolder()}/mac"
        } else {
            return "${getCurrentFolder()}/win"
        }
    }

}