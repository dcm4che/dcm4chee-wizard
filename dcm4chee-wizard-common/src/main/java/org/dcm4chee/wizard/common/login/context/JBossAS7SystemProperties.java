package org.dcm4chee.wizard.common.login.context;

public final class JBossAS7SystemProperties {

	public static final String JBOSS_SERVER_BASE_DIR = 
			System.getProperty("jboss.server.base.dir");
	
	public static final String JBOSS_SERVER_CONFIG_DIR = 
			System.getProperty("jboss.server.config.dir");
	
	public static final String JBOSS_BIND_ADDRESS = 
			System.getProperty("jboss.bind.address") == null ? "127.0.0.1" : 
				System.getProperty("jboss.bind.address");
	
	public static final String JBOSS_BIND_PORT = 
			System.getProperty("jboss.bind.port") == null ? "8080" : 
				System.getProperty("jboss.bind.port");
}
