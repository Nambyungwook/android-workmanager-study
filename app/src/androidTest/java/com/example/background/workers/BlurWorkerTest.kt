package com.example.background.workers

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.workDataOf
import com.example.background.KEY_IMAGE_URI
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import java.lang.Exception

class BlurWorkerTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    var wmRule = WorkManagerTestRule()

    @Test
    fun testFailsIfNoInput() {
        // 입력 데이터 정의 - 비어있음

        // workrequest 생성
        val workRequest = OneTimeWorkRequestBuilder<BlurWorker>().build()

        wmRule.workManager.enqueue(workRequest).result.get()
        val workInfo = wmRule.workManager.getWorkInfoById(workRequest.id).get()

        assertThat(workInfo.state, `is`(WorkInfo.State.FAILED))
    }

    @Test
    @Throws(Exception::class)
    fun testAppliesBlur() {
        // 입력 데이터 정의
        val inputDataUri = copyFileFromTestToTargetContext(
            wmRule.testContext,
            wmRule.targetContext,
            "test_image.png"
        )
        val inputData = workDataOf(KEY_IMAGE_URI to inputDataUri.toString())

        // workRequest 생성
        val workRequest = OneTimeWorkRequestBuilder<BlurWorker>()
            .setInputData(inputData)
            .build()

        wmRule.workManager.enqueue(workRequest).result.get()

        val workInfo = wmRule.workManager.getWorkInfoById(workRequest.id).get()
        val outputUri = workInfo.outputData.getString(KEY_IMAGE_URI)

        // Assert
        assertThat(uriFileExists(wmRule.targetContext, outputUri), `is`(true))
        assertThat(workInfo.state, `is`(WorkInfo.State.SUCCEEDED))

    }
}