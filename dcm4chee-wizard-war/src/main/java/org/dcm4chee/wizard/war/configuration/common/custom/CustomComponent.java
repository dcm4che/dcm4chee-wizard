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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.dcm4chee.wizard.war.util.ClassUtils;

/**
 * @author Robert David <robert.david@agfa.com>
 */
@XmlRootElement(name = "component")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class CustomComponent implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String componentPackage = "org.apache.wicket.markup.html.form.";
	
	public enum Container {Mandatory,Optional};

	public enum ConfigurationType {
		Basic,
		Proxy, 
		Archive
	};

	public enum ComponentType {
		TextField,
		CheckBox, 
		DropDown
	};
	
	public enum ObjectType {
		Device,
		Connection,
		AE, 
		TransferCapability
	};

	private String name;
	private Container container;
	private ConfigurationType configurationType;
	private ComponentType componentType;
	private String dataType;
	private String getFrom;
	private String storeTo;
	private boolean required;
	private String validator;
	private String options;

	@XmlElement
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNamePrefix() {
		return name.substring(0, name.lastIndexOf("."));
	}

	@XmlElement
	public Container getContainer() {
		return container;
	}

	public void setContainer(Container container) {
		this.container = container;
	}

	public ConfigurationType getConfigurationType() {
		return configurationType;
	}

	public void setConfigurationType(ConfigurationType configurationType) {
		this.configurationType = configurationType;
	}

	@XmlElement
	public ComponentType getComponentType() {
		return componentType;
	}

	public void setComponentType(ComponentType componentType) {
		this.componentType = componentType;
	}

	public String getClassName() {
		return componentPackage + componentType;
	}
	
	@XmlElement
	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public Class<?> getDataClass() throws ClassNotFoundException {
		return ClassUtils.forName(dataType);
	}
	
	@XmlElement
	public String getGetFrom() {
		return getFrom;
	}

	public void setGetFrom(String getFrom) {
		this.getFrom = getFrom;
	}

	@XmlElement
	public String getStoreTo() {
		return storeTo;
	}

	public void setStoreTo(String storeTo) {
		this.storeTo = storeTo;
	}

	public Method getStoreMethod(Class<?> storeClass, Class<?> parameter) 
			throws NoSuchMethodException, SecurityException, ClassNotFoundException {
System.out.println("METHOD Name: " + storeTo.substring(storeTo.lastIndexOf(".") + 1));
		return storeClass.getDeclaredMethod(storeTo, parameter);
	}

	@XmlElement
	public boolean getRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	@XmlElement
	public String getValidator() {
		return validator;
	}

	public void setValidator(String validator) {
		this.validator = validator;
	}

	@XmlElement
	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}

	public List<String> getOptionList() {
		return options == null ? null : Arrays.asList(options.split("\\|"));
	}
}
