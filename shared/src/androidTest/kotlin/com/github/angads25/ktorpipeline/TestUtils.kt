package com.github.angads25.ktorpipeline

actual fun <T: Any> runBlocking(block: suspend () -> T): T = kotlinx.coroutines.runBlocking { block() }