llCrop allows either [removal of unwanted outer areas from a JPEG-photo](https://en.wikipedia.org/wiki/Cropping_(image)) 
or creation of zoom-ins.

Load a JPEG photo, make a rectangular selection, to be saved as a new photo-file.

While there are many apps capable of cropping images (some with additional features) these apps cause [quality-losses caused by reencoding of JPEG](https://en.wikipedia.org/wiki/Lossy_compression).

llCrop ("ll" stands for lossless) can do [cropping without quality-losses](https://en.wikipedia.org/wiki/Lossy_compression#JPEG) because it crops the raw JPEG photo, without 
the need to reencode the JPEG image. It also preserves embedded metadata (EXIF/IPTC and XMP).

Note:

This app if focused on "lossless jpg" image manipulation so isses that propose additional features 
like "add text to image" or support "png" file format are out of scope.  

---

## Supported Workflows:

* [#1](https://github.com/k3b/LosslessJpgCrop/issues/1) From Android **App-Launcher**:
    * Pick an image and crop it to a new public file
* [#1](https://github.com/k3b/LosslessJpgCrop/issues/1) From any **File manager** or **Gallery app** that supports [intent-action-EDIT](https://developer.android.com/reference/android/content/Intent#ACTION_EDIT) for MIME *image/jpeg*:
	* Crop current selected image to a new public file
* [#2](https://github.com/k3b/LosslessJpgCrop/issues/2): From any app that supports [intent-action-SEND](https://developer.android.com/reference/android/content/Intent#ACTION_SEND) or [intent-action-SEND-TO](https://developer.android.com/reference/android/content/Intent#ACTION_SENDTO) for MIME *image/jpeg*
	* Send/SendTo/Share a cropped version of the currently selected image 
* [#3](https://github.com/k3b/LosslessJpgCrop/issues/3)/[#8](https://github.com/k3b/LosslessJpgCrop/issues/8): From any app that supports [intent-action-GET-CONTENT](https://developer.android.com/reference/android/content/Intent#ACTION_GET_CONTENT) or intent-action-PICK for MIME *image/jpeg*
	* Open/Pick the cropping of an uncropped image
  
---

## Requirements

* Android 4.4 KitKat (API 19) or newer
* CPU arm64-v8a, arbeabi-v7a, x86 or x86_64 because of the C++ cropping code
* Permissions
  * READ_EXTERNAL_STORAGE (to open a local image)
  * WRITE_EXTERNAL_STORAGE (to save the cropped image)

---

[<img src="https://github.com/k3b/APhotoManager/wiki/fdroid.png" alt="available on F-Droid app store" height="82" width="324">](https://f-droid.org/en/packages/de.k3b.android.lossless_jpg_crop)
