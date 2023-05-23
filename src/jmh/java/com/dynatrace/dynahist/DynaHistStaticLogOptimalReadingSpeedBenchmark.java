/*
 * Copyright 2022 Dynatrace LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dynatrace.dynahist;

import static com.dynatrace.dynahist.Constants.ABSOLUTE_ERROR;
import static com.dynatrace.dynahist.Constants.MAX;
import static com.dynatrace.dynahist.Constants.MIN;
import static com.dynatrace.dynahist.Constants.PRECISION;
import static com.dynatrace.dynahist.Constants.RANGE;

import java.util.SplittableRandom;
import java.util.concurrent.ThreadLocalRandom;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;

import com.dynatrace.dynahist.layout.LogOptimalLayout;
import com.dynatrace.dynahist.value.ValueEstimator;

public class DynaHistStaticLogOptimalReadingSpeedBenchmark {

	private static final int NUM_VALUES = 1_000_000;
	private static final int NUM_TEST_DATA_SETS = 100;
	private static final double[][] TEST_DATA_DOUBLE = new double[NUM_TEST_DATA_SETS][];

	private static final Histogram histogram =  Histogram.createStatic(LogOptimalLayout.create(ABSOLUTE_ERROR, PRECISION, 0, MAX));

	static {
		final SplittableRandom random = new SplittableRandom(0);
		for (int j = 0; j < NUM_TEST_DATA_SETS; ++j) {
			double[] dataDouble = new double[NUM_VALUES];
			for (int i = 0; i < NUM_VALUES; ++i) {
				dataDouble[i] = MIN * Math.pow(RANGE, random.nextDouble());
			}
			TEST_DATA_DOUBLE[j] = dataDouble;
		}

		double[] testData = TEST_DATA_DOUBLE[ThreadLocalRandom.current().nextInt(NUM_TEST_DATA_SETS)];

		for (int i = 0; i < NUM_VALUES; ++i) {
			add(testData[i]);
		}
	}


	protected static void add(double value) {
		histogram.addValue(value);
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	public void test(Blackhole blackhole) {
		for (double d : TEST_DATA_DOUBLE[0]) {
			final double quantile = histogram.getQuantile(d, ValueEstimator.MID_POINT);

			blackhole.consume(quantile);
		}
	}
}
