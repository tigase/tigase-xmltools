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
package tigase.xml.db;

/**
 * This exception is thrown when user tries to add subnode with name which already exists in data base. Data base
 * implementation requires that all nodes have unique names thus adding multiple nodes with the same name is not
 * permitted. <p> Created: Thu Nov 11 20:52:34 2004 </p>
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev$
 */
public class NodeExistsException
		extends XMLDBException {

	private static final long serialVersionUID = 1L;

	public NodeExistsException() {
		super();
	}

	public NodeExistsException(String message) {
		super(message);
	}

	public NodeExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public NodeExistsException(Throwable cause) {
		super(cause);
	}

} // NodeExistsException