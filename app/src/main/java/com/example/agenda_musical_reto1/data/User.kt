package com.example.agenda_musical_reto1.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: Int?,
    val name: String,
    val surname: String,
    val email: String,
    val password: String?,
) : Parcelable {
    constructor(
        name: String,
        surname: String,
        email: String,
        password: String,
    ) : this(null, name, surname, email, password) {

    }
}
