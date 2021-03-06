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
package org.terasology.monitoring.impl;

import org.terasology.monitoring.ThreadActivity;

/**
 * @author Immortius
 */
public class ThreadActivityInternal implements ThreadActivity {

    private SingleThreadMonitor monitor;//单线程监控器

    public ThreadActivityInternal(SingleThreadMonitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public void close() {
        monitor.endTask();//监控停止任务
    }
}
