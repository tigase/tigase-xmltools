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
package tigase.xml.db;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import tigase.xml.DomBuilderHandler;
import tigase.xml.SimpleParser;
import tigase.xml.db.NodeNotFoundException;
import tigase.xml.db.NodeExistsException;

/**
 * Describe class XMLDB here.
 *
 *
 * Created: Tue Oct 26 15:27:33 2004
 *
 * @author <a href="mailto:artur.hefczyc@gmail.com">Artur Hefczyc</a>
 * @version $Rev$
 */
public class XMLDB {

  private String root_name = "root";
  private String node1_name = "node1";
  private DBElementComparator comparator =
    new DBElementComparator();
  private Lock lock = new ReentrantLock();

  private Logger log = Logger.getLogger("tigase.xml.db.XMLDB");
  private String dbFile = "xml_db.xml";
  private DBElement root = null;
  private ArrayList<DBElement> node1s = null;

  /**
   * Used only for searching for given node, do NOT use for any
   * other purpose.
   */
  private DBElement tmp_node1 = null;

  public XMLDB() {
    tmp_node1 = new DBElement(node1_name);
    loadDB();
  }

  public XMLDB(String root_name, String node1_name) {
    this.root_name = root_name;
    this.node1_name = node1_name;
    tmp_node1 = new DBElement(node1_name);
    loadDB();
  }

  protected void loadDB() {
    try {
      FileReader file = new FileReader(repositoryFile);
      char[] buff = new char[16*1024];
      SimpleParser parser = new SimpleParser();
      DomBuilderHandler<DBElement> domHandler =
        new DomBuilderHandler<DBElement>(DBElementFactory.getFactory());
      int result = -1;
      while((result = file.read(buff)) != -1) {
        parser.parse(domHandler, buff);
      }
      file.close();
      root = domHandler.getParsedElements().poll();
      node1s = root.getChildren();
      Collections.sort(node1s, comparator);
      log.finest(root.formatedString(0, 2));
    } catch (Exception e) {
      log.severe("Can't load repository file: "+e);
      log.severe("Create empty DB.");
      root = new DBElement(root_name);
      node1s = new ArrayList<DBElement>();
      root.setChildren(node1s);
    } // end of try-catch
  }

  protected void saveDB() {
    lock.lock();
    try {
      String buffer = root.formatedString(0, 1);
      FileWriter file = new FileWriter(repositoryFile, false);
      file.write(buffer, 0, buffer.length());
      file.close();
    } // end of try
    catch (Exception e) {
      log.severe("Can't save repository file: "+e);
    } // end of try-catch
    finally {
      lock.unlock();
    } // end of try-finally
  }

  protected final DBElement getNode1(String node1_id)
    throws NodeNotFoundException {
    lock.lock();
    try {
      tmp_node1.setAttribute(DBElement.NAME, node1_id);
      int idx = Collections.binarySearch(node1s, tmp_node1, comparator);
      if (idx >= 0) {
        return node1s.get(idx);
      } // end of if (idx >= 0)
      else {
        throw new NodeNotFoundException("Node1: " + node1_id +
          " has not been found in db.");
      } // end of if (idx >= 0) else
    } finally {
      lock.unlock();
    } // end of try-finally
  }

  protected final DBElement getNode(String node1_id, String subnode)
    throws NodeNotFoundException {
    DBElement node = getNode1(node1_id);
    if (subnode != null) {
      node = node.buildNodesTree(subnode);
    } // end of if (subnode != null)
    return node;
  }

  public void addNode1(String node1_id) throws NodeExistsException {
    lock.lock();
    try {
      tmp_node1.setAttribute(DBElement.NAME, node1_id);
      int idx = Collections.binarySearch(node1s, tmp_node1, comparator);
      if (idx >= 0) {
        throw new NodeExistsException("Node1: "+node1_id+" already exists.");
      } // end of if (idx >= 0)
      DBElement newNode1 = new DBElement(node1_name, DBElement.NAME, node1_id);
      newNode1.addChild(new DBElement(DBElement.MAP));
      node1s.add(newNode1);
      Collections.sort(node1s, comparator);
    } finally {
      lock.unlock();
    } // end of try-finally
  }

  public void removeNode1(String node1_id) throws NodeNotFoundException {
    lock.lock();
    try {
      tmp_node1.setAttribute(DBElement.NAME, node1_id);
      int idx = Collections.binarySearch(node1s, tmp_node1, comparator);
      if (idx >= 0) {
        node1s.remove(idx);
      } // end of if (idx >= 0)
      else {
        throw new NodeNotFoundException("Node1: " + node1_id +
          " has not been found in repository.");
      } // end of if (idx >= 0) else
    } finally {
      lock.unlock();
    } // end of try-finally
  }

  /**
   * Describe <code>setData</code> method here.
   *
   * @param node1_id a <code>String</code> value
   * @param subnode a <code>String</code> value
   * @param key a <code>String</code> value
   * @param value a <code>String</code> value
   */
  public void setData(String node1_id, String subnode, String key, String value)
    throws NodeNotFoundException {
    getNode(node1_id, subnode).setEntry(key, value);
    saveDB();
  }

  /**
   * Describe <code>setData</code> method here.
   *
   * @param node1_id a <code>String</code> value
   * @param key a <code>String</code> value
   * @param value a <code>String</code> value
   */
  public void setData(String node1_id, String key, String value)
    throws NodeNotFoundException {
    setData(node1_id, null, key, value);
  }

  /**
   * Describe <code>setDataList</code> method here.
   *
   * @param node1_id a <code>String</code> value
   * @param subnode a <code>String</code> value
   * @param key a <code>String</code> value
   * @param list a <code>String[]</code> value
   * @exception NodeNotFoundException if an error occurs
   */
  public void setDataList(String node1_id, String subnode, String key, String[] list)
    throws NodeNotFoundException {
    getNode(node1_id, subnode).setEntry(key, list);
    saveDB();
  }

  /**
   * Describe <code>getDataList</code> method here.
   *
   * @param node1_id a <code>String</code> value
   * @param subnode a <code>String</code> value
   * @param key a <code>String</code> value
   * @return a <code>String[]</code> value
   * @exception NodeNotFoundException if an error occurs
   */
  public String[] getDataList(String node1_id, String subnode, String key)
    throws NodeNotFoundException {
    return getNode(node1_id, subnode).getEntryValues(key);
  }

  /**
   * Describe <code>getData</code> method here.
   *
   * @param node1_id a <code>String</code> value
   * @param subnode a <code>String</code> value
   * @param key a <code>String</code> value
   * @param def a <code>String</code> value
   * @return a <code>String</code> value
   */
  public String getData(String node1_id, String subnode, String key, String def)
    throws NodeNotFoundException {
    return getNode(node1_id, subnode).getEntryValue(key, def);
  }

  /**
   * Describe <code>getData</code> method here.
   *
   * @param node1_id a <code>String</code> value
   * @param subnode a <code>String</code> value
   * @param key a <code>String</code> value
   * @return a <code>String</code> value
   */
  public String getData(String node1_id, String subnode, String key)
    throws NodeNotFoundException {
    return getData(node1_id, subnode, key, null);
  }

  /**
   * Describe <code>getData</code> method here.
   *
   * @param node1_id a <code>String</code> value
   * @param key a <code>String</code> value
   * @return a <code>String</code> value
   */
  public String getData(String node1_id, String key)
    throws NodeNotFoundException {
    return getData(node1_id, null, key, null);
  }

  /**
   * Describe <code>getSubnodes</code> method here.
   *
   * @param node1_id a <code>String</code> value
   * @param subnode a <code>String</code> value
   * @return a <code>String[]</code> value
   */
  public String[] getSubnodes(String node1_id, String subnode)
    throws NodeNotFoundException {
    return getNode(node1_id, subnode).getSubnodes();
  }

  /**
   * Describe <code>getSubnodes</code> method here.
   *
   * @param node1_id a <code>String</code> value
   * @return a <code>String[]</code> value
   */
  public String[] getSubnodes(String node1_id)
    throws NodeNotFoundException {
    return getSubnodes(node1_id, null);
  }

  /**
   * Describe <code>getKeys</code> method here.
   *
   * @param node1_id a <code>String</code> value
   * @param subnode a <code>String</code> value
   * @return a <code>String[]</code> value
   */
  public String[] getKeys(String node1_id, String subnode)
    throws NodeNotFoundException {
    return getNode(node1_id, subnode).getEntryKeys();
  }

  /**
   * Describe <code>getKeys</code> method here.
   *
   * @param node1_id a <code>String</code> value
   * @return a <code>String[]</code> value
   */
  public String[] getKeys(String node1_id)
    throws NodeNotFoundException {
    return getKeys(node1_id, null);
  }

  /**
   * Describe <code>removeData</code> method here.
   *
   * @param node1_id a <code>String</code> value
   * @param subnode a <code>String</code> value
   * @param key a <code>String</code> value
   */
  public void removeData(String node1_id, String subnode, String key)
    throws NodeNotFoundException {
    getNode(node1_id, subnode).removeEntry(key);
    saveDB();
  }

  /**
   * Describe <code>removeData</code> method here.
   *
   * @param node1_id a <code>String</code> value
   * @param key a <code>String</code> value
   */
  public void removeData(String node1_id, String key)
    throws NodeNotFoundException {
    removeData(node1_id, null, key);
  }

  /**
   * Describe <code>removeSubnode</code> method here.
   *
   * @param node1_id a <code>String</code> value
   * @param subnode a <code>String</code> value
   */
  public void removeSubnode(String node1_id, String subnode)
    throws NodeNotFoundException {
    saveDB();
  }

  private class DBElementComparator
    implements Comparator<DBElement> {

    public int compare(DBElement el1, DBElement el2) {
      String name1 = el1.getAttribute("name");
      String name2 = el2.getAttribute("name");
      return name1.compareTo(name2);
    }

  }

} // XMLDB
