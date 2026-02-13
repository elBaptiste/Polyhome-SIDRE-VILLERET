package com.villeret.polyhome_sidre_villeret

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class HouseActivity : AppCompatActivity() {

    private val api = Api()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_house)
        val token = intent.getStringExtra("token")

        if (token != null) {
            fetchHouseId(token)}
    }

    private fun fetchHouseId(token: String) {
        Log.d("POLYHOME_DEBUG", "Appel de l'API pour récupérer la maison...")
        api.get<List<HouseResponse>>("https://polyhome.lesmoulinsdudev.com/api/houses", { code, houses ->
            if (code == 200 && houses != null) {
                if (houses.isNotEmpty()) {
                    val houseId = houses[0].houseId
                    Log.d("ID_HOUSE", "House id =  : $houseId")
                    //dans logcat mettre le filtre ID_HOUSE
                } else {
                    Log.w("POLYHOME_DEBUG", "Liste de maisons vide pour cet utilisateur.")
                }
            } else {
                Log.e("POLYHOME_DEBUG", "Erreur API : Code de réponse $code")
            }
        }, token)
    }
}