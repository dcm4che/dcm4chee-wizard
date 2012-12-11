/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chee.wizard.war.configuration.common.custom;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.dcm4chee.wizard.war.WizardApplication;
import org.dcm4chee.wizard.war.configuration.common.custom.CustomComponent.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class ConfigManager {

    private static Logger log = LoggerFactory.getLogger(ConfigManager.class);
    
    public synchronized static Configuration getConfigurationFor(String name) {

			InputStream stream = null;
			try {
		    	stream = WizardApplication.get().getServletContext()
		    			.getResourceAsStream("/WEB-INF/configuration/" + name + ".xml");
	    	
		    	return (Configuration) 
					JAXBContext.newInstance(Configuration.class).createUnmarshaller().unmarshal(stream);
			
//		    	Configuration cs = (Configuration) 
//				JAXBContext.newInstance(Configuration.class).createUnmarshaller().unmarshal(stream);
//				System.out.println("Components:");
//				for (Component c : cs.getComponents())
//					System.out.println("Component: name=" + c.getName() + 
//							", type=" + c.getType() + 
//							", null=" + c.getNull() + 
//							", regex=" + c.getRegex());

		} catch (JAXBException je) {
			log.error("Error processing configuration from xml file", je);
		} catch (Exception e) {
			log.error("Error processing configuration from xml file", e);
		} finally {
			try { stream.close(); } catch (IOException ignore) {} catch (NullPointerException ignore) {}
		}
		return null;
    }
    
//    @SuppressWarnings({ "rawtypes", "unchecked" })
//	public synchronized static FormComponent getComponentFor(CustomComponent customComponent) {
//    	try {
//System.out.println("Custom component: " + customComponent.getName());
//
//    		Class<?> clazz = Class.forName(customComponent.getClassName());
//    		FormComponent component = 
//    				(FormComponent) clazz.getConstructor(String.class)
//    				.newInstance(customComponent.getName());
//    		component.setMarkupId(customComponent.getName());
//    		component.setRequired(!customComponent.getRequired());
//    		if (customComponent.getValidator() != null)
//    			component.add(new PatternValidator(customComponent.getRegex()));
//
//    		return component;
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;	
//	}
    
//    public synchronized static List<CustomComponent> 
//    	filter(List<CustomComponent> customComponents, CustomComponent.Container container) {
//    	List<CustomComponent> result = new ArrayList<CustomComponent>(customComponents.size());
//        for (CustomComponent customComponent : customComponents)
//        	if (container.equals(customComponent.getContainer()))
//        		result.add(customComponent);
//		return result;
//    }

	public static List<CustomComponent> filter(
			List<CustomComponent> customComponents,
			CustomComponent.Container container,
			boolean basic) {
System.out.println("filter for: " + container + " " + basic);
    	List<CustomComponent> result = new ArrayList<CustomComponent>(customComponents.size());
        for (CustomComponent customComponent : customComponents)
        	if (container.equals(customComponent.getContainer())
        			&& !(basic ^ customComponent.getConfigurationType()
        				.equals(CustomComponent.ConfigurationType.Basic))) {
        		result.add(customComponent);
        		System.out.println("filter: added " + customComponent.getName());
        	}
		return result;
	}
}
