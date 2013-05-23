package org.dcm4chee.wizard.war;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

import org.apache.wicket.Application;
import org.apache.wicket.protocol.http.WebApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProxyManifest {
	
	protected static Logger log = LoggerFactory.getLogger(ProxyManifest.class);
	
    private static Map<String, String> properties = null;

    public static Map<String, String> get() {
    	if (properties != null)
    		return properties;

        properties = new HashMap<String, String>();
        String wizardWarName = null;
        String proxyJarName = null;
        String manifestPath = null;
		try {
			wizardWarName = ((WebApplication) Application.get()).getInitParameter("wizardWarName");
			proxyJarName = ((WebApplication) Application.get()).getInitParameter("proxyJarName");

			if (wizardWarName == null)
				throw new RuntimeException("Can't get InitParameter 'wizardWarName' from Wicket Application!");
			if (proxyJarName == null)
				throw new RuntimeException("Can't get InitParameter 'proxyJarName' from Wicket Application!");

			String osName = System.getProperty("os.name");
			manifestPath = osName != null && osName.toLowerCase().contains("windows") ? 
					"vfs:/" + System.getProperty("jboss.home.dir").replace("\\", "/") + "/bin/content/" + wizardWarName + "/WEB-INF/lib/" + proxyJarName + "/META-INF/MANIFEST.MF"
					: "vfs:/content/" + wizardWarName + "/WEB-INF/lib/" + proxyJarName + "/META-INF/MANIFEST.MF";

			final Attributes attributes = new Manifest(new URL(manifestPath).openStream()).getMainAttributes();
            for (Object key : attributes.keySet())
            	properties.put(key.toString(), attributes.getValue((Name) key));
		} catch (Exception e) {
			log.error("Failed to retrieve " + manifestPath, e);
		}
		return properties;
    }
}
