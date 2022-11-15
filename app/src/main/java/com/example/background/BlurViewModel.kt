/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.background

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import com.example.background.workers.BlurWorker
import com.example.background.workers.CleanupWorker
import com.example.background.workers.SaveImageToFileWorker

class BlurViewModel(application: Application) : ViewModel() {

    internal var imageUri: Uri? = null
    internal var outputUri: Uri? = null
    private val workManager = WorkManager.getInstance(application)
    internal val outputWorkInfos: LiveData<List<WorkInfo>>

    init {
        outputWorkInfos = workManager.getWorkInfosByTagLiveData(TAG_OUTPUT)
    }

    internal fun cancelWork() {
        workManager.cancelUniqueWork(IMAGE_MANIPULATION_WORK_NAME)
    }

    private fun createInputDataForUri(): Data {
        val builder = Data.Builder()
        imageUri?.let {
            builder.putString(KEY_IMAGE_URI, imageUri.toString())
        }
        return builder.build()
    }

    /**
     * Create the WorkRequest to apply the blur and save the resulting image
     * @param blurLevel The amount to blur the image
     * 학습 목적이기 때문에 약간 부자연스럽지만 이렇게 작업합니다.
     * 블러 코드를 세 번 호출하는 것은 블러의 '수준'을 제어하는 입력을 BlurWorker에 입력하는 방식보다 효율이 떨어집니다.
     * 하지만 WorkManager 체이닝의 유연성을 보여 주기 위해 이렇게 했습니다.
     */
    internal fun applyBlur(blurLevel: Int) {
        // CleanupWorker WorkRequest, BlurImage WorkRequest, SaveImageToFile WorkRequest의 체인을 만듭니다. BlurImage WorkRequest에 입력을 전달합니다.

        // Add WorkRequest to Cleanup temporary images
        // 아래 코드 대체됨
//        var continuation = workManager
//            .beginWith(OneTimeWorkRequest
//                .from(CleanupWorker::class.java))
        var continuation = workManager
            .beginUniqueWork(
                IMAGE_MANIPULATION_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequest.from(CleanupWorker::class.java)
            )

        // Add WorkRequests to blur the image the number of times requested
        for (i in 0 until blurLevel) {
            val blurBuilder = OneTimeWorkRequestBuilder<BlurWorker>()

            // Input the Uri if this is the first blur operation
            // After the first blur operation the input will be the output of previous
            // blur operations.
            if (i == 0) {
                blurBuilder.setInputData(createInputDataForUri())
            }

            continuation = continuation.then(blurBuilder.build())
        }

        // 제약조건 추가
        val constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .build()

        // Add WorkRequest to save the image to the filesystem
        // WorkManager ID를 사용하는 대신 태그를 사용하여 작업의 라벨을 지정하겠습니다.
        // 왜냐하면 사용자가 여러 이미지를 블러 처리하는 경우 모든 이미지 저장 WorkRequest의 태그가 같지만 ID는 같지 않기 때문입니다. 또한 태그를 선택할 수도 있습니다.
        // getWorkInfosForUniqueWork를 사용하지 않습니다. 모든 블러 WorkRequest 및 정리 WorkRequest의 WorkInfo도 반환하기 때문입니다.
        // (이렇게 반환하려면 이미지 저장 WorkRequest를 찾기 위한 추가 로직이 필요함).
        val save = OneTimeWorkRequestBuilder<SaveImageToFileWorker>()
            .setConstraints(constraints)
            .addTag(TAG_OUTPUT) // TAG 설정
            .build()

        continuation = continuation.then(save)

        // work 시작
        continuation.enqueue()
    }

    private fun uriOrNull(uriString: String?): Uri? {
        return if (!uriString.isNullOrEmpty()) {
            Uri.parse(uriString)
        } else {
            null
        }
    }

    /**
     * Setters
     */
    internal fun setImageUri(uri: String?) {
        imageUri = uriOrNull(uri)
    }

    internal fun setOutputUri(outputImageUri: String?) {
        outputUri = uriOrNull(outputImageUri)
    }

    class BlurViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(BlurViewModel::class.java)) {
                BlurViewModel(application) as T
            } else {
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
