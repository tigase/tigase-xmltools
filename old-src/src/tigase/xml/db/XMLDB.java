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

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import tigase.xmpp.rep.UserRepository;
import tigase.xml.DomBuilderHandler;
import tigase.xml.SimpleParser;
import tigase.xmpp.rep.UserNotFoundException;
import tigase.xmpp.rep.UserExistsException;

/**
 * Describe class XMLRepository here.
 *
 *
 * Created: Tue Oct 26 15:27:33 2004
 *
 * @author <a href="mailto:artur.hefczyc@gmail.com">Artur Hefczyc</a>
 * @version $Rev$
 */
public class XMLRepository implements UserRepository {

  private RepositoryElementComparator comparator =
    new RepositoryElementComparator();
  private Lock lock = new ReentrantLock();

  private Logger log =
    Logger.getLogger("tigase.xmpp.rep.xml.XMLRepository");
  private String repositoryFile = "users_rep.xml";
  private RepositoryElement root = null;
  private ArrayList<RepositoryElement> users = null;

  /**
   * Used only for searching for given user, do NOT use for any
   * other purpose.
   */
  private RepositoryElement tmp_user = new RepositoryElement("user");

  public XMLRepository() {
    loadRepository();
  }

  protected void loadRepository() {
    try {
      FileReader file = new FileReader(repositoryFile);
      char[] buff = new char[16*1024];
      SimpleParser parser = new SimpleParser();
      DomBuilderHandler<RepositoryElement> domHandler =
        new DomBuilderHandler<RepositoryElement>(RepositoryElementFactory.getFactory());
      int result = -1;
      while((result = file.read(buff)) != -1) {
        parser.parse(domHandler, buff);
      }
      file.close();
      root = domHandler.getParsedElements().poll();
      users = root.getChildren();
      Collections.sort(users, comparator);
      log.finest(root.formatedString(0, 2));
    } catch (Exception e) {
      log.severe("Can't load repository file: "+e);
    } // end of try-catch
  }

  protected void saveRepository() {
    lock.lock();
    try {
      String buffer = root.formatedString(0, 2);
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

  protected final RepositoryElement getUser(String user)
    throws UserNotFoundException {
    lock.lock();
    try {
      tmp_user.setAttribute("name", user);
      int idx = Collections.binarySearch(users, tmp_user, comparator);
      if (idx >= 0) {
        return users.get(idx);
      } // end of if (idx >= 0)
      else {
        throw new UserNotFoundException("User: "+user+
          " has not been found in repository.");
      } // end of if (idx >= 0) else
    } finally {
      lock.unlock();
    } // end of try-finally
  }

  protected final RepositoryElement getNode(String user, String subnode)
    throws UserNotFoundException {
    RepositoryElement node = getUser(user);
    if (subnode != null) {
      node = node.buildNodesTree(subnode);
    } // end of if (subnode != null)
    return node;
  }

  // Implementation of tigase.xmpp.rep.UserRepository

  public void addUser(String user) throws UserExistsException {
    lock.lock();
    try {
      tmp_user.setAttribute("name", user);
      int idx = Collections.binarySearch(users, tmp_user, comparator);
      if (idx >= 0) {
        throw new UserExistsException("User: "+user+" already exists.");
      } // end of if (idx >= 0)
      RepositoryElement newUser = new RepositoryElement("user", "name", user);
      newUser.addChild(new RepositoryElement("map"));
      users.add(newUser);
      Collections.sort(users, comparator);
    } finally {
      lock.unlock();
    } // end of try-finally
  }

  public void removeUser(String user) throws UserNotFoundException {
    lock.lock();
    try {
      tmp_user.setAttribute("name", user);
      int idx = Collections.binarySearch(users, tmp_user, comparator);
      if (idx >= 0) {
        users.remove(idx);
      } // end of if (idx >= 0)
      else {
        throw new UserNotFoundException("User: "+user+
          " has not been found in repository.");
      } // end of if (idx >= 0) else
    } finally {
      lock.unlock();
    } // end of try-finally
  }

  /**
   * Describe <code>setData</code> method here.
   *
   * @param user a <code>String</code> value
   * @param subnode a <code>String</code> value
   * @param key a <code>String</code> value
   * @param value a <code>String</code> value
   */
  public void setData(String user, String subnode, String key, String value)
    throws UserNotFoundException {
    getNode(user, subnode).setEntry(key, value);
    saveRepository();
  }

  /**
   * Describe <code>setData</code> method here.
   *
   * @param user a <code>String</code> value
   * @param key a <code>String</code> value
   * @param value a <code>String</code> value
   */
  public void setData(String user, String key, String value)
    throws UserNotFoundException {
    setData(user, null, key, value);
  }

  /**
   * Describe <code>setDataList</code> method here.
   *
   * @param user a <code>String</code> value
   * @param subnode a <code>String</code> value
   * @param key a <code>String</code> value
   * @param list a <code>String[]</code> value
   * @exception UserNotFoundException if an error occurs
   */
  public void setDataList(String user, String subnode, String key, String[] list)
    throws UserNotFoundException {
    getNode(user, subnode).setEntry(key, list);
    saveRepository();
  }

  /**
   * Describe <code>getDataList</code> method here.
   *
   * @param user a <code>String</code> value
   * @param subnode a <code>String</code> value
   * @param key a <code>String</code> value
   * @return a <code>String[]</code> value
   * @exception UserNotFoundException if an error occurs
   */
  public String[] getDataList(String user, String subnode, String key)
    throws UserNotFoundException {
    return getNode(user, subnode).getEntryValues(key);
  }

  /**
   * Describe <code>getData</code> method here.
   *
   * @param user a <code>String</code> value
   * @param subnode a <code>String</code> value
   * @param key a <code>String</code> value
   * @param def a <code>String</code> value
   * @return a <code>String</code> value
   */
  public String getData(String user, String subnode, String key, String def)
    throws UserNotFoundException {
    return getNode(user, subnode).getEntryValue(key, def);
  }

  /**
   * Describe <code>getData</code> method here.
   *
   * @param user a <code>String</code> value
   * @param subnode a <code>String</code> value
   * @param key a <code>String</code> value
   * @return a <code>String</code> value
   */
  public String getData(String user, String subnode, String key)
    throws UserNotFoundException {
    return getData(user, subnode, key, null);
  }

  /**
   * Describe <code>getData</code> method here.
   *
   * @param user a <code>String</code> value
   * @param key a <code>String</code> value
   * @return a <code>String</code> value
   */
  public String getData(String user, String key)
    throws UserNotFoundException {
    return getData(user, null, key, null);
  }

  /**
   * Describe <code>getSubnodes</code> method here.
   *
   * @param user a <code>String</code> value
   * @param subnode a <code>String</code> value
   * @return a <code>String[]</code> value
   */
  public String[] getSubnodes(String user, String subnode)
    throws UserNotFoundException {
    return getNode(user, subnode).getSubnodes();
  }

  /**
   * Describe <code>getSubnodes</code> method here.
   *
   * @param user a <code>String</code> value
   * @return a <code>String[]</code> value
   */
  public String[] getSubnodes(String user)
    throws UserNotFoundException {
    return getSubnodes(user, null);
  }

  /**
   * Describe <code>getKeys</code> method here.
   *
   * @param user a <code>String</code> value
   * @param subnode a <code>String</code> value
   * @return a <code>String[]</code> value
   */
  public String[] getKeys(String user, String subnode)
    throws UserNotFoundException {
    return getNode(user, subnode).getEntryKeys();
  }

  /**
   * Describe <code>getKeys</code> method here.
   *
   * @param user a <code>String</code> value
   * @return a <code>String[]</code> value
   */
  public String[] getKeys(String user)
    throws UserNotFoundException {
    return getKeys(user, null);
  }

  /**
   * Describe <code>removeData</code> method here.
   *
   * @param user a <code>String</code> value
   * @param subnode a <code>String</code> value
   * @param key a <code>String</code> value
   */
  public void removeData(String user, String subnode, String key)
    throws UserNotFoundException {
    getNode(user, subnode).removeEntry(key);
    saveRepository();
  }

  /**
   * Describe <code>removeData</code> method here.
   *
   * @param user a <code>String</code> value
   * @param key a <code>String</code> value
   */
  public void removeData(String user, String key)
    throws UserNotFoundException {
    removeData(user, null, key);
  }

  /**
   * Describe <code>removeSubnode</code> method here.
   *
   * @param user a <code>String</code> value
   * @param subnode a <code>String</code> value
   */
  public void removeSubnode(String user, String subnode)
    throws UserNotFoundException {
    saveRepository();
  }

  private class RepositoryElementComparator
    implements Comparator<RepositoryElement> {

    public int compare(RepositoryElement el1, RepositoryElement el2) {
      String name1 = el1.getAttribute("name");
      String name2 = el2.getAttribute("name");
      return name1.compareTo(name2);
    }

  }

} // XMLRepository
