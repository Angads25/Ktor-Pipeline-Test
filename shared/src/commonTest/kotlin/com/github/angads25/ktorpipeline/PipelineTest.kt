package com.github.angads25.ktorpipeline

import io.ktor.util.pipeline.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PipelineTest {

    private fun <Subject : Any> Pipeline<Subject, Unit>.executeBlocking(subject: Subject) = runBlocking { execute(Unit, subject) }

    private fun <Context : Any, Subject: Any> Pipeline<Subject, Context>.executeBlocking(context: Context, subject: Subject) = runBlocking { execute(context, subject) }

    @Test
    fun testSubject() {
        val startSubject = 1

        val startPhase = PipelinePhase("Start")
        val endPhase = PipelinePhase("End")

        val basePipeline = Pipeline<Int, Unit>(startPhase, endPhase)
        basePipeline.intercept(startPhase) { }
        basePipeline.intercept(endPhase) {
            proceedWith(this.subject * 100)
        }

        val endSubject = basePipeline.executeBlocking(startSubject)

        assertEquals(endSubject, startSubject * 100)
    }

    @Test
    fun testBasePipeline() {
        val events = HashSet<String>()

        val startPhase = PipelinePhase("Start-Phase")
        val middlePhase = PipelinePhase("Middle-Phase")
        val endPhase = PipelinePhase("End-Phase")

        val basePipeline = Pipeline<String, Unit>(startPhase, middlePhase, endPhase)

        basePipeline.intercept(startPhase) { events.add("start") }
        basePipeline.intercept(middlePhase) { events.add("middle") }
        basePipeline.intercept(endPhase) { events.add("end")
            proceedWith("Changed")
        }

        basePipeline.executeBlocking("Test Subject")

        assertTrue(events.contains("start"))
        assertTrue(events.contains("middle"))
        assertTrue(events.contains("end"))

    }

    @Test
    fun testNestedPipeline() {
val events = HashSet<String>()

val startPhase = PipelinePhase("Start-Phase")
val middlePhase = PipelinePhase("Middle-Phase")
val endPhase = PipelinePhase("End-Phase")

val outerPipeline = Pipeline<String, Unit>(startPhase, middlePhase, endPhase)

val innerPhaseStart = PipelinePhase("Inner-Phase-Start")
val innerPhaseMiddle = PipelinePhase("Inner-Phase-Middle")
val innerPhaseEnd = PipelinePhase("Inner-Phase-End")
val innerPipeline = Pipeline<String, String>(innerPhaseStart, innerPhaseMiddle, innerPhaseEnd)

innerPipeline.intercept(innerPhaseStart) { events.add("start-inner") }
innerPipeline.intercept(innerPhaseMiddle) { events.add("middle-inner") }
innerPipeline.intercept(innerPhaseEnd) { events.add("end-inner") }

outerPipeline.intercept(startPhase) { events.add("start-outer") }
outerPipeline.intercept(middlePhase) { events.add("middle-outer") }
outerPipeline.intercept(endPhase) {
    innerPipeline.execute("Test Context", "Test Subject")
    events.add("end-outer")
}

outerPipeline.executeBlocking("Test Subject")

assertTrue(events.contains("start-outer"))
assertTrue(events.contains("middle-outer"))

assertTrue(events.contains("start-inner"))
assertTrue(events.contains("middle-inner"))
assertTrue(events.contains("end-inner"))

assertTrue(events.contains("end-outer"))
    }

    @Test
    fun testSequence() {
        val events = ArrayList<String>()

        val startPhase = PipelinePhase("Start-Phase")
        val middlePhase = PipelinePhase("Middle-Phase")
        val endPhase = PipelinePhase("End-Phase")

        val outerPipeline = Pipeline<String, Unit>(startPhase, middlePhase, endPhase)

        val innerPhaseStart = PipelinePhase("Inner-Phase-Start")
        val innerPhaseMiddle = PipelinePhase("Inner-Phase-Middle")
        val innerPhaseEnd = PipelinePhase("Inner-Phase-End")
        val innerPipeline = Pipeline<String, String>(innerPhaseStart, innerPhaseMiddle, innerPhaseEnd)

        innerPipeline.intercept(innerPhaseStart) { events.add("start-inner") }
        innerPipeline.intercept(innerPhaseMiddle) { events.add("middle-inner") }
        innerPipeline.intercept(innerPhaseEnd) { events.add("end-inner") }

        outerPipeline.intercept(startPhase) { events.add("start-outer") }
        outerPipeline.intercept(middlePhase) { events.add("middle-outer") }
        outerPipeline.intercept(endPhase) {
            innerPipeline.execute("Test Context", "Test Subject")
            events.add("end-outer")
        }

        outerPipeline.executeBlocking("Test Subject")

        assertTrue(events[0] == "start-outer")
        assertTrue(events[1] == "middle-outer")

        assertTrue(events[2] == "start-inner")
        assertTrue(events[3] == "middle-inner")
        assertTrue(events[4] == "end-inner")

        assertTrue(events[5] == "end-outer")
    }

    @Test
    fun testPipelineMerge() {
        val events = ArrayList<String>()

        val startPhase = PipelinePhase("Start-Phase")
        val middlePhase = PipelinePhase("Middle-Phase")
        val endPhase = PipelinePhase("End-Phase")

        val startPhase2 = PipelinePhase("Start-Phase-2")
        val middlePhase2 = PipelinePhase("Middle-Phase-2")
        val endPhase2 = PipelinePhase("End-Phase-2")

        val firstPipeline = Pipeline<String, Unit>(startPhase, middlePhase, endPhase)

        val secondPipeline = Pipeline<String, Unit>(startPhase2, middlePhase2, endPhase2)

        firstPipeline.merge(secondPipeline)

        firstPipeline.intercept(startPhase) { events.add("start-1") }
        firstPipeline.intercept(middlePhase) { events.add("middle-1") }
        firstPipeline.intercept(endPhase) { events.add("end-1") }

        firstPipeline.intercept(startPhase2) { events.add("start-2") }
        firstPipeline.intercept(middlePhase2) { events.add("middle-2") }
        firstPipeline.intercept(endPhase2) { events.add("end-2") }

        firstPipeline.executeBlocking("Test Subject")

        assertTrue(events[0] == "start-1")
        assertTrue(events[1] == "middle-1")
        assertTrue(events[2] == "end-1")

        assertTrue(events[3] == "start-2")
        assertTrue(events[4] == "middle-2")
        assertTrue(events[5] == "end-2")
    }
}