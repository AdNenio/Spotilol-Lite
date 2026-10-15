package com.project.lol.webview.helpers

import java.util.concurrent.atomic.AtomicLong

object NavToken {
    private val generation = AtomicLong(0L)
    fun current(): Long = generation.get()
    fun bump(): Long = generation.incrementAndGet()
}