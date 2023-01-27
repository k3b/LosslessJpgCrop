* v Feature [#35](https://github.com/k3b/LosslessJpgCrop/issues/35) : Display current crop box coordinates and size
    * v Show XY offset of top left corner of crop box displayed along with it's dimensions.
    * ? snap to multible of 8
* > Feature [#15](https://github.com/k3b/LosslessJpgCrop/issues/15) : Define crop box size or aspect ratio
  * v if you set width and height to a value below 100 then you define the aspect ratio of the cropping result
  * v if you set width and height to a value above 100 then you define the absolute size in pixel of the cropping result.
  * v menu to define free/square/predefined/userdefined
  * bugs
>   * 200x400 => swap =>  377x188 (expected 400x200)
v   * v 200x400 => free => free not working
  * todo 
    * add userdefined to menu
v   * readme.md += aspectratio
