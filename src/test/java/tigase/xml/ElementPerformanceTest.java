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

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

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
		@Param({ "1", "5", "10"}) //, "20" })
		int maxChild;
		String name = "message";

		///@Param({"false", "true"})
		//boolean randomChild = true;

		@Param({"ArrayList", "LinkedList"})
		String clazz;

		@Setup(Level.Trial)
		public void initializeElement() {
			switch (clazz) {
				case "ArrayList" -> Element.listSupplier = ArrayList::new;
				case "LinkedList" -> Element.listSupplier = LinkedList::new;
			}

//			i = randomChild ? (new Random().nextInt(1, maxChild + 1)) : 1;
			element = new Element("root");
			for (int childIdx = 0; childIdx < maxChild - 1; childIdx++) {
				element.addChild(new Element("child-" + childIdx + 1).withElement("subchild-1", null));
			}
			name = new String(getNameToFind());
			element.addChild(new Element(name));
		}
		
		public String getNameToFind() {
			return "message";
		}
	}

	@State(Scope.Thread)
	public static class BenchmarkStateStatic extends BenchmarkState {
		@Setup(Level.Trial)
		@Override
		public void initializeElement() {
			super.initializeElement();
			name = name.intern();
		}

	}

	@State(Scope.Thread)
	public static class BenchmarkStateDedup extends BenchmarkState {
		@Param({"false", "true"})
		boolean willDedup;

		@Setup(Level.Trial)
		public void initializeElement() {
			super.initializeElement();
			String dedupName = DEDUP_ELEM_NAME.get(name);
			if (dedupName != null) {
//				name = dedupName;
				((Element) element.children.get(element.children.size()-1)).name = dedupName;
			}
		}

		@Override
		public String getNameToFind() {
			if (willDedup) {
				return "message";
			} else {
				return "messag3";
			}
		}
	}

	//
	@Benchmark
	@Measurement(iterations = 1000)
	@BenchmarkMode(Mode.Throughput)
	public void benchmarkFindChildIdentity(BenchmarkStateStatic state, Blackhole blackhole) {
		blackhole.consume(state.element.findChild(el -> el.getName() == state.name));
	}
	@Benchmark
	@Measurement(iterations = 1000)
	@BenchmarkMode(Mode.Throughput)
	public void benchmarkFindChildEquality(BenchmarkState state, Blackhole blackhole) {
		blackhole.consume(state.element.findChild(el -> (state.name).equals(el.getName())));
	}

	private static final Map<String,String> DEDUP_ELEM_NAME = List.of("message", "iq", "presence", "query", "pubsub", "body").stream().collect(Collectors.toMap(Function.identity(), Function.identity()));
//	private static String dedupName(String elemName) {
//		String result = DEDUP_ELEM_NAME.get(elemName);
//		if (result != null) {
//			return result;
//		}
//		return elemName;
//	}

	@Benchmark
	@Measurement(iterations = 1000)
	@BenchmarkMode(Mode.Throughput)
	public void benchmarkFindChildDedupEquality(BenchmarkStateDedup state, Blackhole blackhole) {
		String dedupName = DEDUP_ELEM_NAME.get(state.name);
		if (dedupName != null) {
			blackhole.consume(state.element.findChild(el -> dedupName == el.getName()));
		} else {
			blackhole.consume(state.element.findChild(el -> state.name.equals(el.getName())));
		}
	}

	@Benchmark
	@Measurement(iterations = 1000)
	@BenchmarkMode(Mode.Throughput)
	public void benchmarkFindChildDedupEqualityPossibleOptimized(BenchmarkStateDedup state, Blackhole blackhole) {
		if (state.element.children.size() < 5) {
			blackhole.consume(state.element.findChild(el -> state.name.equals(el.getName())));
		} else {
			String dedupName = DEDUP_ELEM_NAME.get(state.name);
			if (dedupName != null) {
				blackhole.consume(state.element.findChild(el -> dedupName == el.getName()));
			} else {
				blackhole.consume(state.element.findChild(el -> state.name.equals(el.getName())));
			}
		}
	}
	
//	@Benchmark
//	@Measurement(iterations = 1000)
//	@BenchmarkMode(Mode.Throughput)
//	public void benchmarkFindChildStreamIdentity(BenchmarkState state, Blackhole blackhole) {
//		blackhole.consume(state.element.findChildStream(el -> el.getName() == state.name));
//	}
//
//	@Benchmark
//	@Measurement(iterations = 1000)
//	@BenchmarkMode(Mode.Throughput)
//	public void benchmarkFindChildStreamDirectIdentity(BenchmarkState state, Blackhole blackhole) {
//		blackhole.consume(state.element.findChildStreamDirect(el -> el.getName() == state.name));
//	}

//	@Benchmark
//	@Measurement(iterations = 1000)
//	@BenchmarkMode(Mode.Throughput)
//	public void benchmarkFindChildNewMatcher(BenchmarkState state, Blackhole blackhole) {
//		blackhole.consume(state.element.findChild(Element.Matcher.byName(state.name)));
//	}
//
//	@Benchmark
//	@Measurement(iterations = 1000)
//	@BenchmarkMode(Mode.Throughput)
//	public void benchmarkFindChildGetChildStaticStr(BenchmarkStateStatic state, Blackhole blackhole) {
//		blackhole.consume(state.element.getChildStaticStr(state.name));
//	}

//	@Benchmark
//	@Measurement(iterations = 1000)
//	@BenchmarkMode(Mode.Throughput)
//	public void benchmarkFindChildGetChildStaticStrOptional(BenchmarkStateStatic state, Blackhole blackhole) {
//		blackhole.consume(Optional.ofNullable(state.element.getChildStaticStr(state.name)).orElse(null));
//	}

//	@Benchmark
//	@Measurement(iterations = 1000)
//	@BenchmarkMode(Mode.Throughput)
//	public void benchmarkFindChildOldEmpty(BenchmarkState state, Blackhole blackhole) {
//		blackhole.consume(Optional.ofNullable(state.element.getChildStaticStr(state.name)).orElse(null));
//	}

//	@Benchmark
//	@Measurement(iterations = 1000)
//	@BenchmarkMode(Mode.Throughput)
//	public void benchmarkFindChildOptionalMapperAndGetName(BenchmarkState state, Blackhole blackhole) {
//		blackhole.consume(state.element.findChild(state.name, null).map(Element::getName).orElse(null));
//	}

//	@Benchmark
//	@Measurement(iterations = 1000)
//	@BenchmarkMode(Mode.Throughput)
//	public void benchmarkFindChildNullableAndGetName(BenchmarkState state, Blackhole blackhole) {
//		final Element child = state.element.getChild(state.name, null);
//		blackhole.consume(child != null ? child.getName() : null);
//	}


	// --------------- LEAVE TI
//	@Benchmark
//	@Measurement(iterations = 100)
//	@BenchmarkMode(Mode.AverageTime)
//	public void benchmarkFlatMapManual(BenchmarkState state, Blackhole blackhole) {
//		List<Element> children = state.element.getChildren();
//		List<Element> result = new LinkedList<>();
//		for (Element child : children) {
//			for (Element subChild : child.getChildren()) {
//				if (subChild.getName() == "subchild-1") {
//					result.add(subChild);
//				}
//			}
//		}
//		blackhole.consume(result);
//	}
//
//	@Benchmark
//	@Measurement(iterations = 100)
//	@BenchmarkMode(Mode.AverageTime)
//	public void benchmarkFlatMap(BenchmarkState state, Blackhole blackhole) {
//		List<Element> children = state.element.flatMapChildren(Element::getChildren);
//		List<Element> children2 = new LinkedList<>();
//		for (Element child : children) {
//			if (child.getName().equals("subchild-1")) {
//				children2.add(child);
//			}
//		}
//		blackhole.consume(children2);
//	}
	// --------------- LEAVE TI


//	@Benchmark
//	@Measurement(iterations = 100)
//	@BenchmarkMode(Mode.AverageTime)
//	public void benchmarkFlatMapStreamToListGetChildrenStream(BenchmarkState state, Blackhole blackhole) {
//		List<Element> children = state.element.getChildren()
//				.stream()
//				.map(Element::getChildren)
//				.flatMap(List::stream)
//				.filter(el -> el.getName().equals("subchild-1"))
//				.toList();
//		blackhole.consume(children);
//	}
//
//	@Benchmark
//	@Measurement(iterations = 100)
//	@BenchmarkMode(Mode.AverageTime)
//	public void benchmarkFlatMapStreamToListStreamChildren(BenchmarkState state, Blackhole blackhole) {
//		List<Element> children = state.element.streamChildren()
//				.flatMap(Element::streamChildren)
//				.filter(el -> el.getName().equals("subchild-1"))
//				.toList();
//		blackhole.consume(children);
//	}
//
//	@Benchmark
//	@Measurement(iterations = 100)
//	@BenchmarkMode(Mode.AverageTime)
//	public void benchmarkFlatMapStreamDirectToListStreamChildrenDirect(BenchmarkState state, Blackhole blackhole) {
//		List<Element> children = state.element.streamChildrenDirect()
//				.flatMap(Element::streamChildrenDirect)
//				.filter(el -> el.getName().equals("subchild-1"))
//				.toList();
//		blackhole.consume(children);
//	}
//
//	@Benchmark
//	@Measurement(iterations = 100)
//	@BenchmarkMode(Mode.AverageTime)
//	public void benchmarkFlatMapStreamToArray(BenchmarkState state, Blackhole blackhole) {
//		Element[] children = state.element.streamChildren()
//				.flatMap(Element::streamChildren)
//				.filter(el -> el.getName().equals("subchild-1"))
//				.toArray(Element[]::new);
//		blackhole.consume(children);
//	}
//
//	@Benchmark
//	@Measurement(iterations = 100)
//	@BenchmarkMode(Mode.AverageTime)
//	public void benchmarkMapStreamToList(BenchmarkState state, Blackhole blackhole) {
//		List<String> children = state.element.streamChildren()
//				.filter(el -> el.getName().equals(state.name))
//				.map(Element::getName)
//				.toList();
//		blackhole.consume(children);
//	}

	// Benchmarking .getChildMethod: null vs optional

//	@Benchmark
//	@Measurement(iterations = 1000)
//	@BenchmarkMode(Mode.Throughput)
//	public void benchmarkGetChildAndNullComparison(BenchmarkState state, Blackhole blackhole) {
//		Element el = state.element.getChild(state.name);
//		if (el != null) {
//			blackhole.consume(el.getChildren());
//		}
//	}
//
//	@Benchmark
//	@Measurement(iterations = 1000)
//	@BenchmarkMode(Mode.Throughput)
//	public void benchmarkGetChildAndOptional(BenchmarkState state, Blackhole blackhole) {
//		Optional.ofNullable(state.element.getChild(state.name)).map(Element::getChildren).ifPresent(blackhole::consume);
//	}

//	@State(Scope.Thread)
//	public static class BenchmarkState10 {
//		LinkedList<Element> list = Stream.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
//				.map(i -> new Element("child-" + i))
//				.collect(Collectors.toCollection(LinkedList::new));
//
//		@Setup(Level.Trial)
//		public void initialize() {
//		}
//	}
//
//	@Benchmark
//	@Measurement(iterations = 100)
//	@BenchmarkMode(Mode.AverageTime)
//	public void benchmarkFindChildLinkedList(BenchmarkState10 state, Blackhole blackhole) {
//		for (int i=0; i<1000; i++) {
//			Element child = null;
//			for (Element el : state.list) {
//				if ("child-5".equals(el.getName())) {
//					child = el;
//					break;
//				}
//			}
//			blackhole.consume(child);
//		}
//	}
//
//	@State(Scope.Thread)
//	public static class BenchmarkState11 {
//		ArrayList<Element> list = Stream.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
//				.map(i -> new Element("child-" + i))
//				.collect(Collectors.toCollection(ArrayList::new));
//
//		@Setup(Level.Trial)
//		public void initialize() {
//		}
//	}
//
//	@Benchmark
//	@Measurement(iterations = 100)
//	@BenchmarkMode(Mode.AverageTime)
//	public void benchmarkFindChildArrayList(BenchmarkState11 state, Blackhole blackhole) {
//		for (int i=0; i<1000; i++) {
//			Element child = null;
//			for (Element el : state.list) {
//				if ("child-5".equals(el.getName())) {
//					child = el;
//					break;
//				}
//			}
//			blackhole.consume(child);
//		}
//	}
}
