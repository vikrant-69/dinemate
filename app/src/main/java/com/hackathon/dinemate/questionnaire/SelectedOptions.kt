package com.hackathon.dinemate.questionnaire

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SelectedOptionsStore {
    private val _items = MutableStateFlow<List<String>>(emptyList())
    val items: StateFlow<List<String>> = _items.asStateFlow()


    /** Rebuild from selections + the authoritative question set. */
    fun setFrom(selections: Map<String, Set<String>>, questions: List<Question>) {
        val values: List<String> = selections.flatMap { (qId, selectedIds) ->
            val q = questions.firstOrNull { it.id == qId }
            q?.options?.filter { it.id in selectedIds }?.map { it.value } ?: emptyList()
        }
        _items.value = values
    }


    fun clear() { _items.value = emptyList() }
}