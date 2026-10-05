package com.exapps.nooralhuda.feature.quran.data

interface AudioDownloads {
    suspend fun downloadedUri(reciterId: String, surahId: Int): String?
    suspend fun download(reciterId: String, surahId: Int, url: String): Result<String>
}
