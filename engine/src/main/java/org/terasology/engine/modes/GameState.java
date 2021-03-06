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

package org.terasology.engine.modes;

import org.terasology.engine.GameEngine;

/**
 * @author Anton Kireev <adeon.k87@gmail.com>
 * @version 0.1
 */
public interface GameState {//mainmenu state game state stop state

    void init(GameEngine engine);

    void dispose();

    void handleInput(float delta);

    void update(float delta);

    void render();

    /**
     * @return Whether the game should hibernate when it loses focus
     */
    boolean isHibernationAllowed();//是否冬眠
}
