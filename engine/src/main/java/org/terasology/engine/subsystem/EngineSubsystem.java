/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.engine.subsystem;

import org.terasology.config.Config;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.modes.GameState;

public interface EngineSubsystem {//所有引擎的父接口
    void preInitialise();//初始化准备

    void postInitialise(Config config);//初始化

    void preUpdate(GameState currentState, float delta);//准备更新

    void postUpdate(GameState currentState, float delta);//更新

    void shutdown(Config config);//关闭

    void dispose();//取消

    void registerSystems(ComponentSystemManager componentSystemManager);//注册 lwjglgraphics lwjglinput都没有使用到这个
}
