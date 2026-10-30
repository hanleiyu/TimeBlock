package com.shijiankuai.app.data

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.shijiankuai.app.domain.DailyBackup

class DownloadsBackupWriter(private val context: Context) {
    fun write(text: String) {
        if (Build.VERSION.SDK_INT < 29) {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            DailyBackup.writeOverwrite(dir, text)
            return
        }
        val resolver = context.contentResolver
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val existing = findExisting(collection)
        val uri = existing ?: insertNew(collection)
        resolver.openOutputStream(uri, "wt")?.use { stream ->
            stream.write(text.toByteArray(Charsets.UTF_8))
        } ?: error("无法写入下载目录")
        if (existing == null) {
            resolver.update(
                uri,
                ContentValues().apply { put(MediaStore.Downloads.IS_PENDING, 0) },
                null,
                null,
            )
        }
    }

    private fun findExisting(collection: Uri): Uri? {
        val resolver = context.contentResolver
        resolver.query(
            collection,
            arrayOf(MediaStore.Downloads._ID),
            "${MediaStore.MediaColumns.DISPLAY_NAME}=?",
            arrayOf(DailyBackup.FILE_NAME),
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(0)
                return ContentUris.withAppendedId(collection, id)
            }
        }
        return null
    }

    private fun insertNew(collection: Uri): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, DailyBackup.FILE_NAME)
            put(MediaStore.Downloads.MIME_TYPE, DailyBackup.MIME)
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        return context.contentResolver.insert(collection, values)
            ?: error("无法在下载目录创建备份文件")
    }
}
