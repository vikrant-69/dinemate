package com.hackathon.dinemate.home

data class GroupCreateRequest(
    val firebase_id: String,
    val name: String,
    val description: String
)

data class GroupCreateResponse(
    val invite_code: String,
    val group_id: String? = null // if backend returns it; otherwise remove
)

data class JoinGroupRequest(
    val firebase_id: String,
    val invite_code: String
)

data class Message(
    val id: String,
    val group_id: String,
    val user_id: String,
    val user_name: String,
    val message_type: String, // "text", etc.
    val content: String,
    val restaurant_data: String?, // keep it String/JSON string or Map if you prefer
    val created_at: String
)

data class GroupSummary(
    val id: String,
    val name: String,
    val description: String?,
    val created_by: String?,
    val invite_code: String?,
    val status: String?,
    val member_count: Int?,
    val max_members: Int?,
    val last_message_at: String?,
    val message_count: Int?,
    val selected_restaurant: String?, // keep as String/JSON; adjust if needed
    val created_at: String?
)