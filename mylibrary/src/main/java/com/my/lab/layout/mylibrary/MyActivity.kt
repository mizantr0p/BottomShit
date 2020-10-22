package com.my.lab.layout.mylibrary

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.my.lab.layout.mylibrary.BottomLayout.Companion.afID
import com.my.lab.layout.mylibrary.BottomLayout.Companion.deviceToken
import com.my.lab.layout.mylibrary.BottomLayout.Companion.locale
import com.my.lab.layout.mylibrary.BottomLayout.Companion.newText
import com.my.lab.layout.mylibrary.BottomLayout.Companion.os
import kotlinx.android.synthetic.main.activity_view.*
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class MyActivity : AppCompatActivity() {
    private var mCM: String? = null
    private var mUM: ValueCallback<Uri>? = null
    private var mUMA: ValueCallback<Array<Uri>>? = null
    private val FCR = 1
    private val FILECHOOSER_RESULTCODE = 1
    private var results: Array<Uri>? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)
        try {
            sent()
            webView.restoreState(savedInstanceState)
            webView.settings.javaScriptEnabled = true
            webView.settings.javaScriptCanOpenWindowsAutomatically = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            }

            webView.settings.domStorageEnabled = true
            CookieManager.getInstance().setAcceptCookie(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
            }

            webView.loadUrl(intent.getStringExtra("extra"))

            webView.setDownloadListener { url, userAgent,
                                          contentDisposition, mimeType,
                                          contentLength ->

                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED
                    ) {
                        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        requestPermissions(permissions, 1)
                    } else {

                        val request = DownloadManager.Request(
                            Uri.parse(url)
                        )
                        request.setMimeType(mimeType)
                        val cookies = CookieManager.getInstance().getCookie(url)
                        request.addRequestHeader("cookie", cookies)
                        request.addRequestHeader("User-Agent", userAgent)
                        request.setDescription("Downloading File...")
                        request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType))
                        request.allowScanningByMediaScanner()
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        request.setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(
                                url, contentDisposition, mimeType
                            )
                        )
                        val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                        dm.enqueue(request)
                        Toast.makeText(applicationContext, "Downloading File", Toast.LENGTH_LONG)
                            .show()
                    }
            }

            swipeRefreshLayout.setOnRefreshListener {
                webView.reload()
                swipeRefreshLayout.refreshDrawableState()

                swipeRefreshLayout.setRefreshing(false)
            }
            swipeRefreshLayout.setRefreshing(false)

            webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(wView: WebView, url: String): Boolean {
                    return url.indexOf("some part of my redirect uri") > -1
                }

            }

            webView.webChromeClient = RegistrationWebChromeClient()


            webView.setOnKeyListener(object : View.OnKeyListener {
                override fun onKey(p0: View?, p1: Int, p2: KeyEvent): Boolean {
                    if (p2.getAction() === KeyEvent.ACTION_DOWN) {
                        val webView = p0 as WebView
                        when (p1) {
                            KeyEvent.KEYCODE_BACK -> if (webView.canGoBack()) {
                                webView.goBack()
                                return true
                            }
                        }
                    }

                    return false
                }

            })

        } catch (e: Exception) {
        }

    }

    private fun sent(){
        Thread {
            val url = "http://pamyatki.com/loguser"

            var map = HashMap<String, String>()
            map.put("appBundle", packageName)
            map.put("locale", locale)
            map.put("deviceToken", deviceToken)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (Build.VERSION.SDK_INT >= 21) {
            //Check if response is positive
            if (resultCode == RESULT_OK) {
                if (requestCode == FCR) {
                    if (null == mUMA) {
                        return
                    }
                    if (intent == null) {
                        //Capture Photo if no image available
                        if (mCM != null) {
                            results = arrayOf(Uri.parse(mCM))
                        }
                    } else {
                        val dataString = intent.dataString
                        if (dataString != null) {
                            results = arrayOf(Uri.parse(dataString))
                        }
                    }
                }
            }
            results?.let {
                mUMA!!.onReceiveValue(it)
            }
            mUMA = null
        } else {
            if (requestCode == FCR) {
                if (null == mUM) return
                val result = if (intent == null || resultCode != RESULT_OK) null else intent.data
                mUM!!.onReceiveValue(result)
                mUM = null
            }
        }
    }

        override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val startMain = Intent(Intent.ACTION_MAIN)
            startMain.addCategory(Intent.CATEGORY_HOME)
            startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(startMain)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }


    override fun onPostResume() {
        super.onPostResume()
        try {
            webView.onResume()
        } catch (e: Exception) {
        }
    }

    inner class RegistrationWebChromeClient : WebChromeClient() {
        override fun onConsoleMessage(message: String, lineNumber: Int, sourceID: String) {
            Log.w(sourceID, message)
        }

        @RequiresApi(Build.VERSION_CODES.M)
        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED
            ) {
                Log.d(
                    "permission",
                    "permission denied to READ_EXTERNAL_STORAGE - requesting it"
                )
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permissions, 1)
            }else {


                if (mUMA != null) {
                    mUMA!!.onReceiveValue(null)
                }
                mUMA = filePathCallback
                var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent!!.resolveActivity(packageManager) != null) {
                    var photoFile: File? = null
                    try {
                        photoFile = createImageFile()
                        takePictureIntent.putExtra("PhotoPath", mCM)
                    } catch (ex: IOException) {
                        Log.e("Webview", "Image file creation failed", ex)
                    }
                    if (photoFile != null) {
                        mCM = "file:" + photoFile.absolutePath
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile))
                    } else {
                        takePictureIntent = null
                    }
                }

                val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                contentSelectionIntent.type = "*/*"
                val intentArray: Array<Intent?>
                intentArray = if (takePictureIntent != null) {
                    arrayOf(takePictureIntent)
                } else {
                    arrayOfNulls(0)
                }


                val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser")
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
                startActivityForResult(chooserIntent, FCR)
                return true
            }
            return false
        }

        fun openFileChooser(uploadMsg: ValueCallback<Uri?>?) {
            this.openFileChooser(uploadMsg, "*/*")
        }

        fun openFileChooser(uploadMsg: ValueCallback<Uri?>?, acceptType: String?) {
            this.openFileChooser(uploadMsg, acceptType, null)
        }

        fun openFileChooser(
            uploadMsg: ValueCallback<Uri?>?,
            acceptType: String?,
            capture: String?
        ) {
            val i = Intent(Intent.ACTION_GET_CONTENT)
            i.addCategory(Intent.CATEGORY_OPENABLE)
            i.type = "*/*"
            startActivityForResult(
                Intent.createChooser(i, "File Browser"),
                FILECHOOSER_RESULTCODE
            )
        }





        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            progressBar.visibility = View.VISIBLE
            progressBar.progress = newProgress
            if (newProgress == 100) {
                progressBar.visibility = View.GONE
                swipeRefreshLayout.stopNestedScroll()
            }

        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        @SuppressLint("SimpleDateFormat") val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "img_" + timeStamp + "_"
        val storageDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }


}




