# RotaryButton
Custom view for user who want to have a rotary button like volume button (volume knob) or an arc progress bar.

![preview](./demo.PNG)

Installation
-------

Add jitpack.io repository to your root build.gradle:
```groovy
allprojects {
 repositories {
    google()
    jcenter()
    maven { url "https://jitpack.io" }
 }
}
```
Add the dependency to your module build.gradle:

`implementation 'com.github.hiennv3192:rotarybutton:1.0.5'`

API
-------

Method | Xml | Description | Default value
--- | --- | --- | ---
`setEnabled` | android:enabled | make view enable to interact | true
`isEnabled` |  | Check view is enable or not | 
`setProgressBgImgRes` | app:rotary_progressBackgroundDrawable | Set the image for progress's background | 
`setProgressFgImgRes` | app:rotary_progressForegroundDrawable | Set the image for progress's foreground | 
`setButtonBgImgRes` | app:rotary_buttonBackgroundDrawable | Set the image for button's background | 
`setButtonFgImgRes` | app:rotary_buttonForegroundDrawable | Set the image for button's foreground | 
`setProgressMax` | app:rotary_progressMax | Set max for progress | 100
`getProgressMax` |  | Get max progress | 
`setProgress` | app:rotary_progress | Set progress | 
`getProgress` |  | Get current progress | 
`setMaxRotateDegrees` | app:rotary_maxRotateDegrees | Set the max rotation degrees of button | 270
`setProgressStartDegrees` | app:rotary_progressStartDegrees | Starting angle (in degrees) where the progress begins | 135
`setButtonStartDegrees` | app:rotary_buttonStartDegrees | Set the start point in degrees of button foreground | 135
`setProgressPadding` | app:rotary_progressPadding | Set padding for progress foreground and progress background | 0
`setButtonBgPadding` | app:rotary_buttonBackgroundPadding | Set padding for button background | 100
`setButtonFgPadding` | app:rotary_buttonForegroundPadding | Set padding for button foreground | 180
`setOnSeekBarChangeListener` |  | Add a listener that will be invoked when the user interacts(start touch, rotate, stop touch) with the view | 
`setOnClickListener` |  | Add a listener that will be invoked when the user interacts(touch only) with the view | 

Note
-------

`Please besure your progress foreground image, progress background image, button foreground image, button background image are square`

License
-------

    Copyright 2020 Nguyễn Văn Hiển

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
