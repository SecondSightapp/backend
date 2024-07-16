package com.secondsight.gemeniimplementation

import java.util.Properties
import java.io.FileInputStream

class API {
    val key: String = getApiKey()
    private fun getApiKey(): String {
        val properties = Properties()
        val localPropertiesFile = "local.properties"
        FileInputStream(localPropertiesFile).use { inputStream ->
            properties.load(inputStream)
        }
        return properties.getProperty("GEMINI_API_KEY")
    }
}