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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static tigase.xml.ElementFilters.name;

/**
 * Simple tests for Element class
 *
 * @author andrzej
 */
public class ElementTest {

	private static final String xmlns = "xmlns:1";
	private static final Path path = Path.of(new ElementMatcher("parent", null, Collections.emptyList()),
											 new ElementMatcher("child", null, Collections.emptyList()));
	private static final Path pathComplex = Path.of(new ElementMatcher("parent", null, Collections.emptyList()),
													new ElementMatcher("child", xmlns, List.of(new ElementMatcher.Attribute("child_att_name", "child_att_value"))));

	private String c_att_name = null;
	private String c_att_value = null;
	private Element child = null;
	private String p_att_name = null;
	private String p_att_value = null;
	private Element parent = null;
	private String value = null;
	
	@Before
	public void setUp() throws Exception {
		value = "correct-value";
		parent = new Element("parent");
		child = new Element("child").setXMLNS(xmlns);
		//multiple call to setCData to verify the value is correct
		child.setCData(value);
		child.setCData(value);
		child.setCData(value);
		parent.addChild(child);
		p_att_name = "parent_att_name";
		p_att_value = "parent_att_value";
		parent.setAttribute(p_att_name, p_att_value);
		c_att_name = "child_att_name";
		c_att_value = "child_att_value";
		child.setAttribute(c_att_name, c_att_value);
	}

	@After
	public void tearDown() throws Exception {
		parent = null;
		child = null;
		value = null;
		p_att_name = null;
		p_att_value = null;
		c_att_name = null;
		c_att_value = null;
	}

	/**
	 * Test of addAttribute method, of class Element.
	 */
	@Test
	public void testAddAttribute() {
		String attName = "key";
		String attValue = "value";

		parent.setAttribute(attName, attValue);
		assertEquals(attValue, parent.getAttribute(attName));
	}

	/**
	 * Test of addAttributes method, of class Element.
	 */
	@Test
	public void testAddAttributes() {
		Map<String, String> attrs = new HashMap<String, String>();

		parent.removeAttribute(p_att_name);
		attrs.put("key1", "val1");
		attrs.put("key2", "val2");
		parent.setAttributes(attrs);
		assertEquals(attrs.size(), parent.getAttributes().size());
		assertEquals(attrs, parent.getAttributes());
	}

	/**
	 * Test of addChild method, of class Element.
	 */
	@Test
	public void testAddChild() {
		Element instance = new Element("elem1").setCData("cdata1");

		parent.addChild(instance);
		assertEquals(instance, parent.findChild("elem1"));
	}

	/**
	 * Test of clone method, of class Element.
	 */
	@Test
	public void testClone() {
		Element result = parent.clone();

		assertEquals(parent, result);
	}

	/**
	 * Test of findChildAt method, of class Element.
	 */
	@Test
	public void testFindChildAt_Simple() {
		Element result = parent.findChildAt(path);

		assertEquals(child, result);
	}

	/**
	 * Test of findChildAt method, of class Element.
	 */
	@Test
	public void testFindChildAt_Complex() {
		Element result = parent.findChildAt(pathComplex);

		assertEquals(child, result);
	}

	/**
	 * Test of fincChildrenAt() method of class Element.
	 */
	@Test
	public void testFindChildrenAt_Simple() {
		List<Element> result = parent.findChildrenAt(path);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(child, result.get(0));
	}

	/**
	 * Test of fincChildrenAt() method of class Element.
	 */
	@Test
	public void testFindChildrenAt_Complex() {
		List<Element> result = parent.findChildrenAt(pathComplex);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(child, result.get(0));
	}

	/**
	 * Test of getAttribute method, of class Element.
	 */
	@Test
	public void testGetAttribute() {
		assertEquals(p_att_value, parent.getAttribute(p_att_name));
	}

	/**
	 * Test of getAttributeAt method, of class Element.
	 */
	@Test
	public void testGetAttributeAt() {
		assertEquals(c_att_value, parent.getAttributeAt(path, c_att_name));
	}

	/**
	 * Test of getCDataAt method, of class Element.
	 */
	@Test
	public void testGetCDataAt() {
		assertEquals(value, parent.getCDataAt(path));
	}


	/**
	 * Test of getCData method, of class Element.
	 */
	@Test
	public void testGetCData() {
		assertEquals(value, child.getCData());
	}

	/**
	 * Test of findChild method, of class Element.
	 */
	@Test
	public void testFindChild() {
		assertEquals(child, parent.findChild("child"));
		assertEquals(child, parent.findChild(new String("child")));
	}

	/**
	 * Test of getName method, of class Element.
	 */
	@Test
	public void testGetName() {
		assertEquals("parent", parent.getName());
	}

	/**
	 * Test of getXMLNS method, of class Element.
	 */
	@Test
	public void testGetXMLNS() {
		assertEquals(xmlns, child.getXMLNS());
	}

	/**
	 * Test of removeAttribute method, of class Element.
	 */
	@Test
	public void testRemoveAttribute() {
		parent.removeAttribute(p_att_name);
		assertNull(parent.getAttribute(p_att_name));
	}

	/**
	 * Test of removeChild method, of class Element.
	 */
	@Test
	public void testRemoveChild() {
		parent.removeChild(child);
		assertNull(parent.findChild("child"));
	}

	/**
	 * Test of setAttribute method, of class Element.
	 */
	@Test
	public void testSetAttribute() {
		testAddAttribute();
	}

	/**
	 * Test of setCData method, of class Element.
	 */
	@Test
	public void testSetCData() {
		testGetCData();
	}

	@Test
	public void testFindChildVariants() {
		Element element = new Element("root");
		for (int childIdx = 0; childIdx < 10; childIdx++) {
			element.addChild(new Element("child-" + childIdx).addChild(new Element("subchild-1")));
		}

		assertEquals("child-1", element.findChild(el -> "child-1".equals(el.getName())).getName());
		assertEquals("child-1", element.findChild("child-1").getName());
		assertEquals("child-1", element.findChild(name("child-1")).getName());
	}
}

