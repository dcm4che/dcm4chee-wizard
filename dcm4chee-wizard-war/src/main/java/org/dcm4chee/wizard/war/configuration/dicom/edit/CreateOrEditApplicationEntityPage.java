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

package org.dcm4chee.wizard.war.configuration.dicom.edit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
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
import org.dcm4chee.proxy.conf.ForwardRule;
import org.dcm4chee.proxy.conf.ProxyApplicationEntity;
import org.dcm4chee.proxy.conf.ProxyDevice;
import org.dcm4chee.proxy.conf.Schedule;
import org.dcm4chee.web.common.base.BaseWicketPage;
import org.dcm4chee.web.common.behaviours.FocusOnLoadBehaviour;
import org.dcm4chee.web.common.markup.BaseForm;
import org.dcm4chee.web.common.markup.modal.MessageWindow;
import org.dcm4chee.wizard.war.configuration.dicom.ConfigurationTreeNode;
import org.dcm4chee.wizard.war.configuration.dicom.DeviceTreeProvider;
import org.dcm4chee.wizard.war.configuration.dicom.model.ApplicationEntityModel;
import org.dcm4chee.wizard.war.configuration.dicom.model.ConnectionReferenceModel;
import org.dcm4chee.wizard.war.configuration.dicom.model.DeviceModel;
import org.dcm4chee.wizard.war.configuration.dicom.model.StringArrayModel;
import org.dcm4chee.wizard.war.configuration.dicom.validator.AETitleValidator;
import org.dcm4chee.wizard.war.configuration.dicom.validator.ConnectionReferenceValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.security.components.SecureWebPage;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class CreateOrEditApplicationEntityPage extends SecureWebPage {
    
    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(CreateOrEditApplicationEntityPage.class);
    
    private static final ResourceReference BaseCSS = new CssResourceReference(BaseWicketPage.class, "base-style.css");
    
    private MessageWindow msgWin = new MessageWindow("msgWin");

    private boolean isProxy = false;
    
    // mandatory
    private Model<String> aeTitleModel;
    private Model<Boolean> associationAcceptorModel;
    private Model<Boolean> associationInitiatorModel;
	private Model<ArrayList<ConnectionReferenceModel>> connectionReferencesModel;
	// ProxyApplicationEntity only
	private Model<Boolean> acceptDataOnFailedNegotiationModel;
	private Model<Boolean> enableAuditLogModel;
	private Model<String> spoolDirectoryModel;

	// optional
	private StringArrayModel applicationClustersModel;
	private Model<String> descriptionModel;
	private Model<Boolean> installedModel;
	private StringArrayModel calledAETitlesModel;
	private StringArrayModel callingAETitlesModel;
	private StringArrayModel supportedCharacterSetsModel;
	private Model<String> vendorDataModel;	
	
    public CreateOrEditApplicationEntityPage(final ModalWindow window, final ApplicationEntityModel aeModel, 
    		final ConfigurationTreeNode aesNode) {
    	super();

        msgWin.setTitle("");
        add(msgWin);
        add(new WebMarkupContainer("create-applicationEntity-title").setVisible(aeModel == null));
        add(new WebMarkupContainer("edit-applicationEntity-title").setVisible(aeModel != null));

        setOutputMarkupId(true);
        final BaseForm form = new BaseForm("form");
        form.setResourceIdPrefix("dicom.edit.applicationEntity.");
        add(form);

        final String oldAETitle = aeModel == null ? null : aeModel.getAETitle();
        
        ArrayList<ConnectionReferenceModel> connectionReferences = new ArrayList<ConnectionReferenceModel>();
        try {
	        isProxy = (((DeviceModel) aesNode.getParent().getModel()).getDevice() instanceof ProxyDevice);
	        
	        connectionReferencesModel = new Model<ArrayList<ConnectionReferenceModel>>();
	        connectionReferencesModel.setObject(new ArrayList<ConnectionReferenceModel>());
			for (Connection connection : ((DeviceModel) aesNode.getParent().getModel()).getDevice().listConnections()) {
				ConnectionReferenceModel connectionReference = 
						new ConnectionReferenceModel(connection.getCommonName(), connection.getHostname(), connection.getPort());
				connectionReferences.add(connectionReference);
				if 	(aeModel != null && aeModel.getApplicationEntity().getConnections().contains(connection))
					connectionReferencesModel.getObject().add(connectionReference);
			}

			if (aeModel == null) {
		        aeTitleModel = Model.of();
		        associationAcceptorModel = Model.of();
		        associationInitiatorModel = Model.of();       

				acceptDataOnFailedNegotiationModel = Model.of(false);
				enableAuditLogModel = Model.of(false);
				spoolDirectoryModel = Model.of();

		        applicationClustersModel = new StringArrayModel(null);
				descriptionModel = Model.of();
				installedModel = Model.of();
				calledAETitlesModel = new StringArrayModel(null);
				callingAETitlesModel = new StringArrayModel(null);
				supportedCharacterSetsModel = new StringArrayModel(null);
				vendorDataModel = Model.of("size 0");
			} else {
		        aeTitleModel = Model.of(aeModel.getAETitle());
		        associationAcceptorModel = Model.of(aeModel.getApplicationEntity().isAssociationAcceptor());
		        associationInitiatorModel = Model.of(aeModel.getApplicationEntity().isAssociationInitiator());       

		        acceptDataOnFailedNegotiationModel = Model.of(isProxy ? 
     					((ProxyApplicationEntity) aeModel.getApplicationEntity()).isAcceptDataOnFailedNegotiation() : false);
				enableAuditLogModel = Model.of(isProxy ? 
     					((ProxyApplicationEntity) aeModel.getApplicationEntity()).isEnableAuditLog() : false);
				spoolDirectoryModel = Model.of(isProxy ? 
     					((ProxyApplicationEntity) aeModel.getApplicationEntity()).getSpoolDirectory() : null);

		        applicationClustersModel = new StringArrayModel(aeModel.getApplicationEntity().getApplicationClusters());
				descriptionModel = Model.of(aeModel.getApplicationEntity().getDescription());
				installedModel = Model.of(aeModel.getApplicationEntity().getInstalled());
				calledAETitlesModel = new StringArrayModel(aeModel.getApplicationEntity().getPreferredCalledAETitles());
				callingAETitlesModel = new StringArrayModel(aeModel.getApplicationEntity().getPreferredCallingAETitles());
				supportedCharacterSetsModel = new StringArrayModel(aeModel.getApplicationEntity().getSupportedCharacterSets());
				vendorDataModel = Model.of("size " + aeModel.getApplicationEntity().getVendorData().length);				
			}
		} catch (ConfigurationException ce) {
			log.error("Error creating connection reference list", ce);
		}
		
        form.add(new Label("aeTitle.label", new ResourceModel("dicom.edit.applicationEntity.aeTitle.label")))
        .add(new TextField<String>("aeTitle", aeTitleModel)
        		.add(new AETitleValidator(aeTitleModel.getObject(), DeviceTreeProvider.get().getUniqueAETitles()))
                .setRequired(true).add(FocusOnLoadBehaviour.newFocusAndSelectBehaviour()));

        form.add(new Label("associationAcceptor.label", new ResourceModel("dicom.edit.applicationEntity.associationAcceptor.label")))
        .add(new CheckBox("associationAcceptor", associationAcceptorModel));
        form.add(new Label("associationInitiator.label", new ResourceModel("dicom.edit.applicationEntity.associationInitiator.label")))
        .add(new CheckBox("associationInitiator", associationInitiatorModel));
        
        form.add(new CheckBoxMultipleChoice<ConnectionReferenceModel>("connections", 
        		connectionReferencesModel,
        		new Model<ArrayList<ConnectionReferenceModel>>(connectionReferences), 
        		new IChoiceRenderer<ConnectionReferenceModel>() {

					private static final long serialVersionUID = 1L;

					public Object getDisplayValue(ConnectionReferenceModel connectionReference) {
						return connectionReference.getCommonName() 
								+ " (" + connectionReference.getHostname() 
								+ ":" + connectionReference.getPort() 
								+ ")";
					}

					public String getIdValue(ConnectionReferenceModel model, int index) {
						return String.valueOf(index);
					}
        		}).add(new ConnectionReferenceValidator()));

        form.add(new Label("acceptDataOnFailedNegotiation.label", new ResourceModel("dicom.edit.applicationEntity.acceptDataOnFailedNegotiation.label"))
        	.setVisible(isProxy))
        .add(new CheckBox("acceptDataOnFailedNegotiation", acceptDataOnFailedNegotiationModel)
        	.setVisible(isProxy));

        form.add(new Label("enableAuditLog.label", new ResourceModel("dicom.edit.applicationEntity.enableAuditLog.label"))
    		.setVisible(isProxy))
    	.add(new CheckBox("enableAuditLog", enableAuditLogModel)
    		.setVisible(isProxy));

        form.add(new Label("spoolDirectory.label", new ResourceModel("dicom.edit.applicationEntity.spoolDirectory.label"))
    		.setVisible(isProxy))
    	.add(new TextField<String>("spoolDirectory", spoolDirectoryModel)
    		.setRequired(true)
    		.setVisible(isProxy));      
        
        final WebMarkupContainer optionalContainer = new WebMarkupContainer("optionalContainer");
        form.add(optionalContainer
        		.setOutputMarkupId(true)
        		.setOutputMarkupPlaceholderTag(true)
        		.setVisible(false));
        
        optionalContainer.add(new Label("applicationClusters.label", new ResourceModel("dicom.edit.applicationEntity.applicationClusters.label")))
        .add(new TextArea<String>("applicationClusters", applicationClustersModel));
        
        optionalContainer.add(new Label("description.label", new ResourceModel("dicom.edit.applicationEntity.description.label")))
        .add(new TextField<String>("description", descriptionModel));

        optionalContainer.add(new Label("installed.label", new ResourceModel("dicom.edit.connection.installed.label")))
        .add(new CheckBox("installed", installedModel));

        optionalContainer.add(new Label("calledAETitles.label", new ResourceModel("dicom.edit.applicationEntity.calledAETitles.label")))
        .add(new TextArea<String>("calledAETitles", calledAETitlesModel));

        optionalContainer.add(new Label("callingAETitles.label", new ResourceModel("dicom.edit.applicationEntity.callingAETitles.label")))
        .add(new TextArea<String>("callingAETitles", callingAETitlesModel));

        optionalContainer.add(new Label("supportedCharacterSets.label", new ResourceModel("dicom.edit.applicationEntity.supportedCharacterSets.label")))
        .add(new TextArea<String>("supportedCharacterSets", supportedCharacterSetsModel));
    	
    	optionalContainer.add(new Label("vendorData.label", 
    			new ResourceModel("dicom.edit.applicationEntity.vendorData.label")))
    	.add(new Label("vendorData", vendorDataModel));
    	
        form.add(new Label("optional.label", new ResourceModel("dicom.edit.optional.label")))
        .add(new AjaxCheckBox("optional", new Model<Boolean>()) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(optionalContainer.setVisible(this.getModelObject()));
			}
        });        
        
        form.add(new AjaxFallbackButton("submit", new ResourceModel("saveBtn"), form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                	ApplicationEntity applicationEntity;
                	if (isProxy)
                		applicationEntity = aeModel == null ? 
                				new ProxyApplicationEntity(aeTitleModel.getObject()) : (ProxyApplicationEntity) aeModel.getApplicationEntity();
                	else
	                	applicationEntity = aeModel == null ? 
	                			new ApplicationEntity(aeTitleModel.getObject()) : aeModel.getApplicationEntity();

                    applicationEntity.setAETitle(aeTitleModel.getObject());
                    applicationEntity.setAssociationAcceptor(associationAcceptorModel.getObject());
                    applicationEntity.setAssociationInitiator(associationInitiatorModel.getObject());
                	applicationEntity.getConnections().clear();
                	for (ConnectionReferenceModel connectionReference : connectionReferencesModel.getObject()) 
                		for (Connection connection : ((DeviceModel) aesNode.getParent().getModel()).getDevice().listConnections()) 
                			if (connectionReference.getHostname().equals(connection.getHostname())
                    				&& connectionReference.getPort().equals(connection.getPort()))
                    			applicationEntity.addConnection(connection);
                	
                	applicationEntity.setApplicationClusters(applicationClustersModel.getArray());
                    applicationEntity.setDescription(descriptionModel.getObject());
                    applicationEntity.setInstalled(installedModel.getObject());
                    applicationEntity.setPreferredCalledAETitles(calledAETitlesModel.getArray());
                    applicationEntity.setPreferredCallingAETitles(callingAETitlesModel.getArray());
                    applicationEntity.setSupportedCharacterSets(supportedCharacterSetsModel.getArray());

                	if (isProxy) {
                    	ProxyApplicationEntity proxyApplicationEntity = 
                    			(ProxyApplicationEntity) applicationEntity;
                    	proxyApplicationEntity
                    		.setAcceptDataOnFailedNegotiation(acceptDataOnFailedNegotiationModel.getObject());
                    	proxyApplicationEntity.setEnableAuditLog(enableAuditLogModel.getObject());
                    	proxyApplicationEntity.setSpoolDirectory(spoolDirectoryModel.getObject());
                    	
                    	// TODO: workaround because if not set will throw a null pointer exception on persisting
                    	proxyApplicationEntity.setForwardSchedules(new HashMap<String,Schedule>());
                    	
                    	List<ForwardRule> forwardRules = new ArrayList<ForwardRule>();
						proxyApplicationEntity.setForwardRules(forwardRules);
                	}

                    if (aeModel == null)
                    	DeviceTreeProvider.get().addApplicationEntity(aesNode, applicationEntity);
                    else
                    	DeviceTreeProvider.get().editApplicationEntity(aesNode, applicationEntity, oldAETitle);
                	

                    window.close(target);
                } catch (Exception e) {
                	log.error("Error modifying application entity", e);
                    msgWin.show(target, new ResourceModel(aeModel == null ? 
                    		"dicom.edit.applicationEntity.create.failed" : "dicom.edit.applicationEntity.update.failed")
                    		.wrapOnAssignment(this));
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
        if (CreateOrEditApplicationEntityPage.BaseCSS != null)
        	response.renderCSSReference(CreateOrEditApplicationEntityPage.BaseCSS);
    }    
 }
