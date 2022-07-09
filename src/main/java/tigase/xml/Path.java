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

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Path {
	
	public static class PathFormatException extends Throwable {

	}

	public static Path parse(String text) throws PathFormatException {
		if (!text.startsWith("/")) {
			throw new PathFormatException();
		}

		State state = new State();
		state.text = text;
		List<ElementMatcher> matchers = new ArrayList<>();
		while(!state.text.isEmpty()) {
			state.text = state.text.substring(1);
			matchers.add(ElementMatcher.parse(state));
			if (state.text.startsWith("/")) {
				state.text = state.text.substring(1);
			} else if (!state.text.isEmpty()) {
				throw new PathFormatException();
			}
		}
		return new Path(matchers.toArray(ElementMatcher[]::new));
	}

	/**
	 * Simplified for to ease transition from String[] to Path.
	 * @param matcherStrings
	 * @return
	 * @throws PathFormatException
	 */
	public static Path of(String... matcherStrings) throws PathFormatException {
		ArrayList<ElementMatcher> matchers = new ArrayList<>();
		for (String str : matcherStrings) {
			State state = new State();
			state.text = str;
			matchers.add(ElementMatcher.parse(state));
		}
		return new Path(matchers.toArray(ElementMatcher[]::new));
	}

	public static Path of(ElementMatcher... matchers) {
		return new Path(matchers);
	}

	protected static class State {
		String text;
	}

	private final ElementMatcher[] matchers;

	private Path(ElementMatcher[] matchers) {
		this.matchers = matchers;
	}

	@Nullable
	public Element evaluate(Element element) {
		Element el = element;
		for (ElementMatcher predicate : matchers) {
			if (el == null) {
				return null;
			}
			el = el.findChild(predicate);
		}
		return el;
	}

	public List<Element> evaluateAll(Element element) {
		if (element == null) {
			// FIXME: I'm not sure about this.. maybe assert would be a better option..
			return Collections.emptyList();
		}

		List<Element> result = List.of(element);
		for (ElementMatcher predicate : matchers) {
			List<Element> tmp = new ArrayList<>();
			for (Element el : result) {
				tmp.addAll(el.findChildren(predicate));
			}
			result = tmp;
		}
		return result;
	}

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
