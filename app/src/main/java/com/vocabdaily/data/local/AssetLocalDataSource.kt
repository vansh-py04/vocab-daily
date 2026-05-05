package com.vocabdaily.data.local

import android.content.Context
import com.vocabdaily.data.datasource.LocalDataSource
import com.vocabdaily.domain.model.Word
import org.json.JSONArray

class AssetLocalDataSource(
    private val context: Context,
) : LocalDataSource {
    override suspend fun loadWords(): List<Word> {
        return runCatching { loadFromCsv("ENGLISH_CERF_WORDS.csv") }
            .getOrElse { loadFromJson("cefr_words.json") }
    }

    private fun loadFromJson(assetName: String): List<Word> {
        val json = context.assets.open(assetName).bufferedReader().use { it.readText() }
        val array = JSONArray(json)
        return buildList(array.length()) {
            for (index in 0 until array.length()) {
                val obj = array.getJSONObject(index)
                add(
                    Word(
                        text = obj.optString("text"),
                        level = obj.optString("level"),
                        meaning = obj.optString("meaning"),
                        example = obj.optString("example"),
                        ipa = obj.optString("ipa"),
                    ),
                )
            }
        }
    }

    private fun loadFromCsv(assetName: String): List<Word> {
        context.assets.open(assetName).bufferedReader().use { reader ->
            val out = ArrayList<Word>(4096)
            var isHeader = true
            reader.forEachLine { line ->
                if (isHeader) {
                    isHeader = false
                    return@forEachLine
                }
                val trimmed = line.trim()
                if (trimmed.isEmpty()) return@forEachLine

                val columns = parseCsvLine(trimmed)
                val headword = columns.getOrNull(0)?.trim().orEmpty()
                val level = columns.getOrNull(1)?.trim().orEmpty()
                if (headword.isBlank() || level.isBlank()) return@forEachLine

                out.add(
                    Word(
                        text = headword,
                        level = level,
                        meaning = "",
                        example = "",
                        ipa = "",
                    ),
                )
            }
            return out
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = ArrayList<String>(4)
        val current = StringBuilder()
        var inQuotes = false

        for (ch in line) {
            when (ch) {
                '"' -> inQuotes = !inQuotes
                ',' -> {
                    if (inQuotes) current.append(ch) else {
                        result.add(current.toString())
                        current.setLength(0)
                    }
                }
                else -> current.append(ch)
            }
        }
        result.add(current.toString())
        return result
    }
}
