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

import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ElementPerformanceTest2 {

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

	@State(Scope.Thread)
	public static class BenchmarkStateSimilar {
		public enum ChildPosition {
			first,
			middle,
			last
		}

		Element element;
		int i;

//		@Param({"1", "5", "10", "20" })
		@Param({"1", "5", "10", "20", "50" })
		int maxChild;
		String name;

//		@Param({"first", "middle", "last"})
		@Param({"last"})
		ChildPosition childPosition;

		String clazz = "ArrayList";

		public String generateChildName(int position) {
			return "child-" + position;
		}

		@Setup(Level.Trial)
		public void initializeElement() {
			switch (clazz) {
				case "ArrayList" -> Element.listSupplier = ArrayList::new;
				case "LinkedList" -> Element.listSupplier = LinkedList::new;
			}

			element = new Element("root");
			for (int childIdx = 1; childIdx <= maxChild; childIdx++) {
				element.addChild(new Element(generateChildName( childIdx)).withElement("subchild-1", null));
			}
		}

		@Setup(Level.Iteration)
		public void initializeVariable() {
			i = switch (childPosition) {
				case first -> 1;
				case last -> element.getChildren().size();
				case middle -> Math.round(element.getChildren().size() / 2);
			};
			name = generateChildName(i);
		}
	}

	@State(Scope.Thread)
	public static class BenchmarkStateString {

		public String name;
		public String value;

		@Setup(Level.Iteration)
		public void initializeVariable() {
			name = "child-" + new Random().nextInt();
			value = new String(name);
		}
	}

	@State(Scope.Thread)
	public static class BenchmarkStateStringOneInternalized extends BenchmarkStateString {

		@Setup(Level.Iteration)
		@Override
		public void initializeVariable() {
			super.initializeVariable();
			name = name.intern();
		}
	}

	@State(Scope.Thread)
	public static class BenchmarkStateStringInternalized extends BenchmarkStateStringOneInternalized {

		@Setup(Level.Iteration)
		@Override
		public void initializeVariable() {
			super.initializeVariable();
			value = value.intern();
		}
	}

	@State(Scope.Thread)
	public static class BenchmarkStateStatic extends BenchmarkStateSimilar {
		@Setup(Level.Iteration)
		@Override
		public void initializeVariable() {
			super.initializeVariable();
			name = name.intern();
		}
	}

	@Benchmark
	@Measurement(iterations = 1000)
	@BenchmarkMode(Mode.Throughput)
	public void benchmarkFindChildIdentity(BenchmarkStateStatic state, Blackhole blackhole) {
		blackhole.consume(state.element.findChild(el -> el.getName() == state.name));
	}

	@Benchmark
	@Measurement(iterations = 1000)
	@BenchmarkMode(Mode.Throughput)
	public void benchmarkFindChildEquality(BenchmarkStateSimilar state, Blackhole blackhole) {
		blackhole.consume(state.element.findChild(el -> (state.name).equals(el.getName())));
	}

	@Benchmark
	@Measurement(iterations = 1000)
	@BenchmarkMode(Mode.Throughput)
	public void benchmarkStringEquals(BenchmarkStateString state, Blackhole blackhole) {
		blackhole.consume(state.name.equals(state.value));
	}

	@Benchmark
	@Measurement(iterations = 1000)
	@BenchmarkMode(Mode.Throughput)
	public void benchmarkStringIdentity(BenchmarkStateString state, Blackhole blackhole) {
		blackhole.consume(state.name.intern() == state.value.intern());
	}

	@Benchmark
	@Measurement(iterations = 1000)
	@BenchmarkMode(Mode.Throughput)
	public void benchmarkStringIdentityOneInternalized(BenchmarkStateStringOneInternalized state, Blackhole blackhole) {
		blackhole.consume(state.name == state.value.intern());
	}

	@Benchmark
	@Measurement(iterations = 1000)
	@BenchmarkMode(Mode.Throughput)
	public void benchmarkStringIdentityInternalized(BenchmarkStateStringInternalized state, Blackhole blackhole) {
		blackhole.consume(state.name == state.value);
	}

	@Benchmark
	@Measurement(iterations = 1000)
	@BenchmarkMode(Mode.Throughput)
	public void benchmarkStringIdentityInternalizedOverkill(BenchmarkStateStringInternalized state, Blackhole blackhole) {
		blackhole.consume(state.name.intern() == state.value.intern());
	}
}
