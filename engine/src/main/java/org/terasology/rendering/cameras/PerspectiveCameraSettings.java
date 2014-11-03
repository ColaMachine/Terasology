/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.cameras;

import org.terasology.rendering.nui.layers.mainMenu.videoSettings.CameraSetting;

public class PerspectiveCameraSettings {//透视摄像机设置  这个累其实是对 CameraSetting的封装类 是用在ui绘制的时候用的
    private CameraSetting cameraSetting;//摄像机设置

    public PerspectiveCameraSettings(CameraSetting cameraSetting) {
        this.cameraSetting = cameraSetting;
    }

    public CameraSetting getCameraSetting() {
        return cameraSetting;
    }

    public void setCameraSetting(CameraSetting cameraSetting) {
        this.cameraSetting = cameraSetting;
    }

    public int getSmoothingFramesCount() {
        return cameraSetting.getSmoothingFrames();
    }
}
