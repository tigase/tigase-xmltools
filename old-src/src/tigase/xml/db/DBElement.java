/*  Package Jabber Server
 *  Copyright (C) 2001, 2002, 2003, 2004, 2005
 *  "Artur Hefczyc" <artur.hefczyc@gmail.com>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software Foundation,
 *  Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * $Rev$
 * Last modified by $Author$
 * $Date$
 */
package tigase.xmpp.rep.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import tigase.xml.Element;

/**
 * Describe class RepositoryElement here.
 *
 *
 * Created: Tue Oct 26 22:01:47 2004
 *
 * @author <a href="mailto:artur.hefczyc@gmail.com">Artur Hefczyc</a>
 * @version $Rev$
 */
public class RepositoryElement extends Element<RepositoryElement> {

  public RepositoryElement(String argName) {
    super(argName);
  }

  public RepositoryElement(String argName, String attname, String attvalue) {
    super(argName, null,
      new String[] {attname}, new String[] {attvalue});
  }

  public RepositoryElement(String argName, String argCData,
    StringBuilder[] att_names, StringBuilder[] att_values) {
    super(argName, argCData, att_names, att_values);
  }

  public final String formatedString(int indent, int step) {
    StringBuilder result = new StringBuilder();
    result.append("\n");
    for (int i = 0; i < indent; i++) {
      result.append(" ");
    }
    result.append("<"+name);
    if (attributes != null) {
      for (String key : attributes.keySet()) {
        result.append(" "+key+"='"+attributes.get(key)+"'");
      } // end of for ()
    } // end of if (attributes != null)
    String childrenStr = childrenFormatedString(indent+step, step);
    if (cdata != null || childrenStr.length() > 0) {
      result.append(">");
      if (cdata != null) {
        result.append(cdata);
      } // end of if (cdata != null)
      result.append(childrenStr);
      result.append("\n");
      for (int i = 0; i < indent; i++) {
        result.append(" ");
      }
      result.append("</"+name+">");
    } else {
      result.append("/>");
    }
    return result.toString();
  }

  public final String childrenFormatedString(int indent, int step) {
    StringBuilder result = new StringBuilder();
    if (children != null) {
      synchronized (children) {
        for (RepositoryElement child : children) {
          result.append(child.formatedString(indent, step));
        } // end of for ()
      }
    } // end of if (child != null)
    return result.toString();
  }

  public final RepositoryElement getSubnode(String name) {
    if (children == null) {
      return null;
    } // end of if (children == null)
    synchronized (children) {
      for (RepositoryElement elem : children) {
        if (elem.getName().equals("node") &&
          elem.getAttribute("name").equals(name)) {
          return elem;
        } //
      } // end of for (RepositoryElement node : children)
    }
    return null;
  }

  public final String[] getSubnodes() {
    if (children == null || children.size() == 1) {
      return null;
    } // end of if (children == null)
    // Minus <map/> element
    String[] result = new String[children.size()-1];
    synchronized (children) {
      int idx = 0;
      for (RepositoryElement elem : children) {
        if (elem.getName().equals("node")) {
          result[idx++] = elem.getAttribute("name");
        } //
      } // end of for (RepositoryElement node : children)
    }
    return result;
  }

  public final RepositoryElement findNode(String nodePath) {
    StringTokenizer strtok = new StringTokenizer(nodePath, "/", false);
    if (!getName().equals("node") ||
      !getAttribute("name").equals(strtok.nextToken())) {
      return null;
    } // end of if (!strtok.nextToken().equals(child.getName()))
    RepositoryElement node = this;
    while (strtok.hasMoreTokens() && node != null) {
      node = node.getSubnode(strtok.nextToken());
    } // end of while (strtok.hasMoreTokens())
    return node;
  }

  public final RepositoryElement buildNodesTree(String nodePath) {
    StringTokenizer strtok = new StringTokenizer(nodePath, "/", false);
    RepositoryElement node = this;
    while (strtok.hasMoreTokens()) {
      String token = strtok.nextToken();
      RepositoryElement tmp = node.getSubnode(token);
      if (tmp != null) {
        node = tmp;
      } // end of if (node.getSubnode() != null)
      else {
        if (token.equals("") || token.equals("null")) {
          return null;
        } // end of if (token.equals("") || token.equals("null"))
        node = node.newSubnode(token);
      } // end of if (node.getSubnode() != null) else
    } // end of while (strtok.hasMoreTokens())
    return node;
  }

  public final RepositoryElement newSubnode(String name) {
    RepositoryElement node =
      new RepositoryElement("node", "name", name);
    node.addChild(new RepositoryElement("map"));
    addChild(node);
    return node;
  }

  public final RepositoryElement findEntry(String key) {
    RepositoryElement result = null;
    ArrayList<RepositoryElement> entries = getChild("map").getChildren();
    if (entries != null) {
      synchronized (entries) {
        for (RepositoryElement elem : entries) {
          if (elem.getAttribute("key").equals(key)) {
            result = elem;
            break;
          } //
        } // end of for (RepositoryElement node : children)
      }
    }
    return result;
  }

  public final void removeEntry(String key) {
    RepositoryElement result = null;
    ArrayList<RepositoryElement> entries = getChild("map").getChildren();
    if (entries != null) {
      synchronized (entries) {
        for (Iterator<RepositoryElement> it = entries.iterator();
             it.hasNext();) {
          if (it.next().getAttribute("key").equals(key)) {
            it.remove();
            break;
          } //
        } // end of for (RepositoryElement node : children)
      }
    }
  }

  public final String[] getEntryKeys() {
    ArrayList<RepositoryElement> entries = getChild("map").getChildren();
    if (entries != null) {
      String[] result = null;
      synchronized (entries) {
        result = new String[entries.size()];
        for (int i = 0; i < result.length; i++) {
          result[i] = entries.get(i).getAttribute("key");
        } // end of for (int i = 0; i < result.length; i++)
      }
      return result;
    }
    return null;
  }

  public final RepositoryElement getEntry(String key) {
    RepositoryElement result = findEntry(key);
    if (result == null) {
      result = new RepositoryElement("entry","key", key);
      getChild("map").addChild(result);
    } // end of if (result == null)
    return result;
  }

  public final void setEntry(String key, String value) {
    RepositoryElement entry = getEntry(key);
    entry.setAttribute("value", value);
  }

  public final void setEntry(String key, String values[]) {
    RepositoryElement entry = getEntry(key);
    for (String val : values) {
      entry.addChild(new RepositoryElement("item", "value", val));
    } // end of for (String val : values)
  }

  public final String getEntryValue(String key, String def) {
    RepositoryElement entry = findEntry(key);
    String result = null;
    if (entry != null) {
      result = entry.getAttribute("value");
    } // end of if (entry != null)
    return result != null ? result : def;
  }

  public final String[] getEntryValues(String key) {
    RepositoryElement entry = findEntry(key);
    if (entry != null) {
      ArrayList<RepositoryElement> items = entry.getChildren();
      if (items != null) {
        String[] result = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
          result[i] = items.get(i).getAttribute("value");
        } // end of for (int i = 0; i < items.size(); i++)
        return result;
      } // end of if (items != null)
    } // end of if (entry != null)
    return null;
  }

} // RepositoryElement
