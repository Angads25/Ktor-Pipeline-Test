package com.github.angads25.ktorpipeline

expect fun <T: Any> runBlocking(block: suspend () -> T): T