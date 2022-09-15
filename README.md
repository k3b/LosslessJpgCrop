Loss Less Cropping and Image Rotation: Remove unwanted parts of jpg photo without quality loss.

While there are many apps capable of cropping images (often with additional features), 
they generally cause quality loss because they 
[re-encode to JPEG again](https://en.wikipedia.org/wiki/Lossy_compression) when saving the output file.

LLCrop (the "LL" stands for lossless) can [crop JPEG images without quality loss](https://en.wikipedia.org/wiki/Lossy_compression#JPEG) 
because it crops the raw JPEG image without re-encoding the file. It also preserves embedded metadata (EXIF/IPTC and XMP).

Simply load a JPEG image from the in-app image browser, adjust the rectangular selection, rotate it if necessary and save it as a new image file.

Note: This app is focused on lossless JPEG image manipulation, so issues that propose additional 
features (e.g. support for other file formats, add resize-support or adding text to images) are out of scope.

---

## Supported Workflows:

* [#1](https://github.com/k3b/LosslessJpgCrop/issues/1) From Android **app launcher**:
    * Pick an image and crop it to a new public file
* [#1](https://github.com/k3b/LosslessJpgCrop/issues/1) From any **file manager** or **gallery app** that supports [intent-action-EDIT](https://developer.android.com/reference/android/content/Intent#ACTION_EDIT) for MIME *image/jpeg*:
	* Crop current selected image to a new public file
* [#2](https://github.com/k3b/LosslessJpgCrop/issues/2): From any app that supports [intent-action-SEND](https://developer.android.com/reference/android/content/Intent#ACTION_SEND) or [intent-action-SEND-TO](https://developer.android.com/reference/android/content/Intent#ACTION_SENDTO) for MIME *image/jpeg*
	* Send/SendTo/Share a cropped version of the currently selected image 
* [#3](https://github.com/k3b/LosslessJpgCrop/issues/3)/[#8](https://github.com/k3b/LosslessJpgCrop/issues/8): From any app that supports [intent-action-GET-CONTENT](https://developer.android.com/reference/android/content/Intent#ACTION_GET_CONTENT) or intent-action-PICK for MIME *image/jpeg*
	* Open/Pick the cropping of an uncropped image
* [#17](https://github.com/k3b/LosslessJpgCrop/issues/17) : added support for image rotation
---

## Requirements

* Android 4.4 KitKat (API 19) or newer
* CPU arm64-v8a, arbeabi-v7a, x86 or x86_64 because of the C++ cropping code
* Permissions
  * READ_EXTERNAL_STORAGE (to open a local image)
  * WRITE_EXTERNAL_STORAGE (to save the cropped image)

---

[<img src="https://github.com/k3b/APhotoManager/wiki/fdroid.png" alt="available on F-Droid app store" height="82" width="200">](https://f-droid.org/packages/de.k3b.android.lossless_jpg_crop)<br/>
[<img src="https://github.com/k3b/LosslessJpgCrop/raw/master/app/src/debug/res/drawable/qr_code_url_llcrop_fdroid.png" alt="available on F-Droid app store" height="200" width="200">](https://f-droid.org/packages/de.k3b.android.lossless_jpg_crop)
