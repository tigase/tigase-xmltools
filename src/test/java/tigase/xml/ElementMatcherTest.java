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

import static org.junit.Assert.*;

public class ElementMatcherTest {

	private static final String DATA = "parent[@xmlns='xmpp:1']";

	@Test
	public void testParseAndSerialize() throws Path.PathFormatException {
		ElementMatcher matcher = ElementMatcher.parse(DATA);
		assertEquals(DATA, matcher.toString());
	}

	@Test
	public void testBuilderAndSerialize() throws Path.PathFormatException {
		ElementMatcher matcher = new ElementMatcher().setName("parent").setXMLNS("xmpp:1");
		assertEquals(DATA, matcher.toString());
	}

	@Test
	public void testMatcher() throws Path.PathFormatException {
		ElementMatcher matcher = ElementMatcher.parse(DATA);
		Element el = new Element("parent").setXMLNS("xmpp:1");
		assertTrue(matcher.test(el));
		el = new Element("parent");
		assertFalse(matcher.test(el));
		el = new Element("par").setXMLNS("xmpp:1");
		assertFalse(matcher.test(el));
	}
}
