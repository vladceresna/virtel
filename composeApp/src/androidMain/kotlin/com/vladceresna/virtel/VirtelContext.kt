package com.vladceresna.virtel

import android.content.Context
import java.lang.ref.WeakReference

object VirtelContext {
    private var contextRef: WeakReference<Context>? = null

    val context: Context
        get() = contextRef?.get()
            ?: throw IllegalStateException("Context not set. Call initialize() first.")

    fun initialize(context: Context) {
        // Store application context to avoid memory leaks
        this.contextRef = WeakReference(context.applicationContext)
    }
}