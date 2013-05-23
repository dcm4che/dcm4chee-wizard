package org.dcm4chee.wizard.war;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.dcm4che.util.StringUtils;
import org.dcm4chee.wizard.common.login.context.JBossAS7SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class SopClassManager {

	protected static Logger log = LoggerFactory.getLogger(SopClassManager.class);
	
	private static Map<String, String> sopClassTypeMap;
	
	private static void init() {
		
        String fn = System.getProperty("dcm4chee-wizard.cfg.path"); 
        if (fn == null) { 
            log.warn("Wizard config path not found! Not specified with System property 'dcm4chee-wizard.cfg.path'");
            fn = JBossAS7SystemProperties.JBOSS_SERVER_CONFIG_DIR + "/dcm4chee-wizard/";
            log.warn("Using default config path of: " + fn);
        }
        File typesFile = new File(StringUtils.replaceSystemProperties(fn) + "transfer-capability-types.json");
        if (!typesFile.isAbsolute())
        	typesFile = new File(
            		JBossAS7SystemProperties.JBOSS_SERVER_BASE_DIR, 
            		typesFile.getPath());

        sopClassTypeMap = new HashMap<String, String>();
        String line;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(typesFile));
            while ((line = reader.readLine()) != null) {
                JSONObject jsonObject = JSONObject.fromObject(line);
                sopClassTypeMap.put(jsonObject.getString("sopClass"), jsonObject.getString("type"));
            }
        } catch (IOException e) {
			log.error("Error processing transfer capability type mapping file", e);
		} finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignore) {}
            }
        }
	}
	
	public Map<String,String> getTransferCapabilityTypes() {
		if (sopClassTypeMap == null)
			init();
		return sopClassTypeMap;
	}
}
