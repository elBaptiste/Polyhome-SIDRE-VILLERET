package com.villeret.polyhome_sidre_villeret

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class DeleteUserWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val houseId = inputData.getString("houseId") ?: return Result.failure()
        val userName = inputData.getString("userName") ?: return Result.failure()
        val token = inputData.getString("token") ?: return Result.failure()

        Log.d("Worker", "Début de la suppression automatique pour : $userName")

        val api = Api()
        val url = "https://polyhome.lesmoulinsdudev.com/api/houses/${houseId}/users"
        val data = UserLogin(userName)

        return suspendCancellableCoroutine { continuation ->
            api.delete(
                url, data,{code ->
                    if (code == 200) {
                        Log.d("REMOVE_USER", "WORKER")
                        continuation.resume(Result.success())
                    }
                    else {
                        Log.d("REMOVE_USER_ERROR", "$code")
                        continuation.resume(Result.retry())
                    }
                },
                token
            )
        }

    }
}