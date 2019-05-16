llCrop allows you to either [remove unwanted outer areas from a jpg-photo](https://en.wikipedia.org/wiki/Cropping_(image)) 
or to create zoom-ins.

Just load a jpeg photo, select a rectangle and save the rectangle as a new photo-file.

While there are many apps that can crop images (and may have many more features) these apps cause [quality-losses caused by
jpg-re-encoding](https://en.wikipedia.org/wiki/Lossy_compression).

llCrop ("ll" stands for loss-less) can do [cropping without quality-losses](https://en.wikipedia.org/wiki/Lossy_compression#JPEG) because it crops 
in the raw jpg-photo-data without 
the need for jpg-image-re-encoding. It also preserves embedded meta data (Exif/Iptc and xmp)

---

## Workflows:

* [#1](https://github.com/k3b/LosslessJpgCrop/issues/1) From Android **App-Launcher**:
	* Start llCrop
		* pick an image 
	* crop 
		* press save-button
			* pick outputfolder and filename
* [#1](https://github.com/k3b/LosslessJpgCrop/issues/1) From any **File-Manager** or **Gallery-app** that supports [intent-action-EDIT](https://developer.android.com/reference/android/content/Intent#ACTION_EDIT) for mime *image/jpeg*:
	* Select an image (app-specific) 
	* Press edit (app-specific) 
		* choose *llCrop* as editor 
			* crop 
			* press save-button
				* pick outputfolder and filename
* (Not implemented yet) [#2](https://github.com/k3b/LosslessJpgCrop/issues/2): From any app that supports [intent-action-SEND](https://developer.android.com/reference/android/content/Intent#ACTION_SEND) or [intent-action-SEND-TO](https://developer.android.com/reference/android/content/Intent#ACTION_SENDTO) for mime *image/jpeg*
	* Select an image (app-specific) 
	* Press Send/SendTo/Share button (app-specific) 
		* choose *llCrop* as destination 
			* crop 
			* press share-button
				* choose a share destination that should receive the cropped image
* (Not implemented yet) [#3](https://github.com/k3b/LosslessJpgCrop/issues/3)): From any app that supports [intent-action-GET-CONTENT](https://developer.android.com/reference/android/content/Intent#ACTION_GET_CONTENT) for mime *image/jpeg*
	* Press open-file/open-image (app-specific) 
	* In the image/document-chooser open the hamburger menu and choose *llCrop* as image-source
		* *llCrop* opens an other image-chooser where you can pick the uncropped original image
			* crop 
			* press ok-button
				* the app that initiated the first "open file/image" receives the cropped image
  
---

[<img src="https://github.com/k3b/APhotoManager/wiki/fdroid.png" alt="available on F-Droid app store" height="82" width="324">](https://f-droid.org/en/packages/de.k3b.android.lossless_jpg_crop)
