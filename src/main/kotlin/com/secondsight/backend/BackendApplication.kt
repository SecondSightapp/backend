package com.secondsight.backend

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication
import java.io.FileInputStream


// TODO fix this

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class BackendApplication {

}

fun initializeFirebaseApp() {
    // Point this to the serviceAccountKey.json found under the resources folder in the src/main/resources folder
    val serviceAccount = FileInputStream("./src/main/resources/serviceAccountKey.json")

    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .build()

    if (FirebaseApp.getApps().isEmpty()) {
        FirebaseApp.initializeApp(options)
    }
}

fun main(args: Array<String>) {
    initializeFirebaseApp();
    runApplication<BackendApplication>(*args)
}
