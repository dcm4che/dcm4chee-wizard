package org.dcm4chee.wizard.war;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SopClassManager {

	protected static Logger log = LoggerFactory.getLogger(SopClassManager.class);
	
	private static Map<String, String> sopClassTypeMap;
	
	private static void init() {
		
        String configPath = System.getProperty("dcm4chee-wizard.cfg.path", "conf/dcm4chee-wizard/");
        File typesFile = new File(configPath + "transfer-capability-types.json");
        if (!typesFile.isAbsolute())
            typesFile = new File(System.getProperty("jboss.server.home.dir"), typesFile.getPath());

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
