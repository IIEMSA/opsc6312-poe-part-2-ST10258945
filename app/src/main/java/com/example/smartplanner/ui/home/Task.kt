package com.example.smartplanner.ui.home

import java.time.LocalDate

data class Task(
    val title: String,
    val tag: String,
    var done: Boolean,
    val dueDate: LocalDate
)
