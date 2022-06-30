/*
 * Tigase XML Tools - Tigase XML Tools
 * Copyright (C) 2004 Tigase, Inc. (office@tigase.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */
package tigase.xml;

import org.junit.Ignore;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Ignore
public class StringPerformanceTest {

	@Test
	public void launchBenchmark() throws RunnerException {
		Options opt = new OptionsBuilder()
				.include(this.getClass().getName() + ".*")
//				.include(this.getClass().getName() + ".benchmarkFlatMap*")
//				.include(this.getClass().getName() + ".benchmarkFlatMapStreamToList*")
//				.include(this.getClass().getName() + ".benchmarkFindChild*")
//				.include(this.getClass().getName() + ".benchmarkFindChildNew*")
//				.include(this.getClass().getName() + ".benchmarkFindChildStream*")
//				.include(this.getClass().getName() + ".benchmarkFlatMapStream*")
//				.include(this.getClass().getName() + ".benchmarkGetChildAnd*")
				// Set the following options as needed
//				.mode (Mode.AverageTime)
				.timeUnit(TimeUnit.MICROSECONDS)
				.warmupTime(TimeValue.seconds(1))
				.warmupIterations(2)
				.measurementTime(TimeValue.seconds(1))
				.measurementIterations(5)
				.threads(2)
				.forks(1)
				.shouldFailOnError(true)
				.shouldDoGC(true)
				//.jvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintInlining")
				//.addProfiler(WinPerfAsmProfiler.class)
				.build();
		new Runner(opt).run();
	}

	private static final Map<String,String> DEDUP_ELEM_NAME = List.of("message", "iq", "presence", "query", "pubsub", "body").stream().collect(
			Collectors.toMap(Function.identity(), Function.identity()));
	
	@State(Scope.Thread)
	public static class BenchmarkState {
		String name;
		boolean dedup = false;

		@Setup(Level.Trial)
		public void initializeElement() {
			dedup = true;
			List<String> namesToUse = Stream.concat(DEDUP_ELEM_NAME.keySet().stream(), IntStream.range(0, 20).mapToObj(i -> "child-" + i)).map(String::new).collect(
					Collectors.toList());
			Collections.shuffle(namesToUse);
			int selected = new Random().nextInt(namesToUse.size());
			name = namesToUse.get(selected);
		}
	}

	@Benchmark
	@Measurement(iterations = 1000)
	@BenchmarkMode(Mode.Throughput)
	public void stringIntern(BenchmarkState state, Blackhole blackhole) {
		blackhole.consume(state.name.intern());
	}

	@Benchmark
	@Measurement(iterations = 1000)
	@BenchmarkMode(Mode.Throughput)
	public void stringDeduplication(BenchmarkState state, Blackhole blackhole) {
		blackhole.consume(DEDUP_ELEM_NAME.getOrDefault(state.name, state.name));
	}
	
	@Benchmark
	@Measurement(iterations = 1000)
	@BenchmarkMode(Mode.Throughput)
	public void stringNope(BenchmarkState state, Blackhole blackhole) {
		blackhole.consume(state.name);
	}
	
}
