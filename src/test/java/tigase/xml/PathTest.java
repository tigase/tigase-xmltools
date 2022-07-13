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

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class PathTest {

	@Test
	public void testParseAndSerialize() throws Path.PathFormatException {
		Path path = Path.of(new ElementMatcher("command", "http://jabber.org/protocol/command",
											   List.of(new ElementMatcher.Attribute("status", "executing"))))
				.then(new ElementMatcher("x", "jabber:x:data", Collections.emptyList()));
		String str1 = path.toString();
		Path path1 = Path.parse(str1);
		String str2 = path1.toString();
		assertEquals(str1, str2);
	}

	@Test
	public void testMatching() {
		Path path = Path.of(new ElementMatcher().setName("command")
									.setXMLNS("http://jabber.org/protocol/command")
									.addAttribute("status", "executing"))
				.then(new ElementMatcher().setName("x").setXMLNS("jabber:x:data"));
		Element el = new Element("command").setXMLNS("http://jabber.org/protocol/command")
				.setAttribute("status", "executing")
				.addChild(new Element("x").setXMLNS("jabber:x:data"));
		Element result = path.evaluate(el);
		assertNotNull(result);
		assertEquals("x", result.getName());
		el.findChild("x").setXMLNS("test");
		assertNull(path.evaluate(el));
	}
}
