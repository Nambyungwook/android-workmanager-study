package com.example.background.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.background.OUTPUT_PATH
import java.io.*
import java.util.*

@Throws
fun copyFileFromTestToTargetContext(
    testContext: Context,
    targetContext: Context,
    filename: String
): Uri {
    // 테스트 이미지 생성
    val destinationFilename = String.format("blur-test-%s.png", UUID.randomUUID().toString())
    val outputDir = File(targetContext.filesDir, OUTPUT_PATH)
    if (!outputDir.exists()) {
        outputDir.mkdirs()
    }
    val outputFile = File(outputDir, destinationFilename)

    val bis = BufferedInputStream(testContext.assets.open(filename))
    val bos = BufferedOutputStream(FileOutputStream(outputFile))
    val buf = ByteArray(1024)
    bis.read(buf)
    do {
        bos.write(buf)
    } while (bis.read(buf) != -1)
    bis.close()
    bos.close()

    return Uri.fromFile(outputFile)
}

/**
 * context에 파일이 존재하는지 확인
 * @param testContext android test context
 * @param uri for the file
 * @return true if file exist, false if the file does not exist of the Uri is not valid
 */
fun uriFileExists(targetContext: Context, uri: String?): Boolean {
    if (uri.isNullOrEmpty()) {
        return false
    }

    val resolver = targetContext.contentResolver

    // bitmap 생성
    try {
        BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(uri)))
    } catch (e: FileNotFoundException) {
        return false
    }

    return true
}