package com.secondsight.backend

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.io.FileInputStream


// TODO fix this
private const val firebaseDatabaseURL = "https://secondsight-1-default-rtdb.firebaseio.com"

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class BackendApplication {
    /**
     * Initializes firebase for use within the project.
     * the @Bean annotation ensure that this method is called when the application starts.
     */
    @Bean
    fun initializeFirebaseApp(): FirebaseApp {
        // Point this to the serviceAccountKey.json found under the resources folder in the src/main/resources folder
        val serviceAccount = FileInputStream("./src/main/resources/serviceAccountKey.json")

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setDatabaseUrl("https://your-project-id.firebaseio.com")
            .build()

        return FirebaseApp.initializeApp(options)
    }
}

fun main(args: Array<String>) {
    runApplication<BackendApplication>(*args)
}
