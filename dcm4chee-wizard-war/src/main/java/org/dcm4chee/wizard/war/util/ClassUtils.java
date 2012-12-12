package org.dcm4chee.wizard.war.util;

import java.util.HashMap;
import java.util.Map;

public class ClassUtils {

	private static final Map<String, Class<?>> primitiveClasses = 
			new HashMap<String, Class<?>>();

	static {
	    primitiveClasses.put("byte", byte.class);
	    primitiveClasses.put("short", short.class);
	    primitiveClasses.put("char", char.class);
	    primitiveClasses.put("int", int.class);
	    primitiveClasses.put("long", long.class);
	    primitiveClasses.put("float", float.class);
	    primitiveClasses.put("double", double.class);
	    primitiveClasses.put("boolean", boolean.class);
	}

	public static Class<?> forName(String name) throws ClassNotFoundException {
		return primitiveClasses.containsKey(name) ? 
				primitiveClasses.get(name) : Class.forName(name);
	}
}
