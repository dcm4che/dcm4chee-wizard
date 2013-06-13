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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Connection;
import org.dcm4chee.proxy.conf.ProxyAEExtension;
import org.dcm4chee.proxy.conf.ProxyDeviceExtension;
import org.dcm4chee.wizard.common.behavior.FocusOnLoadBehavior;
import org.dcm4chee.wizard.common.component.ExtendedForm;
import org.dcm4chee.wizard.common.component.MainWebPage;
import org.dcm4chee.wizard.common.component.ModalWindowRuntimeException;
import org.dcm4chee.wizard.common.component.secure.SecureSessionCheckPage;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.ApplicationEntityModel;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.ConnectionModel;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.DeviceModel;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.StringArrayModel;
import org.dcm4chee.wizard.war.configuration.simple.tree.ConfigTreeNode;
import org.dcm4chee.wizard.war.configuration.simple.tree.ConfigTreeProvider;
import org.dcm4chee.wizard.war.configuration.simple.validator.AETitleValidator;
import org.dcm4chee.wizard.war.configuration.simple.validator.ConnectionProtocolValidator;
import org.dcm4chee.wizard.war.configuration.simple.validator.ConnectionReferenceValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class CreateOrEditApplicationEntityPage extends SecureSessionCheckPage {
    
    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(CreateOrEditApplicationEntityPage.class);
    
    private static final ResourceReference baseCSS = new CssResourceReference(MainWebPage.class, "base-style.css");
    
    private boolean isProxy = false;
    private String oldAETitle;
    
    // mandatory
    private Model<String> aeTitleModel;
    private Model<Boolean> associationAcceptorModel;
    private Model<Boolean> associationInitiatorModel;
	private Model<ArrayList<ConnectionModel>> connectionReferencesModel;
	// ProxyApplicationEntity only
	private Model<Boolean> acceptDataOnFailedNegotiationModel;
	private Model<Boolean> enableAuditLogModel;
	private Model<String> spoolDirectoryModel;
	private Model<Boolean> deleteFailedDataWithoutRetryConfigurationModel;

	// optional
	private StringArrayModel applicationClustersModel;
	private Model<String> descriptionModel;
	private Model<Boolean> installedModel;
	private StringArrayModel calledAETitlesModel;
	private StringArrayModel callingAETitlesModel;
	private StringArrayModel supportedCharacterSetsModel;
	private Model<String> vendorDataModel;
	// ProxyApplicationEntity only
	private Model<String> proxyPIXConsumerApplicationModel;
	private Model<String> remotePIXManagerApplicationModel;
	private Model<String> fallbackDestinationAETModel;
	
	private List<String> installedRendererChoices;

	private List<String> hl7Applications;
	
    public CreateOrEditApplicationEntityPage(final ModalWindow window, final ApplicationEntityModel aeModel, 
    		final ConfigTreeNode deviceNode) {
    	super();

        add(new WebMarkupContainer("create-applicationEntity-title").setVisible(aeModel == null));
        add(new WebMarkupContainer("edit-applicationEntity-title").setVisible(aeModel != null));

        setOutputMarkupId(true);
        final ExtendedForm form = new ExtendedForm("form");
        form.setResourceIdPrefix("dicom.edit.applicationEntity.");
        add(form);

        installedRendererChoices = new ArrayList<String>();
        installedRendererChoices.add(new ResourceModel("dicom.installed.true.text").wrapOnAssignment(this).getObject());
        installedRendererChoices.add(new ResourceModel("dicom.installed.false.text").wrapOnAssignment(this).getObject());        

        ArrayList<ConnectionModel> connectionReferences = new ArrayList<ConnectionModel>();

        try {
	        oldAETitle = aeModel == null ? 
	        		null : aeModel.getApplicationEntity().getAETitle();

	        isProxy = (((DeviceModel) deviceNode.getModel()).getDevice()
	        		.getDeviceExtension(ProxyDeviceExtension.class) != null);

        	hl7Applications = Arrays.asList(ConfigTreeProvider.get().getUniqueHL7ApplicationNames());
	        
	        connectionReferencesModel = new Model<ArrayList<ConnectionModel>>();
	        connectionReferencesModel.setObject(new ArrayList<ConnectionModel>());
			for (Connection connection : ((DeviceModel) deviceNode.getModel()).getDevice().listConnections()) {
				ConnectionModel connectionReference = new ConnectionModel(connection, 0);
				connectionReferences.add(connectionReference);
				if 	(aeModel != null && aeModel.getApplicationEntity().getConnections().contains(connection))
					connectionReferencesModel.getObject().add(connectionReference);
			}
			
			proxyPIXConsumerApplicationModel = Model.of();
			remotePIXManagerApplicationModel = Model.of();
			fallbackDestinationAETModel = Model.of();
			if (aeModel == null) {
		        aeTitleModel = Model.of();
		        associationAcceptorModel = Model.of();
		        associationInitiatorModel = Model.of();       

				acceptDataOnFailedNegotiationModel = Model.of(false);
				enableAuditLogModel = Model.of(false);
				spoolDirectoryModel = Model.of();
				deleteFailedDataWithoutRetryConfigurationModel = Model.of(false);
				
		        applicationClustersModel = new StringArrayModel(null);
				descriptionModel = Model.of();
				installedModel = Model.of();
				calledAETitlesModel = new StringArrayModel(null);
				callingAETitlesModel = new StringArrayModel(null);
				supportedCharacterSetsModel = new StringArrayModel(null);
				vendorDataModel = Model.of("size 0");				
			} else {
				ProxyAEExtension proxyAEExtension = aeModel.getApplicationEntity().getAEExtension(ProxyAEExtension.class);
				
		        aeTitleModel = Model.of(aeModel.getApplicationEntity().getAETitle());
		        associationAcceptorModel = Model.of(aeModel.getApplicationEntity().isAssociationAcceptor());
		        associationInitiatorModel = Model.of(aeModel.getApplicationEntity().isAssociationInitiator());       

		        acceptDataOnFailedNegotiationModel = Model.of(isProxy ? 
		        		proxyAEExtension.isAcceptDataOnFailedAssociation() : false);
				enableAuditLogModel = Model.of(isProxy ? 
						proxyAEExtension.isEnableAuditLog() : false);
				spoolDirectoryModel = Model.of(isProxy ? 
						proxyAEExtension.getSpoolDirectory() : null);
				deleteFailedDataWithoutRetryConfigurationModel = Model.of(isProxy ? 
						proxyAEExtension.isDeleteFailedDataWithoutRetryConfiguration() : false);
				
		        applicationClustersModel = new StringArrayModel(aeModel.getApplicationEntity().getApplicationClusters());
				descriptionModel = Model.of(aeModel.getApplicationEntity().getDescription());
				installedModel = Model.of(aeModel.getApplicationEntity().getInstalled());
				calledAETitlesModel = new StringArrayModel(aeModel.getApplicationEntity().getPreferredCalledAETitles());
				callingAETitlesModel = new StringArrayModel(aeModel.getApplicationEntity().getPreferredCallingAETitles());
				supportedCharacterSetsModel = new StringArrayModel(aeModel.getApplicationEntity().getSupportedCharacterSets());
				vendorDataModel = Model.of("size " + aeModel.getApplicationEntity().getVendorData().length);				

				if (isProxy) {
					proxyPIXConsumerApplicationModel = Model.of(proxyAEExtension.getProxyPIXConsumerApplication());
					remotePIXManagerApplicationModel = Model.of(proxyAEExtension.getRemotePIXManagerApplication());
					fallbackDestinationAETModel = Model.of(proxyAEExtension.getFallbackDestinationAET());
				}
			}
		} catch (ConfigurationException ce) {
			log.error(this.getClass().toString() + ": " + "Error retrieving application entity data: " + ce.getMessage());
            log.debug("Exception", ce);
            throw new ModalWindowRuntimeException(ce.getLocalizedMessage());
		}
		
        form.add(new Label("aeTitle.label", new ResourceModel("dicom.edit.applicationEntity.aeTitle.label")))
        .add(new TextField<String>("aeTitle", aeTitleModel)
        		.add(new AETitleValidator(aeTitleModel.getObject()))
                .setRequired(true).add(FocusOnLoadBehavior.newFocusAndSelectBehaviour()));

        form.add(new Label("associationAcceptor.label", new ResourceModel("dicom.edit.applicationEntity.associationAcceptor.label")))
        .add(new CheckBox("associationAcceptor", associationAcceptorModel));
        form.add(new Label("associationInitiator.label", new ResourceModel("dicom.edit.applicationEntity.associationInitiator.label")))
        .add(new CheckBox("associationInitiator", associationInitiatorModel));
        
        form.add(new CheckBoxMultipleChoice<ConnectionModel>("connections", 
        		connectionReferencesModel,
        		new Model<ArrayList<ConnectionModel>>(connectionReferences), 
        		new IChoiceRenderer<ConnectionModel>() {

					private static final long serialVersionUID = 1L;

					public Object getDisplayValue(ConnectionModel connectionReference) {
						Connection connection = null;
						try {
							connection = connectionReference.getConnection();
		                } catch (Exception e) {
		        			log.error(this.getClass().toString() + ": " + "Error obtaining connection: " + e.getMessage());
		                    log.debug("Exception", e);
		                    throw new ModalWindowRuntimeException(e.getLocalizedMessage());
		                }
						String location = connection.getHostname() 
								+ (connection.getPort() == -1 ? "" : ":" + connection.getPort());
						return connection.getCommonName() != null ? 
								connection.getCommonName() + " (" + location + ")" : 
									location;
					}

					public String getIdValue(ConnectionModel model, int index) {
						return String.valueOf(index);
					}
			}).add(new ConnectionReferenceValidator())
			.add(new ConnectionProtocolValidator(Connection.Protocol.DICOM)));
        
        WebMarkupContainer proxyContainer = 
	        new WebMarkupContainer("proxy") {
	        	
				private static final long serialVersionUID = 1L;
	
				@Override
				public boolean isVisible() {
					return isProxy;
				}
	        };
        form.add(proxyContainer);
        
        proxyContainer.add(new Label("acceptDataOnFailedNegotiation.label", new ResourceModel("dicom.edit.applicationEntity.proxy.acceptDataOnFailedNegotiation.label"))
        	.setVisible(isProxy))
        .add(new CheckBox("acceptDataOnFailedNegotiation", acceptDataOnFailedNegotiationModel)
        	.setVisible(isProxy));

        proxyContainer.add(new Label("enableAuditLog.label", new ResourceModel("dicom.edit.applicationEntity.proxy.enableAuditLog.label"))
    		.setVisible(isProxy))
    	.add(new CheckBox("enableAuditLog", enableAuditLogModel)
    		.setVisible(isProxy));

        proxyContainer.add(new Label("spoolDirectory.label", new ResourceModel("dicom.edit.applicationEntity.proxy.spoolDirectory.label"))
    		.setVisible(isProxy))
    	.add(new TextField<String>("spoolDirectory", spoolDirectoryModel)
    		.setRequired(true)
    		.setVisible(isProxy));      
        
        proxyContainer.add(new Label("deleteFailedDataWithoutRetryConfiguration.label", new ResourceModel("dicom.edit.applicationEntity.proxy.deleteFailedDataWithoutRetryConfiguration.label")))
        .add(new CheckBox("deleteFailedDataWithoutRetryConfiguration", deleteFailedDataWithoutRetryConfigurationModel));

        final WebMarkupContainer optionalContainer = new WebMarkupContainer("optional");
        form.add(optionalContainer
        		.setOutputMarkupId(true)
        		.setOutputMarkupPlaceholderTag(true)
        		.setVisible(false));
        
        optionalContainer.add(new Label("applicationClusters.label", new ResourceModel("dicom.edit.applicationEntity.optional.applicationClusters.label")))
        .add(new TextArea<String>("applicationClusters", applicationClustersModel));
        
        optionalContainer.add(new Label("description.label", new ResourceModel("dicom.edit.applicationEntity.optional.description.label")))
        .add(new TextField<String>("description", descriptionModel));

        optionalContainer.add(new Label("installed.label", new ResourceModel("dicom.edit.applicationEntity.optional.installed.label")))
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

        optionalContainer.add(new Label("calledAETitles.label", new ResourceModel("dicom.edit.applicationEntity.optional.calledAETitles.label")))
        .add(new TextArea<String>("calledAETitles", calledAETitlesModel));

        optionalContainer.add(new Label("callingAETitles.label", new ResourceModel("dicom.edit.applicationEntity.optional.callingAETitles.label")))
        .add(new TextArea<String>("callingAETitles", callingAETitlesModel));

        optionalContainer.add(new Label("supportedCharacterSets.label", new ResourceModel("dicom.edit.applicationEntity.optional.supportedCharacterSets.label")))
        .add(new TextArea<String>("supportedCharacterSets", supportedCharacterSetsModel));
    	
    	optionalContainer.add(new Label("vendorData.label", 
    			new ResourceModel("dicom.edit.applicationEntity.optional.vendorData.label")))
    	.add(new Label("vendorData", vendorDataModel));

        WebMarkupContainer optionalProxyContainer = 
        		new WebMarkupContainer("proxy");
        optionalContainer.add(optionalProxyContainer.setVisible(isProxy));

        final DropDownChoice<String> proxyPIXConsumerApplicationDropDownChoice = 
        		new DropDownChoice<String>("proxyPIXConsumerApplication", proxyPIXConsumerApplicationModel, hl7Applications);
        optionalProxyContainer.add(new Label("proxyPIXConsumerApplication.label", 
        		new ResourceModel("dicom.edit.applicationEntity.optional.proxy.proxyPIXConsumerApplication.label")))
        .add(proxyPIXConsumerApplicationDropDownChoice
        		.setNullValid(true).setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true));

		final TextField<String> proxyPIXConsumerApplicationTextField = 
				new TextField<String>("proxyPIXConsumerApplication.freetext", proxyPIXConsumerApplicationModel);
		optionalProxyContainer.add(proxyPIXConsumerApplicationTextField
				.setVisible(false).setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true));

		final Model<Boolean> toggleProxyPIXConsumerApplicationModel = Model.of(false);
		if (aeModel != null && proxyPIXConsumerApplicationModel.getObject() != null 
				&& !hl7Applications.contains(proxyPIXConsumerApplicationModel.getObject())) {
			toggleProxyPIXConsumerApplicationModel.setObject(true);
			proxyPIXConsumerApplicationTextField.setVisible(true);
			proxyPIXConsumerApplicationDropDownChoice.setVisible(false);
		}

        optionalProxyContainer.add(new AjaxCheckBox("toggleProxyPIXConsumerApplication", toggleProxyPIXConsumerApplicationModel) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				proxyPIXConsumerApplicationDropDownChoice.setVisible(!toggleProxyPIXConsumerApplicationModel.getObject());
				proxyPIXConsumerApplicationTextField.setVisible(toggleProxyPIXConsumerApplicationModel.getObject());
				target.add(proxyPIXConsumerApplicationDropDownChoice);
				target.add(proxyPIXConsumerApplicationTextField);
			}
        });

        final DropDownChoice<String> remotePIXManagerApplicationDropDownChoice = 
        		new DropDownChoice<String>("remotePIXManagerApplication", remotePIXManagerApplicationModel, hl7Applications);
        optionalProxyContainer.add(new Label("remotePIXManagerApplication.label", 
        		new ResourceModel("dicom.edit.applicationEntity.optional.proxy.remotePIXManagerApplication.label")))
        .add(remotePIXManagerApplicationDropDownChoice
        		.setNullValid(true).setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true));

		final TextField<String> remotePIXManagerApplicationTextField = 
				new TextField<String>("remotePIXManagerApplication.freetext", remotePIXManagerApplicationModel);
		optionalProxyContainer.add(remotePIXManagerApplicationTextField
				.setVisible(false).setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true));

		final Model<Boolean> toggleRemotePIXManagerApplicationModel = Model.of(false);
		if (aeModel != null && remotePIXManagerApplicationModel.getObject() != null 
				&& !hl7Applications.contains(remotePIXManagerApplicationModel.getObject())) {
			toggleRemotePIXManagerApplicationModel.setObject(true);
			remotePIXManagerApplicationTextField.setVisible(true);
			remotePIXManagerApplicationDropDownChoice.setVisible(false);
		}

        optionalProxyContainer.add(new AjaxCheckBox("toggleRemotePIXManagerApplication", toggleRemotePIXManagerApplicationModel) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				remotePIXManagerApplicationDropDownChoice.setVisible(!toggleRemotePIXManagerApplicationModel.getObject());
				remotePIXManagerApplicationTextField.setVisible(toggleRemotePIXManagerApplicationModel.getObject());
				target.add(remotePIXManagerApplicationDropDownChoice);
				target.add(remotePIXManagerApplicationTextField);
			}
        });
        
        List<String> uniqueAETitles = null;
        if (isProxy)
			try {
				uniqueAETitles = Arrays.asList(ConfigTreeProvider.get().getUniqueAETitles());
		        Collections.sort(uniqueAETitles);
			} catch (ConfigurationException ce) {
				log.error(this.getClass().toString() + ": " + "Error retrieving unique ae titles: " + ce.getMessage());
	            log.debug("Exception", ce);
	            throw new ModalWindowRuntimeException(ce.getLocalizedMessage());
			}
        
        final DropDownChoice<String> fallbackDestinationAETDropDownChoice = 
        		new DropDownChoice<String>("fallbackDestinationAET", fallbackDestinationAETModel, uniqueAETitles);        
        optionalProxyContainer.add(new Label("fallbackDestinationAET.label", 
        		new ResourceModel("dicom.edit.applicationEntity.optional.proxy.fallbackDestinationAET.label")))
        .add(fallbackDestinationAETDropDownChoice
        		.setNullValid(true).setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true));

		final TextField<String> fallbackDestinationAETTextField = 
				new TextField<String>("fallbackDestinationAET.freetext", fallbackDestinationAETModel);
		optionalProxyContainer.add(fallbackDestinationAETTextField
				.setVisible(false).setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true));

		final Model<Boolean> toggleFallbackDestinationAETModel = Model.of(false);
		if (aeModel != null && fallbackDestinationAETModel.getObject() != null 
				&& !uniqueAETitles.contains(fallbackDestinationAETModel.getObject())) {
			toggleFallbackDestinationAETModel.setObject(true);
			fallbackDestinationAETTextField.setVisible(true);
			fallbackDestinationAETDropDownChoice.setVisible(false);
		}

        optionalProxyContainer.add(new AjaxCheckBox("toggleFallbackDestinationAET", toggleFallbackDestinationAETModel) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				fallbackDestinationAETDropDownChoice.setVisible(!toggleFallbackDestinationAETModel.getObject());
				fallbackDestinationAETTextField.setVisible(toggleFallbackDestinationAETModel.getObject());
				target.add(fallbackDestinationAETDropDownChoice);
				target.add(fallbackDestinationAETTextField);
			}
        });

        form.add(new Label("toggleOptional.label", new ResourceModel("dicom.edit.toggleOptional.label")))
        .add(new AjaxCheckBox("toggleOptional", new Model<Boolean>()) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(optionalContainer.setVisible(this.getModelObject()));
			}
        });        
        
        form.add(new IndicatingAjaxButton("submit", new ResourceModel("saveBtn"), form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                	ApplicationEntity applicationEntity = aeModel == null ? 
                			new ApplicationEntity(aeTitleModel.getObject()) : aeModel.getApplicationEntity();

                    applicationEntity.setAETitle(aeTitleModel.getObject());
                    applicationEntity.setAssociationAcceptor(associationAcceptorModel.getObject());
                    applicationEntity.setAssociationInitiator(associationInitiatorModel.getObject());
                	applicationEntity.getConnections().clear();
                	for (ConnectionModel connectionReference : connectionReferencesModel.getObject()) 
                		for (Connection connection : ((DeviceModel) deviceNode.getModel()).getDevice().listConnections()) 
                			if (connectionReference.getConnection().getHostname().equals(connection.getHostname())
                    				&& connectionReference.getConnection().getPort() == connection.getPort())
                    			applicationEntity.addConnection(connection);
                	
                	applicationEntity.setApplicationClusters(applicationClustersModel.getArray());
                    applicationEntity.setDescription(descriptionModel.getObject());
                    applicationEntity.setInstalled(installedModel.getObject());
                    applicationEntity.setPreferredCalledAETitles(calledAETitlesModel.getArray());
                    applicationEntity.setPreferredCallingAETitles(callingAETitlesModel.getArray());
                    applicationEntity.setSupportedCharacterSets(supportedCharacterSetsModel.getArray());

                	if (isProxy) {
                    	ProxyAEExtension proxyAEExtension = applicationEntity.getAEExtension(ProxyAEExtension.class);
                		if (proxyAEExtension == null) {
                			proxyAEExtension = new ProxyAEExtension();
                			applicationEntity.addAEExtension(proxyAEExtension);
                		}
                		proxyAEExtension.setAcceptDataOnFailedAssociation(acceptDataOnFailedNegotiationModel.getObject());
                		proxyAEExtension.setEnableAuditLog(enableAuditLogModel.getObject());
                		proxyAEExtension.setSpoolDirectory(spoolDirectoryModel.getObject());
                		proxyAEExtension.setDeleteFailedDataWithoutRetryConfiguration(deleteFailedDataWithoutRetryConfigurationModel.getObject());
                		proxyAEExtension.setProxyPIXConsumerApplication(proxyPIXConsumerApplicationModel.getObject());
                		proxyAEExtension.setRemotePIXManagerApplication(remotePIXManagerApplicationModel.getObject());
                		proxyAEExtension.setFallbackDestinationAET(fallbackDestinationAETModel.getObject());                        
                	}
                    if (aeModel != null) {
                    	if (!"*".equals(oldAETitle))
                    		ConfigTreeProvider.get().unregisterAETitle(oldAETitle);
                    } else
                    	((DeviceModel) deviceNode.getModel()).getDevice().addApplicationEntity(applicationEntity);
                    ConfigTreeProvider.get().mergeDevice(applicationEntity.getDevice());
                    if (!"*".equals(applicationEntity.getAETitle()))
                    	ConfigTreeProvider.get().registerAETitle(applicationEntity.getAETitle());
                    window.close(target);
                } catch (Exception e) {
        			log.error(this.getClass().toString() + ": " + "Error modifying application entity: " + e.getMessage());
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
			}}.setDefaultFormProcessing(false));
    }
    
    @Override
    public void renderHead(IHeaderResponse response) {
    	if (CreateOrEditApplicationEntityPage.baseCSS != null) 
    		response.render(CssHeaderItem.forReference(CreateOrEditApplicationEntityPage.baseCSS));
    }
 }
