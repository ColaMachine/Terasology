/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.input;

import org.terasology.engine.SimpleUri;

/**
 * @author Immortius
 */
public interface BindableButton {//按钮 一个信号源

    /**
     * @return The identifier for this button
     */
    SimpleUri getId();//有唯一的id

    /**
     * @return The display name for this button
     */
    String getDisplayName();//有可以显示的名称

    /**
     * Set the circumstance under which this button sends events
     *
     * @param mode
     */
    void setMode(ActivateMode mode);//当down 或up 何种情况能自动发送时间

    /**
     * @return The circumstance under which this button sends events
     */
    ActivateMode getMode();//响应模式

    /**
     * Sets whether this button sends repeat events while pressed
     *
     * @param repeating
     */
    void setRepeating(boolean repeating);//当按下的时候是否持续发送事件

    /**
     * @return Whether this button sends repeat events while pressed
     */
    boolean isRepeating();//得到是否可以持续发送事件

    /**
     * @param repeatTimeMs The time (in milliseconds) between repeat events being sent
     */
    void setRepeatTime(int repeatTimeMs);//可重复间断时间

    /**
     * @return The time (in milliseconds) between repeat events being sent
     */
    int getRepeatTime();

    /**
     * @return The current state of this button (either up or down)
     */
    ButtonState getState();//发货当前按钮状态 按下 抬起 还是在重复

    /**
     * Used to directly subscribe to the button's events
     *
     * @param subscriber
     */
    void subscribe(BindButtonSubscriber subscriber); //注册

    /**
     * Used to unsubscribe from the button's event
     *
     * @param subscriber
     */
    void unsubscribe(BindButtonSubscriber subscriber);//卸载注册

}
