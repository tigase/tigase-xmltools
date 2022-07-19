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

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.Map.entry;

@Ignore
public class ElementPerformanceTest {

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
	public static class BenchmarkState {
		Element element;
		@Param({"1", "5", "10"}) //, "20" })
		int maxChild;
		String name = "message";

		///@Param({"false", "true"})
		//boolean randomChild = true;

		@Setup(Level.Trial)
		public void initializeElement() {
//			i = randomChild ? (new Random().nextInt(1, maxChild + 1)) : 1;
			element = new Element("root");
			for (int childIdx = 0; childIdx < maxChild - 1; childIdx++) {
				element.addChild(new Element("child-" + childIdx + 1).addChild(new Element("subchild-1")));
			}
			name = new String(getNameToFind());
			element.addChild(new Element(name));
		}

		public String getNameToFind() {
			return "message";
		}
	}
	
//	@Benchmark
//	@Measurement(iterations = 1000)
//	@BenchmarkMode(Mode.Throughput)
//	public void benchmarkFindChildEquality(BenchmarkState state, Blackhole blackhole) {
//		blackhole.consume(state.element.findChild(el -> (state.name).equals(el.getName())));
//	}

	public static final String[] ATTRIBUTE_NAMES = { "id", "name", "from", "to", "xmlns" };

	@Benchmark
	@Measurement(iterations = 1000)
	@BenchmarkMode(Mode.Throughput)
	public void benchmarkElementWithAttributesCreationBuilder(Blackhole blackhole) {
		Element test = new Element("test");
		for (String name : ATTRIBUTE_NAMES) {
			{
				test.setAttribute(name, name);
			}
		}
		blackhole.consume(test);
	}

	@Benchmark
	@Measurement(iterations = 1000)
	@BenchmarkMode(Mode.Throughput)
	public void benchmarkElementWithAttributesCreationMixedStatic(Blackhole blackhole) {
		blackhole.consume(new Element("test").setAttributes(ATTRIBUTE_NAMES,ATTRIBUTE_NAMES));
	}

	private static final Map<String,String> ATTRIBUTES_MAP = Map.of("id", "id", "name", "name", "from", "from", "to", "to", "xmlns", "xmlns");
	@Benchmark
	@Measurement(iterations = 1000)
	@BenchmarkMode(Mode.Throughput)
	public void benchmarkElementWithAttributesCreationDynamicMap(Blackhole blackhole) {
		blackhole.consume(new Element("test").setAttributes(
				Map.of("id", "id", "name", "name", "from", "from", "to", "to", "xmlns", "xmlns")));
	}

	@Benchmark
	@Measurement(iterations = 1000)
	@BenchmarkMode(Mode.Throughput)
	public void benchmarkElementWithAttributesCreationDynamicMapOfEntries(Blackhole blackhole) {
		blackhole.consume(new Element("test").setAttributes(
				Map.ofEntries(entry("id", "id"), entry("name", "name"), entry("from", "from"), entry("to", "to"),
							  entry("xmlns", "xmlns"))));
	}

	@Benchmark
	@Measurement(iterations = 1000)
	@BenchmarkMode(Mode.Throughput)
	public void benchmarkElementWithAttributesCreationStaticMap(Blackhole blackhole) {
		blackhole.consume(new Element("test").setAttributes(ATTRIBUTES_MAP));
	}

}
