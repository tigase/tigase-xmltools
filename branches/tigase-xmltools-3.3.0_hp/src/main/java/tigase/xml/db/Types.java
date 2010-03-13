/*
 * Tigase XMPP/Jabber XML Tools
 * Copyright (C) 2004-2010 "Artur Hefczyc" <artur.hefczyc@tigase.org>
 *
 * This program is licensed to HP, under Licensor's Intellectual Property Rights,
 * a paid-up, worldwide, non-exclusive, non-transferable, perpetual license in the
 * Source Code to use, make, reproduce (for archival and back-up purposes only),
 * and prepare derivative works.
 *
 * The right to prepare derivative works includes translations, localizations,
 * adaptations, and compilations. HP shall own all right, title, and interest in
 * any derivative works prepared by HP.
 *
 * HP shall use its standard forms in distributing Object Code.
 *
 * Licensor retains all right, title, and interest in the Licensed Software and
 * Documentation.
 *
 * $Rev$
 * Last modified by $Author$
 * $Date$
 */
package tigase.xml.db;

import java.util.Map;
import java.util.TreeMap;

/**
 * Describe class Types here.
 *
 *
 * Created: Wed Dec 28 21:54:43 2005
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev$
 */
public abstract class Types {

	public static Map<String, DataType> dataTypeMap =
	  new TreeMap<String, DataType>();

	/**
	 * Describe class DataType here.
	 *
	 *
	 * Created: Tue Dec  6 17:34:22 2005
	 *
	 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
	 * @version $Rev$
	 */
	public enum DataType {

		INTEGER(Integer.class.getSimpleName()),
			INTEGER_ARR(int[].class.getSimpleName()),
			LONG(Long.class.getSimpleName()),
			LONG_ARR(long[].class.getSimpleName()),
			STRING(String.class.getSimpleName()),
			STRING_ARR(String[].class.getSimpleName()),
			DOUBLE(Double.class.getSimpleName()),
			DOUBLE_ARR(double[].class.getSimpleName()),
			BOOLEAN(Boolean.class.getSimpleName()),
			BOOLEAN_ARR(boolean[].class.getSimpleName()),
			UNDEFINED(null)
			;

		private String javaType = null;

		/**
		 * Creates a new <code>DataType</code> instance.
		 *
		 */
		private DataType(String java_type) {
			this.javaType = java_type;
			if (java_type != null) {
				dataTypeMap.put(java_type, this);
			} // end of if (java_type != null)
		}

		public static DataType valueof(String javaType) {
			DataType result = UNDEFINED;
			if (javaType != null && !javaType.equals("")) {
				result = dataTypeMap.get(javaType);
			} // end of if (javaType != null && !javaType.equals(""))
			return result == null ? UNDEFINED : result;
		}

		public String toString() {
			if (javaType == null) {
				return String.class.getSimpleName();
			} // end of if (javaType == null)
			else {
				return javaType;
			} // end of if (javaType == null) else
		}

	} // DataType

} // Types