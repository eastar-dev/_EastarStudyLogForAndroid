package smart.net

import okhttp3.OkHttpClient

fun okHttpClientBuilder(): OkHttpClient.Builder = OkHttpClient().newBuilder()