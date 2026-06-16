package com.example.tarik.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.tarik.data.HistoryItem


// sharehistory fact builds a small shareable fact card from a historyitem and launches the sharesheet
fun shareHistoryFact(context: Context, item: HistoryItem) {
    //cleaning the title
    val cleanTitle = item.title.replace(Regex("<.*?>"), "")

    val shareText = buildString {
        append("Today in History: ${item.year}\n\n")
        append(cleanTitle)
        append("\n\n")
        append(item.extract.take(200))
        if (item.extract.length > 200) append("…")
        append("\n\nRead more: ${item.wikipediaUrl}")
        append("\n\nShared from Tarik")
    }

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, "Today in History: $cleanTitle")
    }

    // ensuring the share picker is always shown
    val chooserIntent = Intent.createChooser(sendIntent, "Share this fact via")


    // defensively starting the activity
    if (sendIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(chooserIntent)
    } else {
        Toast.makeText(context, "No apps available to share with", Toast.LENGTH_SHORT).show()
    }
}
