package com.vladceresna.virtel.controllers

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream


fun backupSystem(path: String){
    archiveToZip(FileSystem.systemPath, path)
}
fun restoreSystem(path: String){
    extractZip(path, FileSystem.systemPath)
}


fun archiveToZip(folderPath: String, zipFilePath: String) {
    val folder = File(folderPath)
    val zipFile = File(zipFilePath)

    FileOutputStream(zipFile).use { fos ->
        ZipOutputStream(fos).use { zos ->
            folder.walkTopDown().forEach { file ->
                if (file.isFile) {
                    val zipEntry = ZipEntry(folder.toPath().relativize(file.toPath()).toString())
                    zos.putNextEntry(zipEntry)
                    file.inputStream().use { fis ->
                        fis.copyTo(zos)
                    }
                    zos.closeEntry()
                }
            }
        }
    }
}


fun extractZip(zipFilePath: String, destinationFolderPath: String) {
    val zipFile = File(zipFilePath)
    val destinationFolder = File(destinationFolderPath)

    if (!destinationFolder.exists()) {
        destinationFolder.mkdirs()
    }

    FileInputStream(zipFile).use { fis ->
        ZipInputStream(fis).use { zis ->
            var zipEntry = zis.nextEntry
            while (zipEntry != null) {
                val destFile = File(destinationFolder, zipEntry.name)
                if (zipEntry.isDirectory) {
                    destFile.mkdirs()
                } else {
                    File(destFile.parent).mkdirs() // Ensure parent directories exist
                    FileOutputStream(destFile).use { fos ->
                        zis.copyTo(fos)
                    }
                }
                zipEntry = zis.nextEntry
            }
        }
    }
}