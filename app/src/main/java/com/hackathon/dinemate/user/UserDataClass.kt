package com.hackathon.dinemate.user

import com.google.firebase.firestore.PropertyName

data class User(
    @get:PropertyName("userId") @set:PropertyName("userId")
    var userId: String = "",

    @get:PropertyName("userName") @set:PropertyName("userName")
    var userName: String = "",

    @get:PropertyName("name") @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("profilePic") @set:PropertyName("profilePic")
    var profilePic: String? = null
)
