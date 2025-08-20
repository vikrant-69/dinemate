package com.hackathon.dinemate.user

import com.google.firebase.firestore.PropertyName

data class User(
    @get:PropertyName("userId") @set:PropertyName("userId")
    var userId: String = "",

    @get:PropertyName("userName") @set:PropertyName("userName")
    var userName: String = "",

    @get:PropertyName("firstName") @set:PropertyName("firstName")
    var firstName: String = "",

    @get:PropertyName("lastName") @set:PropertyName("lastName")
    var lastName: String = "",

    @get:PropertyName("profilePic") @set:PropertyName("profilePic")
    var profilePic: String? = null
)
