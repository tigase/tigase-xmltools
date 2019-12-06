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

import org.junit.Assert;
import org.junit.Test;

public class XMLUtilsTest {

	@Test
	public void testEscape() {
		Assert.assertEquals("&lt;&lt;&lt;&lt;", XMLUtils.escape("<<<<"));
		Assert.assertEquals("&lt;&quot;&amp;&gt;&lt;foo&gt;;&apos;", XMLUtils.escape("<\"&><foo>;'"));
		Assert.assertEquals("&lt;&amp;quot;&amp;amp;&amp;gt;&amp;lt;foo&gt;;&apos;",
							XMLUtils.escape("<&quot;&amp;&gt;&lt;foo>;'"));
	}

	/**
	 * Test for error described in <a href="https://projects.tigase.org/issues/242">Bug #242</a>.
	 */
	@Test
	public void testEscapeUnescape() {
		String be = "<&quot;&amp;&gt;&lt;foo>;'";
		Assert.assertEquals(be, XMLUtils.unescape(XMLUtils.escape(be)));

	}

	@Test
	public void testUnescape() {
		Assert.assertEquals("<<<<", XMLUtils.unescape("&lt;&lt;&lt;&lt;"));
		Assert.assertEquals("<\"&><foo>;'", XMLUtils.unescape("&lt;&quot;&amp;&gt;&lt;foo&gt;;&apos;"));
		Assert.assertEquals("<\"&amp;><foo>;'", XMLUtils.unescape("&lt;&quot;&amp;amp;&gt;&lt;foo&gt;;&apos;"));
	}

}
