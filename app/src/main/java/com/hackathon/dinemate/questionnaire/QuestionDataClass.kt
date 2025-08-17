package com.hackathon.dinemate.questionnaire

data class Question(
    val id: String,
    val text: String,
    val options: List<Option>,
    val maxSelectable: Int = Int.MAX_VALUE
)

data class Option(
    val id: String,
    val text: String,
    val value: String
)

data class UserAnswer(
    val questionId: String,
    val selectedOptionIds: List<String>, // multiple
    val selectedValues: List<String>
)