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
package org.terasology.engine.subsystem.lwjgl;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import org.terasology.rendering.VertexBufferObjectUtil;

import java.nio.IntBuffer;

/**
 * @author Immortius
 */
public class GLBufferPool {

    private static final int BUFFER_FETCH_SIZE = 16;

    private boolean traceBufferUsage;
    private int totalPoolSize;

    private TIntList pool = new TIntArrayList();

    private TIntObjectMap<String> usageTracker;

    public GLBufferPool(boolean traceBufferUsage) {
        this.traceBufferUsage = traceBufferUsage;
        if (traceBufferUsage) {
            usageTracker = new TIntObjectHashMap<>();
        }
    }


    public int get(String forUseBy) {
        if (pool.isEmpty()) {//如果池子空了开始放水
            IntBuffer buffer = BufferUtils.createIntBuffer(BUFFER_FETCH_SIZE);
            GL15.glGenBuffers(buffer);//根据buffer的长度生成buffers吗
            for (int i = 0; i < BUFFER_FETCH_SIZE; ++i) {
                pool.add(buffer.get(i));//存入列表号码
            }
            totalPoolSize += BUFFER_FETCH_SIZE;//means totalPoolSize= BUFFER_FETCH_SIZE
        }

        int result = pool.removeAt(pool.size() - 1);//删除最后一个元素
        if (traceBufferUsage) {//使用列表里压入使用的id
            usageTracker.put(result, forUseBy);//用的列表里存入他
        }	
        return result;
    }

    public void dispose(int buffer) {
        if (buffer != 0) {
            pool.add(buffer);
            IntBuffer dataBuffer = BufferUtils.createIntBuffer(1);
            dataBuffer.put(0);
            dataBuffer.flip();
            VertexBufferObjectUtil.bufferVboData(buffer, dataBuffer, GL15.GL_STATIC_DRAW);
            dataBuffer.flip();

            if (traceBufferUsage) {
                usageTracker.remove(buffer);//在使用 map中取消他
            }
        }
    }

    public int getActivePoolSize() {
        return totalPoolSize - pool.size();
    }

    public String getUsageMap() {
        if (traceBufferUsage) {
            return usageTracker.toString();
        }
        return "Tracing disabled";
    }

}
