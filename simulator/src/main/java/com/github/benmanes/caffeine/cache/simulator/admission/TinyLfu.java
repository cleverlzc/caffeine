/*
 * Copyright 2015 Ben Manes. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.benmanes.caffeine.cache.simulator.admission;

import com.clearspring.analytics.stream.frequency.CountMinTinyLfu;
import com.github.benmanes.caffeine.cache.simulator.BasicSettings;
import com.github.benmanes.caffeine.cache.simulator.admission.sketch.Frequency;
import com.github.benmanes.caffeine.cache.simulator.admission.sketch.FrequencySketch;
import com.typesafe.config.Config;

/**
 * Admits new entries based on the estimated frequency of its historic use.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public final class TinyLfu implements Admittor {
  private final Frequency<Object> sketch;

  public TinyLfu(Config config) {
    BasicSettings settings = new BasicSettings(config);
    String type = settings.tinyLfu().sketch();
    if (type.equalsIgnoreCase("count-min-4")) {
      sketch = new FrequencySketch<>(settings.maximumSize());
    } else if (type.equalsIgnoreCase("count-min-64")) {
      sketch = new CountMinTinyLfu<>(config);
    } else {
      throw new IllegalStateException("Unknown sketch type: " + type);
    }
  }

  @Override
  public void record(Object key) {
    sketch.increment(key);
  }

  @Override
  public boolean admit(Object candidateKey, Object victimKey) {
    long candidateFreq = sketch.frequency(candidateKey);
    long victimFreq = sketch.frequency(victimKey);
    return candidateFreq > victimFreq;
  }
}
