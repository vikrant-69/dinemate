package com.hackathon.dinemate

import android.util.Log
import com.hackathon.dinemate.util.HttpUtil
import com.hackathon.dinemate.util.RequestType

 suspend fun handleUserRegistration(){
    val response = HttpUtil.makeRequest(
        requestType = RequestType.POST,
        url = "",
        body = ""

    )

    Log.d("REGISTRATION RESPONSE", response.body)
}
