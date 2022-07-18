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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * <code>Path</code> holds predicates that need to be matched to find child (element within a tree of elements).
 */
public class Path {

	/**
	 * Exception thrown if it is not possible to parse String into path
	 */
	public static class PathFormatException extends Throwable {

		public PathFormatException(String message) {
			super(message);
		}

	}

	/**
	 * Method parses <code>String</code> into path
	 * @param text
	 * @return
	 * @throws PathFormatException
	 */
	public static @NotNull Path parse(@NotNull String text) throws PathFormatException {
		if (!text.startsWith("/")) {
			throw new PathFormatException("Path cannot start without '/'");
		}

		State state = new State(text);
		state.text = state.text.substring(1);
		List<ElementMatcher> matchers = new ArrayList<>();
		while(!state.text.isEmpty()) {
			matchers.add(ElementMatcher.parse(state));
			if (state.text.startsWith("/")) {
				state.text = state.text.substring(1);
			} else if (!state.text.isEmpty()) {
				throw new PathFormatException("Path cannot have empty element name");
			}
		}
		if (matchers.isEmpty()) {
			throw new PathFormatException("Path cannot be empty!");
		}
		return new Path(matchers.toArray(ElementMatcher[]::new));
	}

	/**
	 * Simplified for to ease transition from String[] to Path.
	 * @param matcherStrings
	 * @return
	 * @throws PathFormatException
	 */
	public static @NotNull Path of(@NotNull String... matcherStrings) throws PathFormatException {
		ArrayList<ElementMatcher> matchers = new ArrayList<>();
		for (String str : matcherStrings) {
			matchers.add(ElementMatcher.parse(str));
		}
		return new Path(matchers.toArray(ElementMatcher[]::new));
	}

	/**
	 * Method for creating <code>Path</code> from <code>ElementMatcher</code>s.
	 * Each matcher is responsible for filtering nodes at one level of the elements tree.
	 * @param matchers
	 * @return
	 */
	public static @NotNull Path of(@NotNull ElementMatcher... matchers) {
		if (matchers.length == 0) {
			throw new IllegalArgumentException("At least 1 matcher is required!");
		}
		return new Path(matchers);
	}

	protected static class State {
		String text;
		public State(String text) {
			this.text = text;
		}
	}

	private final ElementMatcher[] matchers;

	private Path(ElementMatcher[] matchers) {
		this.matchers = matchers;
	}

	/**
	 * Method filters passed element children (and subchildren) and returns first child matching this path
	 * @param element
	 * @return
	 */
	public @Nullable Element evaluate(@NotNull Element element) {
		Element el = element;
		if (!matchers[0].test(el)) {
			return null;
		}
		for (int i = 1; i < matchers.length; i++) {
			if (el == null) {
				return null;
			}
			el = el.findChild(matchers[i]);
		}
		return el;
	}

	/**
	 * Method filters passed element children (and subchildren) and returns children matching this path
	 * @param element
	 * @return
	 */
	public @NotNull List<Element> evaluateAll(@NotNull Element element) {
		Objects.requireNonNull(element);
		if (element == null) {
			// FIXME: I'm not sure about this.. maybe assert would be a better option..
			return Collections.emptyList();
		}
		if (!matchers[0].test(element)) {
			return Collections.emptyList();
		}

		List<Element> result = List.of(element);
		for (int i = 1; i < matchers.length; i++) {
			List<Element> tmp = new ArrayList<>();
			for (Element el : result) {
				tmp.addAll(el.findChildren(matchers[i]));
			}
			result = tmp;
		}
		return result;
	}

	/**
	 * Method for building a new path by adding additional element matcher to the end of the path
	 */
	public Path then(ElementMatcher matcher) {
		ElementMatcher[] newMatchers =  Arrays.copyOf(matchers, matchers.length + 1);
		newMatchers[matchers.length] = matcher;
		return new Path(newMatchers);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (ElementMatcher matcher : matchers) {
			sb.append("/");
			matcher.toStringBuilder(sb);
		}
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof Path) {
			return Arrays.equals(matchers, ((Path) obj).matchers);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(matchers);
	}
	
}
