package com.tradejob.pro.database.sync

import android.content.Context
import android.graphics.Bitmap
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Source
import com.google.firebase.storage.FirebaseStorage
import com.tradejob.pro.database.data.TradeJobDatabase
import com.tradejob.pro.database.data.entity.ClientEntity
import com.tradejob.pro.database.data.entity.JobEntity
import com.tradejob.pro.database.data.entity.JobPhotoEntity
import java.io.File
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.AfterClass
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SyncWorkerEmulatorTest {
    private lateinit var context: Context
    private lateinit var app: FirebaseApp
    private lateinit var database: TradeJobDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var photoFile: File? = null

    @Before
    fun setUp() {
        assumeTrue(
            "Requires local Firebase emulators and -e firebaseEmulators true",
            InstrumentationRegistry.getArguments().getString("firebaseEmulators") == "true"
        )
        context = InstrumentationRegistry.getInstrumentation().targetContext
        check(context.packageName == "com.tradejob.pro.database.test")
        val existingApps = FirebaseApp.getApps(context)
        check(existingApps.isEmpty() || existingApps.single().options.projectId == "demo-tradejob-sync")
        app = existingApps.singleOrNull() ?: FirebaseApp.initializeApp(
            context,
            FirebaseOptions.Builder()
                .setProjectId("demo-tradejob-sync")
                .setApplicationId("1:1234567890:android:abcdef1234567890")
                .setApiKey("fake-api-key")
                .setStorageBucket("demo-tradejob-sync.appspot.com")
                .build()
        )
        app.setDataCollectionDefaultEnabled(false)
        auth = FirebaseAuth.getInstance(app)
        firestore = FirebaseFirestore.getInstance(app)
        storage = FirebaseStorage.getInstance(app)
        if (existingApps.isEmpty()) {
            auth.useEmulator("127.0.0.1", 9099)
            firestore.useEmulator("127.0.0.1", 8080)
            firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build()
            storage.useEmulator("127.0.0.1", 9199)
        }
        database = Room.inMemoryDatabaseBuilder(context, TradeJobDatabase::class.java).build()
    }

    @After
    fun tearDown() {
        if (::database.isInitialized) database.close()
        photoFile?.delete()
        if (::auth.isInitialized) auth.signOut()
    }

    @Test
    fun uploadsClientJobAndPhotoThenRestoresMissingPhoto() = runBlocking {
        withTimeout(90_000) {
            val userId = requireNotNull(auth.signInAnonymously().await().user).uid
            val clientId = database.clientDao().insert(
                ClientEntity(name = "Cliente de prueba local", phone = "000000000")
            )
            val jobId = database.jobDao().insert(
                JobEntity(clientId = clientId, title = "Trabajo de prueba local")
            )
            val file = File.createTempFile("sync-test-", ".jpg", context.cacheDir)
            photoFile = file
            val bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
            try {
                file.outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)) }
            } finally {
                bitmap.recycle()
            }
            val originalBytes = file.readBytes()
            val photoId = database.jobPhotoDao().insert(
                JobPhotoEntity(jobId = jobId, photoPath = file.absolutePath)
            )

            assertEquals(ListenableWorker.Result.success(), worker().doWork())
            assertTrue(requireNotNull(database.clientDao().getClientById(clientId)).isSynced)
            assertTrue(requireNotNull(database.jobDao().getJobById(jobId)).isSynced)
            assertTrue(database.jobPhotoDao().getUnsyncedPhotos().isEmpty())

            val user = firestore.collection("users").document(userId)
            assertEquals("Cliente de prueba local", user.collection("clients")
                .document(clientId.toString()).get(Source.SERVER).await().getString("name"))
            val job = user.collection("jobs").document(jobId.toString())
            assertEquals("Trabajo de prueba local", job.get(Source.SERVER).await().getString("title"))
            val photo = database.jobPhotoDao().getSyncedPhotos().single()
            val remoteUrl = requireNotNull(photo.remoteUrl)
            assertTrue(remoteUrl.startsWith("http://127.0.0.1:9199/"))
            assertEquals(remoteUrl, job.collection("photos").document(photoId.toString())
                .get(Source.SERVER).await().getString("remoteUrl"))
            assertArrayEquals(originalBytes, storage.reference
                .child("users/$userId/jobs/$jobId/photos/$photoId.jpg").getBytes(1_000_000).await())

            database.jobPhotoDao().update(photo.copy(
                remoteUrl = "gs://demo-tradejob-sync.appspot.com/users/$userId/jobs/$jobId/photos/$photoId.jpg"
            ))
            assertTrue(file.delete())
            assertEquals(ListenableWorker.Result.success(), worker().doWork())
            assertTrue(file.exists())
            assertArrayEquals(originalBytes, file.readBytes())
        }
    }

    @Test
    fun withoutAuthenticationLeavesClientPending() = runBlocking {
        withTimeout(10_000) {
            auth.signOut()
            val clientId = database.clientDao().insert(
                ClientEntity(name = "Cliente sin sesión", phone = "000000000")
            )

            assertEquals(ListenableWorker.Result.failure(), worker().doWork())
            assertFalse(requireNotNull(database.clientDao().getClientById(clientId)).isSynced)
        }
    }

    companion object {
        @JvmStatic
        @AfterClass
        fun closeFirebase() {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            if (context.packageName != "com.tradejob.pro.database.test") return
            val app = FirebaseApp.getApps(context).singleOrNull() ?: return
            check(app.options.projectId == "demo-tradejob-sync")
            runBlocking { FirebaseFirestore.getInstance(app).terminate().await() }
            app.delete()
        }
    }

    private fun worker(): SyncWorker = TestListenableWorkerBuilder<SyncWorker>(context)
        .setWorkerFactory(object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters
            ): ListenableWorker? = if (workerClassName == SyncWorker::class.java.name) {
                SyncWorker(appContext, workerParameters, database.clientDao(), database.jobDao(), database.jobPhotoDao())
            } else {
                null
            }
        })
        .build()
}
