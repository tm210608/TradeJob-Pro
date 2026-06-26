package com.tradejob.pro.database.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tradejob.pro.database.data.dao.ClientDao
import com.tradejob.pro.database.data.dao.JobDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val clientDao: ClientDao,
    private val jobDao: JobDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("SyncWorker", "Iniciando sincronización de datos...")
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Log.w("SyncWorker", "No hay usuario autenticado. Sincronización cancelada.")
            return Result.failure()
        }
        val db = FirebaseFirestore.getInstance()

        return try {
            Log.d("SyncWorker", "Sincronizando clientes...")
            syncClients(userId, db)
            Log.d("SyncWorker", "Sincronizando trabajos...")
            syncJobs(userId, db)
            Log.i("SyncWorker", "Sincronización completada con éxito.")
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error durante la sincronización", e)
            Result.retry()
        }
    }

    private suspend fun syncClients(userId: String, db: FirebaseFirestore) {
        val unsyncedClients = clientDao.getUnsyncedClients()
        if (unsyncedClients.isEmpty()) return

        val batch = db.batch()
        unsyncedClients.forEach { client ->
            val clientRef = db.collection("users").document(userId)
                .collection("clients").document(client.id.toString())
            batch.set(clientRef, client.copy(isSynced = true))
        }

        batch.commit().await()
        clientDao.markClientsAsSynced(unsyncedClients.map { it.id })
    }

    private suspend fun syncJobs(userId: String, db: FirebaseFirestore) {
        val unsyncedJobs = jobDao.getUnsyncedJobs()
        if (unsyncedJobs.isEmpty()) return

        val batch = db.batch()
        unsyncedJobs.forEach { job ->
            val jobRef = db.collection("users").document(userId)
                .collection("jobs").document(job.id.toString())
            batch.set(jobRef, job.copy(isSynced = true))
        }

        batch.commit().await()
        jobDao.markJobsAsSynced(unsyncedJobs.map { it.id })
    }
}
