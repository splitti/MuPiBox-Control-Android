package de.mupibox.control

import android.app.Application

class MuPiBoxControlApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
