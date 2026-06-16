package com.example.tarik.data.classifier
import android.content.Context
import org.json.JSONObject

// instead of having our training data in a kt file we have it in a json file and fetch it kotlin such that
// if we want to have more data, we can simply add to our json file without having to change much on the kt logic.
object TrainingData {

    fun load(context: Context): List<TrainingExample> {
        val rawJson = context.assets
            .open("trainingData.json")
            .bufferedReader()
            .use { it.readText() }

        val examples = mutableListOf<TrainingExample>()
        val root = JSONObject(rawJson)
        val array = root.getJSONArray("examples")

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            examples.add(
                TrainingExample(
                    category = obj.getString("category"),
                    text = obj.getString("text")
                )
            )
        }
        return examples
    }
}