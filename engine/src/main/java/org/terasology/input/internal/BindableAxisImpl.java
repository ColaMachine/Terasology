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

package org.terasology.input.internal;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.input.BindAxisEvent;
import org.terasology.input.BindAxisSubscriber;
import org.terasology.input.BindableAxis;
import org.terasology.input.BindableButton;
import org.terasology.input.ButtonState;
import org.terasology.input.SendEventMode;
import org.terasology.math.Vector3i;

import javax.vecmath.Vector3f;
import java.util.List;

/**
 * A Bind Axis is an simulated analog input axis, maintaining a value between -1 and 1.  It is linked to
 * a positive BindableButton (that pushes the axis towards 1) and a negative BindableButton (that pushes it towards -1)
 *
 * @author Immortius
 */
public class BindableAxisImpl implements BindableAxis {
    private String id;
    private BindAxisEvent event;//绑定的坐标事件
    private BindableButton positiveInput;//simpleuri 正输入
    private BindableButton negativeInput;//负的输入
    private float value;

    private List<BindAxisSubscriber> subscribers = Lists.newArrayList();//用户

    private SendEventMode sendEventMode = SendEventMode.WHEN_NON_ZERO;//当新的值不是0的时候发送事件

    public BindableAxisImpl(String id, BindAxisEvent event, BindableButton positiveButton, BindableButton negativeButton) {
        this.id = id;
        this.event = event;
        this.positiveInput = positiveButton;
        this.negativeInput = negativeButton;
    }

    @Override
    public String getId() {
        return id;
    }//得到id

    @Override
    public void setSendEventMode(SendEventMode mode) {
        sendEventMode = mode;
    }//发送事件模式

    @Override
    public SendEventMode getSendEventMode() {
        return sendEventMode;
    }//get

    @Override
    public void subscribe(BindAxisSubscriber subscriber) {
        this.subscribers.add(subscriber);
    }//注册用户

    @Override
    public void unsubscribe(BindAxisSubscriber subscriber) {
        this.subscribers.remove(subscriber);
    }//卸载用户

    @Override
    public float getValue() {
        return value;
    }//getvalue
//更新
    public void update(EntityRef[] inputEntities, float delta, EntityRef target, Vector3i targetBlockPos, Vector3f hitPosition, Vector3f hitNormal) {
        boolean posInput = positiveInput.getState() == ButtonState.DOWN;
        boolean negInput = negativeInput.getState() == ButtonState.DOWN;

        float targetValue = 0;
        if (posInput) {
            targetValue += 1.0f;//如果按钮的按键按下了 那么＋
        }
        if (negInput) {
            targetValue -= 1.0f;//如果负向的按钮按下了 那么-
        }

        // TODO: Interpolate, based on some settings (immediate, linear, lerp?)

        float newValue = targetValue;

        if (sendEventMode.shouldSendEvent(value, newValue)) {
            event.prepare(id, newValue, delta);
            event.setTargetInfo(target, targetBlockPos, hitPosition, hitNormal);
            for (EntityRef entity : inputEntities) {
                entity.send(event);
                if (event.isConsumed()) {
                    break;
                }
            }
            sendEventToSubscribers(delta, target);
        }
        value = newValue;
    }

    private void sendEventToSubscribers(float delta, EntityRef target) {
        for (BindAxisSubscriber subscriber : subscribers) {
            subscriber.update(value, delta, target);
        }
    }

}
