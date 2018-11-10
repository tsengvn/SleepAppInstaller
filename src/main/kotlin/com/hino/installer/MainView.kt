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
import java.lang.Exception
import javax.annotation.processing.FilerException

class MainView : View() {
    override val root : VBox by fxml()
    val adbPathInput : TextField by fxid("input_adb_path")
    val adbPathButton : Label by fxid("btn_adb_path")

    val apkPathInput : TextField by fxid("input_apk_path")
    val apkPathButton : Label by fxid("btn_apk_path")

    val soundPathInput : TextField by fxid("input_sound_path")
    val soundPathButton : Label by fxid("btn_sound_path")

    val installButton : Button by fxid("btn_install")
    val logText : TextArea by fxid("input_log")

    private val separator = System.getProperty("file.separator")

    init {
        soundPathButton.setOnMouseClicked {
            val files = chooseFile(
                    title = "Select Sound folder",
                    filters = arrayOf(FileChooser.ExtensionFilter("All Files", "*.*"))
            )
            if (!files.isEmpty()) {
                soundPathInput.text = files[0].absolutePath.toString()
            }

        }

        adbPathInput.text = getDefaultADBFolder()
        adbPathButton.setOnMouseClicked {
            val files = chooseFile(
                    title = "Select ADB file",
                    filters = arrayOf(FileChooser.ExtensionFilter("All Files", "*.*"))
            )
            if (!files.isEmpty()) {
                adbPathInput.text = files[0].absolutePath.toString()
            }

        }

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
            writeLog("Checking...")

            val apkPath = getAPKPath()
            if (apkPath == null || !apkPath.endsWith(".apk")) {
                writeLog("APK file is invalid")
            } else {
                writeLog("APK found...")

                val adbPath = adbPathInput.text
                if (File(adbPath).exists()) {
                    writeLog("ADB is ready...")
                    writeLog("Starting...")
                    startProcess(apkPath)
                } else {
                    writeLog("Cannot find ADB tool")
                }
            }
        }
    }

    private fun startProcess(apkPath: String) {
        logText.text = ""
        execADBCommand("devices")
        execADBCommand("shell dpm remove-active-admin com.sleepinfuser.launcher/com.sleepinfuser.mainapp.SleepDeviceAdminReceiver")
        execADBCommand("push \"$apkPath\" \"/data/local/tmp/com.sleepinfuser.launcher\"")
        execADBCommand("shell pm install -t -r \"/data/local/tmp/com.sleepinfuser.launcher\"")
        execADBCommand("shell dpm set-device-owner com.sleepinfuser.launcher/com.sleepinfuser.mainapp.SleepDeviceAdminReceiver")

        if (isSoundFolderFound()) {
            writeLog("Sounds folder found, start to copy sound files")

            execADBCommand("shell rm -rf \"/sdcard/Sounds\"")
            execADBCommand("push \"$soundPathInput\" \"/sdcard/\"")
        } else {
            writeLog("Sound folder not found or invalid")
        }
    }

    private fun getCurrentFolder() : String {
        return File(MainView::class.java.protectionDomain.codeSource.location.toURI()).parent
    }

    private fun getAPKPath() : String? {
        return apkPathInput.text
    }

    private fun getDefaultADBFolder() : String {
        val os = System.getProperty("os.name").toLowerCase()
        if (os.contains("mac")) {
            return "${getCurrentFolder()}${separator}mac${separator}adb"
        } else {
            return "${getCurrentFolder()}${separator}win${separator}adb.exe"
        }
    }

    private fun isSoundFolderFound() : Boolean {
        val soundFolder = File(soundPathInput.text)
        return (soundFolder.exists() && soundFolder.isDirectory && soundFolder.name == "Sounds")
    }

    private fun writeLog(line: String) {
        logText.appendText(line)
        logText.appendText("\n")
    }


    private fun execADBCommand(cmd: String) {
        runAsync {
            try {
                execCommand("${adbPathInput.text} $cmd")
            } catch (e : Exception) {
                (e.message ?: "Error when execute $cmd")
            }
        } ui {
            writeLog(it)
        }

    }

    @Throws(java.io.IOException::class)
    fun execCommand(cmd: String): String {
        val s = java.util.Scanner(Runtime.getRuntime().exec(cmd).inputStream).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }

}