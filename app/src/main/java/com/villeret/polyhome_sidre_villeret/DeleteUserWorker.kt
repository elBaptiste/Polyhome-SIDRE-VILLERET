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
    // Worker gérant la suppression d'un utilisateur via l'API
    // Utilise CoroutineWorker pour une exécution en arrière-plan

    // On utilise CoroutineWorker pour que l'exécution attende le résultat de l'appel API avant de retourner un résulat.
    // Avec un Worker classique, la méthode se terminerait avant d'avoir reçu la réponse.
    override suspend fun doWork(): Result {

        // On récupère les données pour faire l'appel API pour supprimer l'utilisateur
        val houseId = inputData.getString("houseId") ?: return Result.failure()
        val userName = inputData.getString("userName") ?: return Result.failure()
        val token = inputData.getString("token") ?: return Result.failure()

        Log.d("POLYHOME_WORKER", "Début de la suppression automatique pour : $userName")

        val api = Api()
        val url = "https://polyhome.lesmoulinsdudev.com/api/houses/${houseId}/users"
        val data = UserLogin(userName)

        // Si on utilise un appel à API classique sans faire suspendCancellableCoroutine donc avec un Worker classique
        // l'appel API va être fait mais le Worker ne va pas attendre le résultat pour son success
        // On utilise suspendCancellableCoroutine pour s'assurer que l'appel API s'effectue entiérement
        // De cette façon, on s'assure d'avoir le bon comportement en focntion du code de réponse.
        // .resume permet d'éxécuter la coroutine voulue en valeur.
        return suspendCancellableCoroutine { continuation ->
            api.delete(
                url, data,{code ->
                    if (code == 200) {
                        // Succès
                        Log.d("REMOVE_USER", "WORKER")
                        continuation.resume(Result.success())
                    } else if (code == 500){
                        // Erreur réseau ou serveur, on relance
                        Log.d("REMOVE_USER_ERROR", "$code")
                        continuation.resume(Result.retry())
                    } else {
                        // Autre type d'erreur, on arrête le processus
                        Log.d("REMOVE_USER_ERROR", "$code")
                        continuation.resume(Result.failure())
                    }
                },
                token
            )
        }

    }
}