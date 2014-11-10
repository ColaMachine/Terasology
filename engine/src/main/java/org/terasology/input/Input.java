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

/**
 * The description of an input, whether key, mouse button or mouse wheel.
 * Immutable.
 *
 * @author Immortius
 */
public interface Input {//是鼠标还是键盘事件

    InputType getType();//得到类型

    int getId();//id是什么 只能是键值了

    String getName();//得到名称

    String getDisplayName();//得到显示名称
}
