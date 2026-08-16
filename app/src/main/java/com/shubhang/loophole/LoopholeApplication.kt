package com.shubhang.loophole

import android.app.Application

class LoopholeApplication : Application() {
    val container by lazy { AppContainer(this) }
}
