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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import tigase.xml.DomBuilderHandler;
import tigase.xml.SimpleParser;

/**
 * <code>XMLDB</code> is the main data base access class.
 * It allows you to create new data base in given file, open data base from
 * given file, add, delete and retrieve data and data lists. All data or data
 * lists are stored in data base nodes. There are three possible kinds of nodes
 * for each data base:
 * <ol>
 * <li><b>root node</b> - this is top node in each <em>XML</em> hierachy tree.
 *  There can be only one root node in data base as there can be only one root
 *  element in <em>XML</em> file. The element name for root node can be defined
 *  by the user when new data base is created or defualt element name
 *  '<code>root</code>' is used.</li>
 * <li><b>node1 nodes</b> - these are the first level nodes under <em>root</em>
 *  node. There can be any number of nodes on this level. All data added for
 *  this data base are added to first level node unless subnode path is
 *  given. User can define element name of <em>node1</em> when new data base is
 *  created. If not given default element name '<code>node</code>' is used.</li>
 * <li><b>subnodes</b> - node on any deeper level under <em>node1</em>
 *  level. There can be any number of <em>subnodes</em> on any
 *  level. <em>Subnodes</em> has always '<code>node</code>' element name and this
 *  can't be changed.</li>
 * </ol>
 * <p>All <em>node1</em> nodes and <em>subnodes</em> can contains any number of
 * data associated with keys. With some keys there ca be more than one value
 * assigned. Such kind of data are called <em>data lists</em>.<br/>
 * Although element name for <em>subnode</em> can not be defined it is actually not
 * important. Because data base user doesn't use subnode element names. He doesn't
 * even use neiher <em>root</em> node element name nor <em>node1</em> element
 * name. Data base user uses <em><b>node name</b></em> what is quite different
 * from <b><em>node element name</em></b>. Let see example below:</p>
 * <pre>&#60;node name='roster'/&#62;</pre>
 * <p>In this example <em>node element name</em> is <b>node</b> and
 * <em>node name</em> is <b>roster.</b><br/>
 * Data base users (actually developers) use only <em>node names</em>.<br/>
 * If you want to access subnode on some level you need to give full path to
 * this subnode. For example, let's assume we have following data base:</p>
 * <pre>  &#60;node name='tigase'>
 * &#60;node name='server'>
 * &#60;/node>
 * &#60;node name='xmpp'>
 * &#60;/node>
 * &#60;/node></pre>
 * <p>If you need to access '<code>server</code>' subnode you need to call method
 * with '<code>/tigase/server</code>' as subnode path and for subnode
 * '<code>xmpp</code>' proper subnode path is of course
 * '<code>/tigase/xmpp</code>'. If you skip subnode path or give
 * <code>null</code> as a parameter you will be accessing data on <em>node1</em>
 * level. You can not access or save data on root node level.</p>
 *
 * <p>
 * Created: Tue Oct 26 15:27:33 2004
 * </p>
 * @author <a href="mailto:artur.hefczyc@gmail.com">Artur Hefczyc</a>
 * @version $Rev$
 */
public class XMLDB {

  private static Logger log = Logger.getLogger("tigase.xml.db.XMLDB");

  private String root_name = "root";
  private String node1_name = "node";
  private DBElementComparator comparator =
    new DBElementComparator();
  private Lock lock = new ReentrantLock();

  private String dbFile = "xml_db.xml";
  private DBElement root = null;
  private ArrayList<DBElement> node1s = null;

  /**
   * Used only for searching for given node, do NOT use for any
   * other purpose.
   */
  private DBElement tmp_node1 = null;

  private XMLDB() {}

  public XMLDB(String db_file) throws IOException {
    dbFile = db_file;
    tmp_node1 = new DBElement(node1_name);
    loadDB();
  }

//   public XMLDB(String db_file, String root_name, String node1_name)
//     throws IOException {
//     dbFile = db_file;
//     this.root_name = root_name;
//     this.node1_name = node1_name;
//     tmp_node1 = new DBElement(node1_name);
//     loadDB();
//   }

  public static XMLDB createDB(String db_file,
    String root_name, String node1_name) {
    XMLDB xmldb = new XMLDB();
    xmldb.setupNewDB(db_file, root_name, node1_name);
    return xmldb;
  }

  protected void setupNewDB(String db_file, String root_name,
    String node1_name) {

    log.info("Create empty DB.");
    this.dbFile = db_file;
    if (root_name != null) {
      this.root_name = root_name;
    } // end of if (root_name != null)
    if (node1_name != null) {
      this.node1_name = node1_name;
    } // end of if (node1_name != null)
    tmp_node1 = new DBElement(node1_name);
    root = new DBElement(root_name);
    node1s = new ArrayList<DBElement>();
    root.setChildren(node1s);
  }

  protected void loadDB() throws IOException {
    FileReader file = new FileReader(dbFile);
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
    this.root_name = root.getName();
    ArrayList<DBElement> children = root.getChildren();
    if (children != null && children.size() > 0) {
      this.node1_name = children.get(0).getName();
    } // end of if (children != null && children.size() > 0)
    Collections.sort(node1s, comparator);
    log.finest(root.formatedString(0, 2));
  }

  protected void saveDB() {
    lock.lock();
    try {
      String buffer = root.formatedString(0, 1);
      FileWriter file = new FileWriter(dbFile, false);
      file.write("<?xml version='1.0' encoding='UTF-8'?>\n");
      file.write(buffer+"\n");
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
    getNode1(node1_id).removeNode(subnode);
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
