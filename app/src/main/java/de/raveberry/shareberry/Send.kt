package de.raveberry.shareberry

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class Send : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    val query = intent.getStringExtra(Intent.EXTRA_TEXT)

                    if (query != null) {
                        val urls = Storage.getUrls(this)
                        val queue = Volley.newRequestQueue(this)
                        newRequest(queue, urls, 0, query)

                        toastMessage("queueing song...")
                    } else {
                        toastMessage("no query given")
                    }
                }
                finish()
            }
            else -> {
                // default
            }
        }
    }

    private fun newRequest(
        queue: RequestQueue,
        hostnames: List<String>,
        hostnameIndex: Int,
        query: String
    ) {
        /* make a new request to the next address in the list of hostnames */
        if (hostnameIndex >= hostnames.size) {
            // tried all given addresses without success
            toastMessage("could not queue song")
            return
        }
        val hostname = hostnames[hostnameIndex]
        val url = "$hostname/api/musiq/post_song/"
        val request = object : StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // success, the query was requested
                toastMessage(response)
            },
            Response.ErrorListener { error ->
                if (error.networkResponse == null) {
                    // Could not reach the hostname, try the next one
                    newRequest(queue, hostnames, hostnameIndex + 1, query)
                } else {
                    // Received an error response. Do a toast message and stop trying
                    toastMessage(String(error.networkResponse.data, Charsets.UTF_8))
                }
            }) {
            override fun getParams(): Map<String, String> {
                return hashMapOf("query" to query)
            }
            // By default, volley retries requests when they take too long
            // Override the retry policy to prevent this behavior, as Raveberry needs some time occasionally
            // https://stackoverflow.com/questions/22428343/android-volley-double-post-when-have-slow-request
            override fun getRetryPolicy(): RetryPolicy {
                return DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
            }
        }
        queue.add(request)
    }

    private fun toastMessage(message: String) {
        val duration = Toast.LENGTH_SHORT
        val toast = Toast.makeText(applicationContext, message, duration)
        toast.show()
    }
}

