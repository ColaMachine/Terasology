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
package org.terasology.engine.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.EngineTime;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

public abstract class TimeBase implements EngineTime {

    private static final Logger logger = LoggerFactory.getLogger(TimeBase.class);
    private static final float DECAY_RATE = 0.95f;//衰减率
    private static final float ONE_MINUS_DECAY_RATE = 1.0f - DECAY_RATE;//1减去衰减率
    private static final float RESYNC_TIME_RATE = 0.1f;//重新同步事件屡

    private static final int MAX_UPDATE_CYCLE_LENGTH = 1000;//最高更新循环长度
    private static final int UPDATE_CAP = 1000;//更新容量

    private AtomicLong last = new AtomicLong(0);//上次事件
    private AtomicLong delta = new AtomicLong(0);//delta 间隔时间
    private float avgDelta;//平均avg
    private long desynch;//
    private boolean paused;//是否暂停

    private AtomicLong gameTime = new AtomicLong(0);//游戏时间 每次遍历 都会增加delta时间量 如果停止就不会增加

    public abstract long getRawTimeInMs();//原生的时间毫秒级

    /**
     * Increments time
     *
     * @return The number of update cycles to run
     */
    public Iterator<Float> tick() {//tick 每一帧 tick一下
        long now = getRawTimeInMs();//当前时间取的是系统时间
        long newDelta = now - last.get();//上次事件也是本地系统时间 相减得出delta 变化时间 然后再把now赋值给last
        if (0 == newDelta) {
            // running too fast, slow down to avoid busy-waiting
            try {
                Thread.sleep(0, 1000);
            } catch (InterruptedException e) {
                // do nothing
            }
            now = getRawTimeInMs();
            newDelta = now - last.get();//新的delta
        }
        if (newDelta >= UPDATE_CAP) {//delta 超时 
            logger.warn("Delta too great ({}), capping to {}", newDelta, UPDATE_CAP);
            newDelta = UPDATE_CAP;
        }
        int updateCycles = (int) ((newDelta - 1) / MAX_UPDATE_CYCLE_LENGTH) + 1;//99/1000=0.099 =1 感觉这里没什么意思 因为updatecycles注定是1啊
        last.set(now);
        avgDelta = avgDelta * DECAY_RATE + newDelta * ONE_MINUS_DECAY_RATE;//用newDelta时间不断去更新avgDelta 就像计算下载速度也是一样 也是不断去取平均数

        // Reduce desynch between server time
        if (desynch != 0) {//和服务器相差时间 //1/10的变化量 逐渐和服务器时间靠近
            long diff = (long) Math.ceil(desynch * RESYNC_TIME_RATE);
            if (diff == 0) {
                diff = (long) Math.signum(desynch);
            }
            gameTime.getAndAdd(diff);//gameTime是在游戏中的时间 每次都加上newDelta 是在 timeStepper的next中增加的
            desynch -= diff;
        }

        if (paused) {
            delta.set(0);
            return new TimeStepper(1, 0);
        } else {
            if (updateCycles > 0) {
                delta.set(newDelta / updateCycles);
            }
            return new TimeStepper(updateCycles, newDelta / updateCycles);//一般updateCycles都是1 如果是2 或者更大值说明和上次的时间
        }
    }

    @Override
    public float getDelta() {
        return delta.get() / 1000f;
    }

    @Override
    public long getDeltaInMs() {
        return delta.get();
    }

    @Override
    public float getFps() {
        return 1000.0f / avgDelta;
    }

    @Override
    public long getGameTimeInMs() {
        return gameTime.get();
    }

    @Override
    public float getGameTime() {
        return gameTime.get() / 1000f;
    }

    @Override
    public void setGameTime(long amount) {
        delta.set(0);
        gameTime.set(amount);
    }

    @Override
    public long getRealTimeInMs() {
        return getRawTimeInMs();
    }

    @Override
    public void updateTimeFromServer(long targetTime) {//让服务的时间和客户端的时间调准
        desynch = targetTime - getGameTimeInMs();
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    private class TimeStepper implements Iterator<Float> {

        private int cycles;
        private long deltaPerCycle;
        private int currentCycle;

        public TimeStepper(int cycles, long deltaPerCycle) {
            this.cycles = cycles;
            this.deltaPerCycle = deltaPerCycle;
        }

        @Override
        public boolean hasNext() {
            return currentCycle < cycles;
        }

        @Override
        public Float next() {
            currentCycle++;
            gameTime.addAndGet(deltaPerCycle);
            return deltaPerCycle / 1000f;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
