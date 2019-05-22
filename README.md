llCrop allows you to either [remove unwanted outer areas from a jpg-photo](https://en.wikipedia.org/wiki/Cropping_(image)) 
or to create zoom-ins.

Just load a jpeg photo, select a rectangle and save the rectangle as a new photo-file.

While there are many apps that can crop images (and may have many more features) these apps cause [quality-losses caused by
jpg-re-encoding](https://en.wikipedia.org/wiki/Lossy_compression).

llCrop ("ll" stands for loss-less) can do [cropping without quality-losses](https://en.wikipedia.org/wiki/Lossy_compression#JPEG) because it crops 
in the raw jpg-photo-data without 
the need for jpg-image-re-encoding. It also preserves embedded meta data (Exif/Iptc and xmp)

---

## Supported Workflows:

* [#1](https://github.com/k3b/LosslessJpgCrop/issues/1) From Android **App-Launcher**:
    * Pick an image and crop it to a new public file
* [#1](https://github.com/k3b/LosslessJpgCrop/issues/1) From any **File-Manager** or **Gallery-app** that supports [intent-action-EDIT](https://developer.android.com/reference/android/content/Intent#ACTION_EDIT) for mime *image/jpeg*:
	* Crop current selected image to a new public file
* [#2](https://github.com/k3b/LosslessJpgCrop/issues/2): From any app that supports [intent-action-SEND](https://developer.android.com/reference/android/content/Intent#ACTION_SEND) or [intent-action-SEND-TO](https://developer.android.com/reference/android/content/Intent#ACTION_SENDTO) for mime *image/jpeg*
	* Send/SendTo/Share a cropped version of the current selected image 
* [#3](https://github.com/k3b/LosslessJpgCrop/issues/3)/[#8](https://github.com/k3b/LosslessJpgCrop/issues/8): From any app that supports [intent-action-GET-CONTENT](https://developer.android.com/reference/android/content/Intent#ACTION_GET_CONTENT) or intent-action-PICK for mime *image/jpeg*
	* Open/Pick the cropping of an uncropped image
  
---

## Requirements

* Android 4.4 KitKat (API 19) or newer
* CPU arm64-v8a, arbeabi-v7a, x86, x86-64 because of c++ cropping code
* Permissions
  * READ_EXTERNAL_STORAGE (to open local image)
  * WRITE_EXTERNAL_STORAGE (to save the cropped image to)

---

[<img src="https://github.com/k3b/APhotoManager/wiki/fdroid.png" alt="available on F-Droid app store" height="82" width="324">](https://f-droid.org/en/packages/de.k3b.android.lossless_jpg_crop)
