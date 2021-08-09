package com.github.angads25.ktorpipeline

actual fun runBlocking(block: suspend () -> Unit) = kotlinx.coroutines.runBlocking { block() }