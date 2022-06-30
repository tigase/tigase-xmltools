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

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Ignore
public class ElementAttributesPerformanceTest {

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
		String attributeName;

		@Param({"IdentityMap", "HashMap", "DedupStaticHashMap", "DedupHashHashMap"})
		String attributesClass;

		@Param({"Dynamic", "Static"})
		String attributeType;

		@Setup(Level.Trial)
		public void initializeElement() {
			Element.listSupplier = ArrayList::new;
			switch (attributesClass) {
				case "IdentityMap" -> Element.attributesProvider = Element.AttributesIdentityMap::new;
				case "HashMap" -> Element.attributesProvider = Element.AttributesHashMap::new;
				case "DedupStaticHashMap" -> Element.attributesProvider = Element.AttributesDedupStaticHashMap::new;
				case "DedupHashHashMap" -> Element.attributesProvider = Element.AttributesDedupHashHashMap::new;
			}
			switch (attributeType) {
				case "Dynamic" -> attributeName = "test-" + new Random().nextInt();
				case "Static" -> attributeName = "id";
			};

			if (attributesClass.equals("IdentityMap")) {
				attributeName = attributeName.intern();
			}
			element = new Element("root").withAttribute(new String(attributeName), "true");
		}

	}

	@Benchmark
	@Measurement(iterations = 1000)
	@BenchmarkMode(Mode.Throughput)
	public void benchmarkAttributeGet(BenchmarkState state, Blackhole blackhole) {
		blackhole.consume(state.element.getAttribute(state.attributeName));
	}
	@Benchmark
	@Measurement(iterations = 1000)
	@BenchmarkMode(Mode.Throughput)
	public void benchmarkAttributeGetIdentity(BenchmarkState state, Blackhole blackhole) {
		blackhole.consume(state.element.getAttributeStaticStr(state.attributeName));
	}

	@Benchmark
	@Measurement(iterations = 1000)
	@BenchmarkMode(Mode.Throughput)
	public void benchmarkAttributePut(BenchmarkState state, Blackhole blackhole) {
		state.element.setAttribute(state.attributeName, state.attributeName);
		blackhole.consume(true);
	}
	@Benchmark
	@Measurement(iterations = 1000)
	@BenchmarkMode(Mode.Throughput)
	public void benchmarkAttributeRemove(BenchmarkState state, Blackhole blackhole) {
		state.element.removeAttribute(state.attributeName);
		blackhole.consume(true);
	}

}
