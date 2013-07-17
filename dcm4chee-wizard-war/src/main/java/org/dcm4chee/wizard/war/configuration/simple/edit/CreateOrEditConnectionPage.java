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

package org.dcm4chee.wizard.war.configuration.simple.edit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.validator.RangeValidator;
import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Connection;
import org.dcm4che.net.hl7.HL7Application;
import org.dcm4che.net.hl7.HL7DeviceExtension;
import org.dcm4che.util.StringUtils;
import org.dcm4chee.wizard.common.component.ExtendedForm;
import org.dcm4chee.wizard.common.component.ModalWindowRuntimeException;
import org.dcm4chee.wizard.common.component.secure.SecureSessionCheckPage;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.ConnectionModel;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.DefaultableModel;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.DeviceModel;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.StringArrayModel;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.TlsCipherSuiteCollectionModel;
import org.dcm4chee.wizard.war.configuration.simple.tree.ConfigTreeNode;
import org.dcm4chee.wizard.war.configuration.simple.tree.ConfigTreeProvider;
import org.dcm4chee.wizard.war.configuration.simple.validator.ConnectionValidator;
import org.dcm4chee.wizard.war.configuration.simple.validator.HostnameValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class CreateOrEditConnectionPage extends SecureSessionCheckPage {
    
    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(CreateOrEditConnectionPage.class);
    
    // mandatory
	private Model<String> hostnameModel;

	// optional
    private Model<String> commonNameModel;
	private Model<Boolean> installedModel;
	private Model<Integer> portModel;
	private TlsCipherSuiteCollectionModel tlsCipherSuitesModel;
	private Model<String> httpProxyModel;
	private Model<Boolean> tlsNeedClientAuthModel;
	private Model<Connection.Protocol> protocolModel;
	private Model<ArrayList<String>> tlsProtocolModel;
	private DefaultableModel<Integer> tcpBacklogModel;
	private DefaultableModel<Integer> tcpConnectTimeoutModel;
	private DefaultableModel<Integer> tcpCloseDelayModel;
	private DefaultableModel<Integer> tcpSendBufferSizeModel;
	private DefaultableModel<Integer> tcpReceiveBufferSizeModel;
	private Model<Boolean> tcpNoDelayModel;
	private StringArrayModel blacklistedHostnameModel;
	private DefaultableModel<Integer> sendPDULengthModel;
	private DefaultableModel<Integer> receivePDULengthModel;
	private DefaultableModel<Integer> maxOpsPerformedModel;
	private DefaultableModel<Integer> maxOpsInvokedModel;
	private Model<Boolean> packPDVModel;
	private DefaultableModel<Integer> aarqTimeoutModel;
	private DefaultableModel<Integer> aaacTimeoutModel;
	private DefaultableModel<Integer> arrpTimeoutModel;
	private DefaultableModel<Integer> responseTimeoutModel;
	private DefaultableModel<Integer> retrieveTimeoutModel;
	private DefaultableModel<Integer> idleTimeoutModel;
    
	private List<String> installedRendererChoices;

    public CreateOrEditConnectionPage(final ModalWindow window, final ConnectionModel connectionModel, 
			final ConfigTreeNode deviceNode) {
        super();
        
        add(new WebMarkupContainer("create-connection-title").setVisible(connectionModel == null));
        add(new WebMarkupContainer("edit-connection-title").setVisible(connectionModel != null));

        setOutputMarkupId(true);
        final ExtendedForm form = new ExtendedForm("form");
        form.setResourceIdPrefix("dicom.edit.connection.");
        add(form);
        
        installedRendererChoices = new ArrayList<String>();
        installedRendererChoices.add(new ResourceModel("dicom.installed.true.text").wrapOnAssignment(this).getObject());
        installedRendererChoices.add(new ResourceModel("dicom.installed.false.text").wrapOnAssignment(this).getObject());

        ArrayList<String> tlsProtocols;
        ArrayList<String> tlsCipherSuites;
        try {
            tlsProtocols = (ArrayList<String>) loadConfiguration("tls-protocols.txt");
            tlsCipherSuites = (ArrayList<String>) loadConfiguration("tls-ciphersuites.txt");
            
	    	tcpBacklogModel = new DefaultableModel<Integer>(Connection.DEF_BACKLOG);
	    	tcpConnectTimeoutModel = new DefaultableModel<Integer>(Connection.NO_TIMEOUT);
	    	tcpCloseDelayModel = new DefaultableModel<Integer>(Connection.DEF_SOCKETDELAY);
	    	tcpSendBufferSizeModel = new DefaultableModel<Integer>(Connection.DEF_BUFFERSIZE);
	    	tcpReceiveBufferSizeModel = new DefaultableModel<Integer>(Connection.DEF_BUFFERSIZE);
	    	sendPDULengthModel = new DefaultableModel<Integer>(Connection.DEF_MAX_PDU_LENGTH);
	    	receivePDULengthModel = new DefaultableModel<Integer>(Connection.DEF_MAX_PDU_LENGTH);
	    	maxOpsPerformedModel = new DefaultableModel<Integer>(Connection.SYNCHRONOUS_MODE);
	    	maxOpsInvokedModel = new DefaultableModel<Integer>(Connection.SYNCHRONOUS_MODE);
	    	aarqTimeoutModel = new DefaultableModel<Integer>(Connection.NO_TIMEOUT);
	    	aaacTimeoutModel = new DefaultableModel<Integer>(Connection.NO_TIMEOUT);
	    	arrpTimeoutModel = new DefaultableModel<Integer>(Connection.NO_TIMEOUT);
	    	responseTimeoutModel = new DefaultableModel<Integer>(Connection.NO_TIMEOUT);
	    	retrieveTimeoutModel = new DefaultableModel<Integer>(Connection.NO_TIMEOUT);
	    	idleTimeoutModel = new DefaultableModel<Integer>(Connection.NO_TIMEOUT);
	    	tlsProtocolModel = new Model<ArrayList<String>>();
	    	tlsProtocolModel.setObject(new ArrayList<String>());
	    	if (connectionModel != null) {
	    		List<String> assignedTlsProtocols = 
	    				Arrays.asList(connectionModel.getConnection().getTlsProtocols());
				for (String tlsProtocol : tlsProtocols)
					if (assignedTlsProtocols.contains(tlsProtocol))
						tlsProtocolModel.getObject().add(tlsProtocol);
	    	}

        	if (connectionModel == null) {
		        hostnameModel = Model.of();
	        	commonNameModel = Model.of();
				installedModel = Model.of();
		        portModel = Model.of(11112);
		        tlsCipherSuitesModel = new TlsCipherSuiteCollectionModel(null, 3);
		    	httpProxyModel = Model.of();
		    	tlsNeedClientAuthModel = Model.of(true);
		    	protocolModel = Model.of(Connection.Protocol.DICOM);
		    	tcpNoDelayModel = Model.of(false);
		    	blacklistedHostnameModel = new StringArrayModel(null);
		    	packPDVModel = Model.of(true);
        	} else {
        		Connection connection = connectionModel.getConnection();
		        hostnameModel = Model.of(connection.getHostname());
	        	commonNameModel = Model.of(connection.getCommonName());
				installedModel = Model.of(connection.getInstalled());
		        portModel = Model.of(connection.getPort());
		        tlsCipherSuitesModel = new TlsCipherSuiteCollectionModel(connectionModel.getConnection(), 3);
		    	httpProxyModel = Model.of(connection.getHttpProxy());
		    	tlsNeedClientAuthModel = Model.of(connection.isTlsNeedClientAuth());
		    	protocolModel = Model.of(connection.getProtocol());
		    	tcpBacklogModel.setObject(connection.getBacklog());
		    	tcpConnectTimeoutModel.setObject(connection.getConnectTimeout());
		    	tcpCloseDelayModel.setObject(connection.getSocketCloseDelay());
		    	tcpSendBufferSizeModel.setObject(connection.getSendBufferSize());
		    	tcpReceiveBufferSizeModel.setObject(connection.getReceiveBufferSize());
		    	tcpNoDelayModel = Model.of(connection.isTcpNoDelay());
		    	blacklistedHostnameModel = new StringArrayModel(connection.getBlacklist());
		    	sendPDULengthModel.setObject(connection.getSendPDULength());
		    	receivePDULengthModel.setObject(connection.getReceivePDULength());
		    	maxOpsPerformedModel.setObject(connection.getMaxOpsPerformed());
		    	maxOpsInvokedModel.setObject(connection.getMaxOpsInvoked());
		    	packPDVModel = Model.of(connection.isPackPDV());
		    	aarqTimeoutModel.setObject(connection.getRequestTimeout());
		    	aaacTimeoutModel.setObject(connection.getAcceptTimeout());
		    	arrpTimeoutModel.setObject(connection.getReleaseTimeout());
		    	responseTimeoutModel.setObject(connection.getResponseTimeout());
		    	retrieveTimeoutModel.setObject(connection.getRetrieveTimeout());
		    	idleTimeoutModel.setObject(connection.getIdleTimeout());
	        }
        } catch (ConfigurationException ce) {
			log.error(this.getClass().toString() + ": " + "Error retrieving connection data: " + ce.getMessage());
            log.debug("Exception", ce);
            throw new ModalWindowRuntimeException(ce.getLocalizedMessage());
		}

        FormComponent<String> hostnameTextField;
		form.add(new Label("hostname.label", new ResourceModel("dicom.edit.connection.hostname.label")))
        .add(hostnameTextField = new TextField<String>("hostname", hostnameModel)
        		.add(new HostnameValidator())
        		.setRequired(true));

        FormComponent<Integer> portTextField;
		form.add(new Label("port.label", new ResourceModel("dicom.edit.connection.port.label")))
        .add(portTextField = new TextField<Integer>("port", portModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(1,65535)));
		if (portModel.getObject().equals(-1))
			portTextField.setModelObject(null);

        final WebMarkupContainer optionalContainer = new WebMarkupContainer("optional");
        form.add(optionalContainer
        		.setOutputMarkupId(true)
        		.setOutputMarkupPlaceholderTag(true)
        		.setVisible(false));
        
        TextField<String> commonNameTextField;
		optionalContainer.add(new Label("commonName.label", new ResourceModel("dicom.edit.connection.optional.commonName.label")))
        .add(commonNameTextField = new TextField<String>("commonName", commonNameModel));

        optionalContainer.add(new Label("installed.label", new ResourceModel("dicom.edit.connection.optional.installed.label")))
        .add(new DropDownChoice<Boolean>("installed", installedModel, 
        		  Arrays.asList(new Boolean[] { new Boolean(true), new Boolean(false) }), 
        		  new IChoiceRenderer<Boolean>() {
 
					private static final long serialVersionUID = 1L;

					public String getDisplayValue(Boolean object) {
						return object.booleanValue() ? 
								installedRendererChoices.get(0) : 
									installedRendererChoices.get(1);
					}

					public String getIdValue(Boolean object, int index) {
						return String.valueOf(index);
					}
        		}).setNullValid(true));

        optionalContainer.add(new Label("tlsCipherSuites.label", new ResourceModel("dicom.edit.connection.optional.tlsCipherSuites.label")));
        DropDownChoice<String> tlsCipherSuiteDropDown1 = 
        		new DropDownChoice<String>("tlsCipherSuite1", tlsCipherSuitesModel.getTlsCipherSuiteModel(0), tlsCipherSuites);
        optionalContainer.add(tlsCipherSuiteDropDown1
        		.setNullValid(true));
        DropDownChoice<String> tlsCipherSuiteDropDown2 = 
        		new DropDownChoice<String>("tlsCipherSuite2", tlsCipherSuitesModel.getTlsCipherSuiteModel(1), tlsCipherSuites);
        optionalContainer.add(tlsCipherSuiteDropDown2
        		.setNullValid(true));
        DropDownChoice<String> tlsCipherSuiteDropDown3 = 
        		new DropDownChoice<String>("tlsCipherSuite3", tlsCipherSuitesModel.getTlsCipherSuiteModel(2), tlsCipherSuites);
        optionalContainer.add(tlsCipherSuiteDropDown3
        		.setNullValid(true));

        optionalContainer.add(new Label("httpProxy.label", new ResourceModel("dicom.edit.connection.optional.httpProxy.label")))
        .add(new TextField<String>("httpProxy", httpProxyModel)
        		.add(new HostnameValidator()));
        
        optionalContainer.add(new Label("tlsNeedClientAuth.label", new ResourceModel("dicom.edit.connection.optional.tlsNeedClientAuth.label")))
        .add(new CheckBox("tlsNeedClientAuth", tlsNeedClientAuthModel));

        DropDownChoice<Connection.Protocol> protocolDropDown;
        optionalContainer.add(new Label("protocol.label", new ResourceModel("dicom.edit.connection.optional.protocol.label")))
        .add((protocolDropDown = new DropDownChoice<Connection.Protocol>("protocol", protocolModel, 
        		  Arrays.asList(new Connection.Protocol[] {
        				  Connection.Protocol.DICOM, 
        				  Connection.Protocol.HL7, 
        				  Connection.Protocol.SYSLOG_TLS, 
        				  Connection.Protocol.SYSLOG_UDP}), 
        		  new IChoiceRenderer<Connection.Protocol>() {
 
					private static final long serialVersionUID = 1L;

					public String getDisplayValue(Connection.Protocol object) {
						return object.name();
					}

					public String getIdValue(Connection.Protocol object, int index) {
						return String.valueOf(index);
					}
        		})).setNullValid(false));

        optionalContainer.add(new Label("tlsProtocol.label", new ResourceModel("dicom.edit.connection.optional.tlsProtocol.label")))
        .add(new CheckBoxMultipleChoice<String>("tlsProtocol", tlsProtocolModel, new Model<ArrayList<String>>(tlsProtocols)));

        optionalContainer.add(new Label("tcpBacklog.label", new ResourceModel("dicom.edit.connection.optional.tcpBacklog.label")))
        .add(new TextField<Integer>("tcpBacklog", tcpBacklogModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(0,Integer.MAX_VALUE)));

        optionalContainer.add(new Label("tcpConnectTimeout.label", new ResourceModel("dicom.edit.connection.optional.tcpConnectTimeout.label")))
        .add(new TextField<Integer>("tcpConnectTimeout", tcpConnectTimeoutModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(0,Integer.MAX_VALUE)));

        optionalContainer.add(new Label("tcpCloseDelay.label", new ResourceModel("dicom.edit.connection.optional.tcpCloseDelay.label")))
        .add(new TextField<Integer>("tcpCloseDelay", tcpCloseDelayModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(0,Integer.MAX_VALUE)));

        optionalContainer.add(new Label("tcpSendBufferSize.label", new ResourceModel("dicom.edit.connection.optional.tcpSendBufferSize.label")))
        .add(new TextField<Integer>("tcpSendBufferSize", tcpSendBufferSizeModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(0,Integer.MAX_VALUE)));
        
        optionalContainer.add(new Label("tcpReceiveBufferSize.label", new ResourceModel("dicom.edit.connection.optional.tcpReceiveBufferSize.label")))
        .add(new TextField<Integer>("tcpReceiveBufferSize", tcpReceiveBufferSizeModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(0,Integer.MAX_VALUE)));
        
        optionalContainer.add(new Label("tcpNoDelay.label", new ResourceModel("dicom.edit.connection.optional.tcpNoDelay.label")))
        .add(new CheckBox("tcpNoDelay", tcpNoDelayModel));

        optionalContainer.add(new Label("blacklistedHostname.label", new ResourceModel("dicom.edit.connection.optional.blacklistedHostname.label")))
        .add(new TextArea<String>("blacklistedHostname", blacklistedHostnameModel));

        optionalContainer.add(new Label("sendPDULength.label", new ResourceModel("dicom.edit.connection.optional.sendPDULength.label")))
        .add(new TextField<Integer>("sendPDULength", sendPDULengthModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(0,Integer.MAX_VALUE)));
        
        optionalContainer.add(new Label("receivePDULength.label", new ResourceModel("dicom.edit.connection.optional.receivePDULength.label")))
        .add(new TextField<Integer>("receivePDULength", receivePDULengthModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(0,Integer.MAX_VALUE)));

        optionalContainer.add(new Label("maxOpsPerformed.label", new ResourceModel("dicom.edit.connection.optional.maxOpsPerformed.label")))
        .add(new TextField<Integer>("maxOpsPerformed", maxOpsPerformedModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(0,Integer.MAX_VALUE)));

        optionalContainer.add(new Label("maxOpsInvoked.label", new ResourceModel("dicom.edit.connection.optional.maxOpsInvoked.label")))
        .add(new TextField<Integer>("maxOpsInvoked", maxOpsInvokedModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(0,Integer.MAX_VALUE)));

        optionalContainer.add(new Label("packPDV.label", new ResourceModel("dicom.edit.connection.optional.packPDV.label")))
        .add(new CheckBox("packPDV", packPDVModel));

        optionalContainer.add(new Label("aarqTimeout.label", new ResourceModel("dicom.edit.connection.optional.aarqTimeout.label")))
        .add(new TextField<Integer>("aarqTimeout", aarqTimeoutModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(0,Integer.MAX_VALUE)));

        optionalContainer.add(new Label("aaacTimeout.label", new ResourceModel("dicom.edit.connection.optional.aaacTimeout.label")))
        .add(new TextField<Integer>("aaacTimeout", aaacTimeoutModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(0,Integer.MAX_VALUE)));

        optionalContainer.add(new Label("arrpTimeout.label", new ResourceModel("dicom.edit.connection.optional.arrpTimeout.label")))
        .add(new TextField<Integer>("arrpTimeout", arrpTimeoutModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(0,Integer.MAX_VALUE)));

        optionalContainer.add(new Label("responseTimeout.label", new ResourceModel("dicom.edit.connection.optional.responseTimeout.label")))
        .add(new TextField<Integer>("responseTimeout", responseTimeoutModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(0,Integer.MAX_VALUE)));

        optionalContainer.add(new Label("retrieveTimeout.label", new ResourceModel("dicom.edit.connection.optional.retrieveTimeout.label")))
        .add(new TextField<Integer>("retrieveTimeout", retrieveTimeoutModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(0,Integer.MAX_VALUE)));
        
        optionalContainer.add(new Label("idleTimeout.label", new ResourceModel("dicom.edit.connection.optional.idleTimeout.label")))
        .add(new TextField<Integer>("idleTimeout", idleTimeoutModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(0,Integer.MAX_VALUE)));

        form.add(new Label("toggleOptional.label", new ResourceModel("dicom.edit.toggleOptional.label")))
        .add(new AjaxCheckBox("toggleOptional", new Model<Boolean>()) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(optionalContainer.setVisible(this.getModelObject()));
			}
        });
        
		form.add(new ConnectionValidator(((DeviceModel) deviceNode.getModel()).getConnections(), 
				commonNameTextField, hostnameTextField, portTextField, connectionModel));

		if (connectionModel != null)
			try {
				Connection connection = connectionModel.getConnection();
	        	for (ApplicationEntity ae : connection.getDevice().getApplicationEntities())
	        		if (ae.getConnections().contains(connection))
	        			protocolDropDown.setEnabled(false)
	        				.add(new AttributeModifier("title", new ResourceModel("dicom.delete.connection.notAllowed")));
	        	
	        	HL7DeviceExtension hl7DeviceExtension = 
	        			connection.getDevice().getDeviceExtension(HL7DeviceExtension.class);
	        	if (hl7DeviceExtension != null) {
	        		for (HL7Application hl7Application : hl7DeviceExtension.getHL7Applications())
	        			if (hl7Application.getConnections().contains(connection))
	        				protocolDropDown.setEnabled(false)
	        				.add(new AttributeModifier("title", new ResourceModel("dicom.delete.connection.notAllowed")));
	        	}
			} catch (ConfigurationException ce) {
	        	log.error(this.getClass().toString() + ": " + "Error checking connections in use: " + ce.getMessage());
	        	log.debug("Exception", ce);
	        	throw new ModalWindowRuntimeException(ce.getLocalizedMessage());
			}

        form.add(new IndicatingAjaxButton("submit", new ResourceModel("saveBtn"), form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    Connection connection = connectionModel == null ? 
                    		new Connection(commonNameModel.getObject(), hostnameModel.getObject()) : 
                    			connectionModel.getConnection();

                    connection.setHostname(hostnameModel.getObject());
                    connection.setCommonName(commonNameModel.getObject());
                    connection.setInstalled(installedModel.getObject());                    
                    connection.setPort(portModel.getObject() == null ? -1 : portModel.getObject().intValue());
                    connection.setTlsCipherSuites(tlsCipherSuitesModel.getTlsCipherSuites().toArray(new String[0]));                 
                    connection.setHttpProxy(httpProxyModel.getObject());
                    connection.setTlsNeedClientAuth(tlsNeedClientAuthModel.getObject());
                    connection.setProtocol(protocolModel.getObject());
                    connection.setTlsProtocols(tlsProtocolModel.getObject().toArray(new String[0]));
                    connection.setBacklog(tcpBacklogModel.getObject());
                    connection.setConnectTimeout(tcpConnectTimeoutModel.getObject());
                    connection.setSocketCloseDelay(tcpCloseDelayModel.getObject());
                    connection.setSendBufferSize(tcpSendBufferSizeModel.getObject());
                    connection.setReceiveBufferSize(tcpReceiveBufferSizeModel.getObject());
                    connection.setTcpNoDelay(tcpNoDelayModel.getObject());
                    connection.setBlacklist(blacklistedHostnameModel.getArray());
                    connection.setSendPDULength(sendPDULengthModel.getObject());
                    connection.setReceivePDULength(receivePDULengthModel.getObject());
                    connection.setMaxOpsPerformed(maxOpsPerformedModel.getObject());                    
                    connection.setMaxOpsInvoked(maxOpsInvokedModel.getObject());
                    connection.setPackPDV(packPDVModel.getObject());
                    connection.setRequestTimeout(aarqTimeoutModel.getObject());
                    connection.setAcceptTimeout(aaacTimeoutModel.getObject());
                    connection.setReleaseTimeout(arrpTimeoutModel.getObject());
                    connection.setResponseTimeout(responseTimeoutModel.getObject());
                    connection.setRetrieveTimeout(retrieveTimeoutModel.getObject());
                    connection.setIdleTimeout(idleTimeoutModel.getObject());

                    if (connectionModel == null) 
                        ((DeviceModel) deviceNode.getModel()).getDevice().addConnection(connection);
                    ConfigTreeProvider.get().mergeDevice(connection.getDevice());
                    window.close(target);
                } catch (Exception e) {
        			log.error(this.getClass().toString() + ": " + "Error modifying connection: " + e.getMessage());
                    log.debug("Exception", e);
                    throw new ModalWindowRuntimeException(e.getLocalizedMessage());
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                if (target != null)
                    target.add(form);
            }
        });
        form.add(new AjaxFallbackButton("cancel", new ResourceModel("cancelBtn"), form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                window.close(target);
            }

			@Override
			protected void onError(AjaxRequestTarget arg0, Form<?> arg1) {
			}
        }.setDefaultFormProcessing(false));
    }
    
    private List<String> loadConfiguration(String filename) {
    	String line;
    	BufferedReader reader = null;
    	try {
    		List<String> tlsProtocols = new ArrayList<String>();
    		String fn = System.getProperty("dcm4chee-wizard.cfg.path", "dcm4chee-wizard/");
    		if (fn == null)
    			throw new FileNotFoundException(
    					"Web config path not found! Not specified with System property 'dcm4chee-wizard.cfg.path'");
    		File configFile = new File(StringUtils.replaceSystemProperties(fn) + filename);    		
            if (!configFile.isAbsolute())
                configFile = new File(System.getProperty("jboss.server.config.dir"), configFile.getPath());
            reader = new BufferedReader(new FileReader(configFile));
            while ((line = reader.readLine()) != null)
                tlsProtocols.add(line);
            return tlsProtocols;
        } catch (IOException ioe) {
            log.error(this.getClass().toString() + ": " + "Error accessing " + filename, ioe);
            log.debug("Exception", ioe);
            throw new RuntimeException(ioe.getLocalizedMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignore) {
                }
            }
        }
    }
}
