## Custom Take Pick File

TakePickFile is a module for camera with face detection, location from coordinate, record video, capture photo ID card and pick file (photo or video) from gallery. Using supporting modules, including:

- **[CameraX](https://developer.android.com/training/camerax)**: CameraX is a Jetpack library, built to make developing camera apps easy.
- **[ML Kit](https://developers.google.com/ml-kit)**: ML Kit’s processing happens on-device. This makes it fast and unlocks real-time use cases like processing of camera input. It also works while offline and can be used for processing images that need to remain on the device.
- **[Location](https://developers.google.com/android/guides/setup)**: To get the location of a specific coordinate from maps
- **[Navigation](https://developer.android.com/guide/navigation)**: Navigation is an interaction that allows users to browse, enter, and exit various content within an application.
- **[Lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle)**: Lifecycle-based components perform actions in response to changes in the lifecycle state of other components, such as activities and fragments.
- **[Compressor](https://github.com/zetbaitsu/Compressor)**: Compressor is a lightweight and powerful android image compression library.
- **[Dexter](https://github.com/Karumi/Dexter)**: Dexter is an Android library that simplifies the process of requesting permissions at runtime.

# Preview


   Capture with Pick File  |  Capture with ID Card Ruler  | Take Video with Pick File |
:-------------------------:|:-------------------------:|:-------------------------:
![](https://github.com/mnaufalhamdani/CustomTakePickFile/blob/main/image/Screenshot_2024-10-24-09-25-29-60.jpg)  |  ![](https://github.com/mnaufalhamdani/CustomTakePickFile/blob/main/image/Screenshot_2024-10-24-09-26-26-60.jpg)  |  ![](https://github.com/mnaufalhamdani/CustomTakePickFile/blob/main/image/Screenshot_20241024_093019.png)
   Capture with Face Detection  |  Camera without Face Detection  | Capture with additional Watermark |
![](https://github.com/mnaufalhamdani/CustomTakePickFile/blob/main/image/Screenshot_2024-10-24-09-27-33-65.jpg)  |  ![](https://github.com/mnaufalhamdani/CustomTakePickFile/blob/main/image/Screenshot_2024-10-24-09-27-47-42.jpg)  |  ![](https://github.com/mnaufalhamdani/CustomTakePickFile/blob/main/image/Screenshot_2024-10-24-09-28-02-12.jpg)


# Usage


1. Gradle dependency:

	```kotlin
	allprojects {
	   repositories {
           	maven { url "https://jitpack.io" }
	   }
	}
	```

    ```kotlin
   implementation 'com.github.mnaufalhamdani:customtakepickfile:24.10.18'
    ```

2. The TakePickFile configuration is created using the builder pattern.

	**Kotlin Photo**

	```kotlin
    TakePickFile.with(this)
		.defaultCamera(TakePickFile.LensCamera.LENS_FRONT_CAMERA)	//default is LENS_BACK_CAMERA
		.typeMedia(TakePickFile.TypeMedia.PHOTO)			//default is PHOTO (PHOTO or VIDEO)
		.setLineOfId(true)						//default is false (for ID Card Ruler)
		.cameraOnly(true)						//default is false (if false, pick file from gallery)
	 	.isFaceDetection(true)						//default is false
	 	.isWaterMark(true)						//default is false
	 	.additionalWaterMark("Custom Watermark is here")		//default is null (if using this line, always additional text)
		.start(0)
    ```

 	**Kotlin Video**

	```kotlin
    TakePickFile.with(this)
		.defaultCamera(TakePickFile.LensCamera.LENS_FRONT_CAMERA)	//default is LENS_BACK_CAMERA
		.typeMedia(TakePickFile.TypeMedia.VIDEO)			//default is PHOTO (PHOTO or VIDEO)
 		.setMaxDuration(0)						//default is null or 0 (if using this line, in milliseconds)
		.start(0)
    ```
    
3. Handling results

    **Override `onActivityResult` method and handle TakePickFile result.**

    ```kotlin
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                data?.let {
                    it.data?.let { uri ->
                        val path = uri.toFile().absolutePath
                        binding.tvPath.text = path
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    ```
    

## License

    Copyright 2024, mnaufalhamdani

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
   
   
