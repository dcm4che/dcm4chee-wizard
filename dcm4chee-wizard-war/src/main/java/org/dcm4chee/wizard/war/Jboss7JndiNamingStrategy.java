package org.dcm4chee.wizard.war;

import org.wicketstuff.javaee.naming.IJndiNamingStrategy;

public class Jboss7JndiNamingStrategy implements IJndiNamingStrategy {

	private static final long serialVersionUID = 1L;

	private final String baseName;
	
	public Jboss7JndiNamingStrategy() {
        baseName = (new StringBuilder()).append("java:module/").toString();
	}

	public Jboss7JndiNamingStrategy(String moduleName) {
        baseName = (new StringBuilder()).append("java:app/").append(moduleName)
                            .append("/").toString();
	}

	public Jboss7JndiNamingStrategy(String applicationName, String moduleName) {
        if (applicationName == null || applicationName.isEmpty()) {
        	baseName = (new StringBuilder()).append("java:global/")
        			.append(moduleName).append("/").toString();
        } else {
        	baseName = (new StringBuilder()).append("java:global/")
        			.append(applicationName).append("/").append(moduleName)
        			.append("/").toString();
        }

}
	public String calculateName(String ejbName, Class<?> ejbType) {
		return new StringBuilder().append(baseName)
				.append(ejbName).append("!")
                .append(ejbType.getCanonicalName()).toString();
	}
}
