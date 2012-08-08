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

package org.dcm4chee.wizard.war.configuration.dicom;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.json.JSONObject;

import org.apache.wicket.Application;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.WindowClosedCallback;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.resource.ResourceReference;
import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Connection;
import org.dcm4che.net.Device;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.icons.behaviours.ImageSizeBehaviour;
import org.dcm4chee.web.common.ajax.MaskingAjaxCallBehavior;
import org.dcm4chee.web.common.behaviours.TooltipBehaviour;
import org.dcm4chee.web.common.markup.BaseForm;
import org.dcm4chee.web.common.markup.modal.ConfirmationWindow;
import org.dcm4chee.wizard.war.common.wickettree.ConnectionPanel;
import org.dcm4chee.wizard.war.common.wickettree.LinkPanel;
import org.dcm4chee.wizard.war.configuration.dicom.ConfigurationTreeNode.TreeNodeType;
import org.dcm4chee.wizard.war.configuration.dicom.DeviceTreeProvider.ConfigurationType;
import org.dcm4chee.wizard.war.configuration.dicom.edit.CreateOrEditApplicationEntityPage;
import org.dcm4chee.wizard.war.configuration.dicom.edit.CreateOrEditCoercionPage;
import org.dcm4chee.wizard.war.configuration.dicom.edit.CreateOrEditConnectionPage;
import org.dcm4chee.wizard.war.configuration.dicom.edit.CreateOrEditDevicePage;
import org.dcm4chee.wizard.war.configuration.dicom.edit.CreateOrEditForwardRulePage;
import org.dcm4chee.wizard.war.configuration.dicom.edit.CreateOrEditForwardSchedulePage;
import org.dcm4chee.wizard.war.configuration.dicom.edit.CreateOrEditRetryPage;
import org.dcm4chee.wizard.war.configuration.dicom.edit.CreateOrEditTransferCapabilityPage;
import org.dcm4chee.wizard.war.configuration.dicom.model.ApplicationEntityModel;
import org.dcm4chee.wizard.war.configuration.dicom.model.CoercionModel;
import org.dcm4chee.wizard.war.configuration.dicom.model.ConnectionModel;
import org.dcm4chee.wizard.war.configuration.dicom.model.DeviceModel;
import org.dcm4chee.wizard.war.configuration.dicom.model.DicomConfigurationSourceModel;
import org.dcm4chee.wizard.war.configuration.dicom.model.ForwardRuleModel;
import org.dcm4chee.wizard.war.configuration.dicom.model.ForwardScheduleModel;
import org.dcm4chee.wizard.war.configuration.dicom.model.RetryModel;
import org.dcm4chee.wizard.war.configuration.dicom.model.TransferCapabilityModel;
import org.dcm4chee.wizard.war.configuration.dicom.proxy.DicomConfigurationProxy;
import org.jboss.bootstrap.api.as.config.JBossASBasedServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wickettree.DefaultTableTree;
import wickettree.TableTree;
import wickettree.content.Folder;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class DicomConfigurationPanel extends Panel {

    private static final long serialVersionUID = 1L;

    private static final String MODULE_NAME = "dicom";

    private transient static Logger log = LoggerFactory.getLogger(DicomConfigurationPanel.class);
    
    private List<WebMarkupContainer> searchTableComponents = new ArrayList<WebMarkupContainer>();
    final MaskingAjaxCallBehavior macb = new MaskingAjaxCallBehavior();

	private BaseForm form;
	private boolean connected;
	private boolean connectFailed;

	private ModalWindow editWindow;
	private ModalWindow echoWindow;
	private ConfirmationWindow<ConfigurationTreeNode> removeConfirmation;
	public WindowClosedCallback windowClosedCallback;
	
	List<IColumn<ConfigurationTreeNode>> deviceColumns;
	TableTree<ConfigurationTreeNode> configTree;
	
    public DicomConfigurationPanel(final String id) {
        super(id);

        windowClosedCallback = 
        		new ModalWindow.WindowClosedCallback() {  
    
				    private static final long serialVersionUID = 1L;
				
				    public void onClose(AjaxRequestTarget target) {
				    	try {
//				    		if (isRefreshTableTree())
//				    			refreshTree(null);//DicomConfigurationProxy.listDevices());
				    		refreshTree();

				            target.add(form);
				    	} catch (ConfigurationException e) {
							log.error("Error generating configuration tree", e);
						}
				    }
				};
        add(macb);
        connected = false;
        connectFailed = false;

        add(form = new BaseForm("form"));
        form.setResourceIdPrefix("dicom.");
        form.add(new Label("connect.title", new Model<String>() {
        	
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {
				return Application.get().getResourceSettings().getLocalizer()
						.getString(connectFailed ? "dicom.connectFailed.title" : 
							connected ? "dicom.connected.title" : "dicom.connect.title", 
								DicomConfigurationPanel.this, "dicom.connect.title");
			}
        }).add(new AttributeAppender("class", 
        		new AbstractReadOnlyModel<String>() {

					private static final long serialVersionUID = 1L;
					
					@Override
					public String getObject() {
						return connectFailed ? "error" : "text"; 
					}
        		})
        ));
        
		editWindow = new ModalWindow("edit-window");
		editWindow
			.setInitialWidth(600)
			.setInitialHeight(400);
		
        AjaxLink<Object> createDevice = 
        		new AjaxLink<Object>("createDevice") {
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    editWindow
                    .setPageCreator(new ModalWindow.PageCreator() {
                        
                        private static final long serialVersionUID = 1L;
                          
                        @Override
                        public Page createPage() {
                            return new CreateOrEditDevicePage(editWindow, null);
                        }
                    }).show(target);
                }
            };
            editWindow.setWindowClosedCallback(windowClosedCallback);
            form.add(editWindow);            
            createDevice.add(new Image("createDeviceImg",ImageManager.IMAGE_WIZARD_DEVICE_ADD)
                .add(new ImageSizeBehaviour("vertical-align: middle;"))
            );
            createDevice.add(new TooltipBehaviour("dicom."));
            createDevice.add(new Label("createDeviceText", new ResourceModel("dicom.createDevice.text"))
                .add(new AttributeAppender("style", Model.of("vertical-align: middle"), " "))
            );
            form.add(createDevice);
            
    		echoWindow = new ModalWindow("echo-window");
    		echoWindow
    			.setInitialWidth(600)
    			.setInitialHeight(400);
            echoWindow.setWindowClosedCallback(windowClosedCallback);
            form.add(echoWindow);
            
            List<IColumn<ConfigurationTreeNode>> deviceColumns = new ArrayList<IColumn<ConfigurationTreeNode>>();
    		deviceColumns.add(new CustomTreeColumn(Model.of("Devices")));

    		configTree = 
    				new DefaultTableTree<ConfigurationTreeNode>("configTree", deviceColumns,
    				DeviceTreeProvider.set(DicomConfigurationPanel.this),  
    				Integer.MAX_VALUE);
    		form.addOrReplace(configTree);
    		
            removeConfirmation = new ConfirmationWindow<ConfigurationTreeNode>("remove-confirmation") {

                private static final long serialVersionUID = 1L;
                
                @Override
                public void onConfirmation(AjaxRequestTarget target, ConfigurationTreeNode configurationTreeNode) {
                	
            		try {
            			
                	if (configurationTreeNode.getType().equals(ConfigurationTreeNode.TreeNodeType.DEVICE)) {                	
                		DeviceTreeProvider.get().removeDevice(configurationTreeNode);

                	} else if (configurationTreeNode.getType().equals(ConfigurationTreeNode.TreeNodeType.CONNECTION)) {
                		DeviceTreeProvider.get().removeConnection(configurationTreeNode);
                		
                	} else if (configurationTreeNode.getType().equals(ConfigurationTreeNode.TreeNodeType.APPLICATION_ENTITY)) {
                		DeviceTreeProvider.get().removeApplicationEntity(configurationTreeNode);
                	
                	} else if (configurationTreeNode.getType().equals(ConfigurationTreeNode.TreeNodeType.TRANSFER_CAPABILITY)) {
                		DeviceTreeProvider.get().removeTransferCapability(configurationTreeNode);
 
                	} else if (configurationTreeNode.getType().equals(ConfigurationTreeNode.TreeNodeType.FORWARD_RULE)) {
                		DeviceTreeProvider.get().removeForwardRule(configurationTreeNode);

                	} else if (configurationTreeNode.getType().equals(ConfigurationTreeNode.TreeNodeType.FORWARD_SCHEDULE)) {
                		DeviceTreeProvider.get().removeForwardSchedule(configurationTreeNode);

                	} else if (configurationTreeNode.getType().equals(ConfigurationTreeNode.TreeNodeType.RETRY)) {
                		DeviceTreeProvider.get().removeRetry(configurationTreeNode);

                	} else if (configurationTreeNode.getType().equals(ConfigurationTreeNode.TreeNodeType.COERCION)) {
                		DeviceTreeProvider.get().removeCoercion(configurationTreeNode);

                	} else 
                		log.error("Missing type of ConfigurationTreeNode");

            		getSession().setAttribute("deviceTreeProvider", configTree.getProvider());

                	connected = true;
	                connectFailed = false;
            		target.add(form);
                    
    				} catch (ConfigurationException e) {
    					e.printStackTrace();
    				} catch (Exception e) {
    					e.printStackTrace();
    				}
            		}
                };
            removeConfirmation.setInitialHeight(150);
            removeConfirmation.setWindowClosedCallback(windowClosedCallback);
            form.add(removeConfirmation);

			try {
				createColumns();
				refreshTree();
				
                connected = true;
                connectFailed = false;
			} catch (ConfigurationException e) {
				log.error("Error connecting to dicom configuration source", e);
				handleFailedConnect();
			}
    }

    private void handleFailedConnect() {
        connected = false;
        connectFailed = true;
        
        List<IColumn<ConfigurationTreeNode>> deviceColumns = new ArrayList<IColumn<ConfigurationTreeNode>>();
		deviceColumns.add(new CustomTreeColumn(Model.of("Devices"))); 
		
//		TableTree<ConfigurationTreeNode> 
		configTree = new TableTree<ConfigurationTreeNode>("configTree", deviceColumns,
				DeviceTreeProvider.set(DicomConfigurationPanel.this), 
				Integer.MAX_VALUE) {

					private static final long serialVersionUID = 1L;

					@Override
					protected Component newContentComponent(String id,
							IModel<ConfigurationTreeNode> model) {
						return new Folder<ConfigurationTreeNode>(id, this, model);
					}};
		form.addOrReplace(configTree);
	}

    @Override
    public void renderHead(IHeaderResponse response) {
    	response.renderOnDomReadyJavaScript("Wicket.Window.unloadConfirmation = false");
    }
    
    // only used in constructor
    protected List<DicomConfigurationSourceModel> listDicomConfigurationSources() {
        String line;
        BufferedReader reader = null;
        List<DicomConfigurationSourceModel> updatedList = new ArrayList<DicomConfigurationSourceModel>();
        try {
        	String fn = System.getProperty("dcm4chee-web3.cfg.path", "conf/dcm4chee-web3/");
        	if (fn == null)
				throw new FileNotFoundException("Web config path not found! Not specified with System property 'dcm4chee-web3.cfg.path'");
	        File configFile = new File(fn + "dicom-configuration.json");
	        if (!configFile.isAbsolute())
	            configFile = new File(System.getProperty(JBossASBasedServerConfig.PROP_KEY_JBOSSAS_SERVER_HOME_DIR), configFile.getPath());

        	reader = new BufferedReader(new FileReader(configFile));
            while ((line = reader.readLine()) != null) 
            	updatedList.add((DicomConfigurationSourceModel) JSONObject.toBean(JSONObject.fromObject(line), DicomConfigurationSourceModel.class));
            
		} catch (IOException ioe) {
			log.error("Error updating dicom configuration list", ioe);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignore) {}
            }
        }
        return updatedList;
    }

    public void createColumns() {
		
    	deviceColumns = new ArrayList<IColumn<ConfigurationTreeNode>>();
		
    	deviceColumns.add(new CustomTreeColumn(Model.of("Devices")));
		
		deviceColumns.add(new AbstractColumn<ConfigurationTreeNode>(Model.of("ConfigurationType")) {

			private static final long serialVersionUID = 1L;

			public void populateItem(final Item<ICellPopulator<ConfigurationTreeNode>> cellItem, final String componentId, 
					final IModel<ConfigurationTreeNode> rowModel) {
				
				final ConfigurationType configurationType = rowModel.getObject().getConfigurationType();
				cellItem.add(new Label(componentId, 
						Model.of(configurationType == null ? "" : configurationType.toString())));
			}
		});

		deviceColumns.add(new AbstractColumn<ConfigurationTreeNode>(Model.of("Connections")) {
			
			private static final long serialVersionUID = 1L;

			public void populateItem(Item<ICellPopulator<ConfigurationTreeNode>> cellItem, String componentId, 
					IModel<ConfigurationTreeNode> rowModel) {
				ConfigurationTreeNode configurationTreeNode = (ConfigurationTreeNode) rowModel.getObject();
				RepeatingView connectionsView = new RepeatingView(componentId);
				cellItem.add(connectionsView);
				try {
					if (configurationTreeNode.getType().equals(ConfigurationTreeNode.TreeNodeType.APPLICATION_ENTITY)) {
						ApplicationEntity applicationEntity = 
								DeviceTreeProvider.get().getDicomConfigurationProxy()
									.getApplicationEntity(configurationTreeNode.getName());
						if (applicationEntity != null) 							
							for (Connection connection : applicationEntity.getConnections())
								connectionsView.add(new ConnectionPanel(connectionsView.newChildId(), 
										ImageManager.IMAGE_WIZARD_CONNECTION, 
										Model.of(connection.getCommonName()), 
										Model.of(connection.toString()))
								);
					}
				} catch (ConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		deviceColumns.add(new AbstractColumn<ConfigurationTreeNode>(Model.of("Echo")) {

			private static final long serialVersionUID = 1L;

			public void populateItem(final Item<ICellPopulator<ConfigurationTreeNode>> cellItem, final String componentId, 
					final IModel<ConfigurationTreeNode> rowModel) {
				
				final TreeNodeType type = rowModel.getObject().getType();
				if (type == null)
					throw new RuntimeException("Error: Unknown node type, cannot create edit modal window");

				AjaxLink<Object> ajaxLink = 
						new AjaxLink<Object>("wickettree.link") { 

			            private static final long serialVersionUID = 1L;

			            @Override
			            public void onClick(AjaxRequestTarget target) {
			            	
			            	if (type.equals(ConfigurationTreeNode.TreeNodeType.APPLICATION_ENTITY)) {
								echoWindow
					                .setPageCreator(new ModalWindow.PageCreator() {
					                    
					                    private static final long serialVersionUID = 1L;
					                      
					                    @Override
					                    public Page createPage() {
					                    	try {
					                    	return new DicomEchoPage(echoWindow, 
					                    			((ApplicationEntityModel) rowModel.getObject().getModel()).getApplicationEntity());
					                    	} catch (Exception e) {
					                    		e.printStackTrace();
					                    		return null;
					                    	}
					                    }
					                });
			            	}
			            	echoWindow
//			            		.setTitle(new ResourceModel("ae.echoPanelTitle"))
			            		.setWindowClosedCallback(windowClosedCallback)
			            		.show(target);
			            }
				};
				if (type.equals(ConfigurationTreeNode.TreeNodeType.APPLICATION_ENTITY))
					cellItem.add(new LinkPanel(componentId, ajaxLink, ImageManager.IMAGE_WIZARD_ECHO, removeConfirmation))
						.add(new AttributeAppender("style", Model.of("width: 50px; text-align: center;")));
				else
					cellItem.add(new Label(componentId));

			}
		});
		
		deviceColumns.add(new AbstractColumn<ConfigurationTreeNode>(Model.of("Edit")) {
			
			private static final long serialVersionUID = 1L;

			public void populateItem(final Item<ICellPopulator<ConfigurationTreeNode>> cellItem, final String componentId, 
					final IModel<ConfigurationTreeNode> rowModel) {
				
				final TreeNodeType type = rowModel.getObject().getType();
				if (type == null)
					throw new RuntimeException("Error: Unknown node type, cannot create edit modal window");

				AjaxLink<Object> ajaxLink = 
						new AjaxLink<Object>("wickettree.link") { 

			            private static final long serialVersionUID = 1L;

			            @Override
			            public void onClick(AjaxRequestTarget target) {
			            	
			            	if (type.equals(ConfigurationTreeNode.TreeNodeType.FOLDER_CONNECTIONS)) {
								editWindow
					                .setPageCreator(new ModalWindow.PageCreator() {
					                    
					                    private static final long serialVersionUID = 1L;
					                      
					                    @Override
					                    public Page createPage() {
					                    	return new CreateOrEditConnectionPage(
					                    			editWindow, 
					                    			null, 
					                    			rowModel.getObject()); 
					                    }
					                });
			            	}
							else if (type.equals(ConfigurationTreeNode.TreeNodeType.FOLDER_APPLICATION_ENTITIES)) {
								editWindow
					                .setPageCreator(new ModalWindow.PageCreator() {
					                    
					                    private static final long serialVersionUID = 1L;
					                      
					                    @Override
					                    public Page createPage() {
				                    		 return new CreateOrEditApplicationEntityPage(
				                    				 editWindow, 
				                    				 null,  
				                    				 rowModel.getObject());
					                    }
					                });
							} else if (type.equals(ConfigurationTreeNode.TreeNodeType.FOLDER_TRANSFER_CAPABILITIES)) {
								editWindow
					                .setPageCreator(new ModalWindow.PageCreator() {
					                    
					                    private static final long serialVersionUID = 1L;
					                      
					                    @Override
					                    public Page createPage() {
					                    	return new CreateOrEditTransferCapabilityPage(
					                    			editWindow, 
					                    			null, 
					                    			rowModel.getObject(), 
					                    			null); 
					                    }
					                });
							} else if (type.equals(ConfigurationTreeNode.TreeNodeType.FOLDER_FORWARD_RULES)) {
								editWindow
					                .setPageCreator(new ModalWindow.PageCreator() {
					                    
					                    private static final long serialVersionUID = 1L;
					                      
					                    @Override
					                    public Page createPage() {
					                    	return new CreateOrEditForwardRulePage(
					                    			editWindow, 
					                    			null, 
					                    			rowModel.getObject(), 
					                    			null); 
					                    }
					                });
							} else if (type.equals(ConfigurationTreeNode.TreeNodeType.FOLDER_FORWARD_SCHEDULES)) {
								editWindow
					                .setPageCreator(new ModalWindow.PageCreator() {
					                    
					                    private static final long serialVersionUID = 1L;
					                      
					                    @Override
					                    public Page createPage() {
					                    	return new CreateOrEditForwardSchedulePage(
					                    			editWindow, 
					                    			null, 
					                    			rowModel.getObject(), 
					                    			null); 
					                    }
					                });
							} else if (type.equals(ConfigurationTreeNode.TreeNodeType.FOLDER_RETRIES)) {
								editWindow
					                .setPageCreator(new ModalWindow.PageCreator() {
					                    
					                    private static final long serialVersionUID = 1L;
					                      
					                    @Override
					                    public Page createPage() {
					                    	return new CreateOrEditRetryPage(
					                    			editWindow, 
					                    			null, 
					                    			rowModel.getObject(), 
					                    			null); 
					                    }
					                });
							} else if (type.equals(ConfigurationTreeNode.TreeNodeType.FOLDER_COERCIONS)) {
								editWindow
					                .setPageCreator(new ModalWindow.PageCreator() {
					                    
					                    private static final long serialVersionUID = 1L;
					                      
					                    @Override
					                    public Page createPage() {
					                    	return new CreateOrEditCoercionPage(
					                    			editWindow, 
					                    			null, 
					                    			rowModel.getObject(), 
					                    			null); 
					                    }
					                });
							} else {
								editWindow
					                .setPageCreator(new ModalWindow.PageCreator() {
					                    
					                    private static final long serialVersionUID = 1L;
					                      
					                    @Override
					                    public Page createPage() {
					        				if (type.equals(ConfigurationTreeNode.TreeNodeType.DEVICE)) {
					        	                return new CreateOrEditDevicePage(editWindow, 
					        	                		(DeviceModel) rowModel.getObject().getModel());			        				
					        				} else if (type.equals(ConfigurationTreeNode.TreeNodeType.CONNECTION)) {
					        					return new CreateOrEditConnectionPage(
					        							editWindow, 
					        							(ConnectionModel) rowModel.getObject().getModel(), 
					        		            		rowModel.getObject().getParent()); 
					        				} else if (type.equals(ConfigurationTreeNode.TreeNodeType.APPLICATION_ENTITY)) {
					        					return new CreateOrEditApplicationEntityPage(
					        							editWindow, 
					        							(ApplicationEntityModel) rowModel.getObject().getModel(), 
					        		            		rowModel.getObject().getParent()); 
					        				} else if (type.equals(ConfigurationTreeNode.TreeNodeType.TRANSFER_CAPABILITY)) {
					        		            return new CreateOrEditTransferCapabilityPage(editWindow, 
					        		            		(TransferCapabilityModel) rowModel.getObject().getModel(), 
					        		            		rowModel.getObject().getParent().getParent(), 
					        		            		rowModel.getObject());
					        				} else if (type.equals(ConfigurationTreeNode.TreeNodeType.FORWARD_RULE)) {
					        		            return new CreateOrEditForwardRulePage(editWindow, 
					        		            		(ForwardRuleModel) rowModel.getObject().getModel(), 
					        		            		rowModel.getObject().getParent(), 
					        		            		rowModel.getObject());
					        				} else if (type.equals(ConfigurationTreeNode.TreeNodeType.FORWARD_SCHEDULE)) {
					        		            return new CreateOrEditForwardSchedulePage(editWindow, 
					        		            		(ForwardScheduleModel) rowModel.getObject().getModel(), 
					        		            		rowModel.getObject().getParent(), 
					        		            		rowModel.getObject());
					        				} else if (type.equals(ConfigurationTreeNode.TreeNodeType.RETRY)) {
					        		            return new CreateOrEditRetryPage(editWindow, 
					        		            		(RetryModel) rowModel.getObject().getModel(), 
					        		            		rowModel.getObject().getParent(), 
					        		            		rowModel.getObject());
					        				} else if (type.equals(ConfigurationTreeNode.TreeNodeType.COERCION)) {
					        		            return new CreateOrEditCoercionPage(editWindow, 
					        		            		(CoercionModel) rowModel.getObject().getModel(), 
					        		            		rowModel.getObject().getParent(), 
					        		            		rowModel.getObject());
					        				} else 
					        					return null;
					                    }
					                });
							}
			            	editWindow
			            		.setWindowClosedCallback(windowClosedCallback)
			            		.show(target);
			            }
				};
				ajaxLink.setVisible(!type.equals(ConfigurationTreeNode.TreeNodeType.FOLDER_APPLICATION_ENTITIES)
						|| rowModel.getObject().getParent().getChildren().get(0).hasChildren());
				
				if (type.equals(ConfigurationTreeNode.TreeNodeType.FOLDER_FORWARD_SCHEDULES))
					ajaxLink.setVisible(DeviceTreeProvider.get().getUniqueAETitles().size() > 0);
					
				ResourceReference image;
				if (type.equals(ConfigurationTreeNode.TreeNodeType.FOLDER_CONNECTIONS)
						|| type.equals(ConfigurationTreeNode.TreeNodeType.FOLDER_APPLICATION_ENTITIES)
						|| type.equals(ConfigurationTreeNode.TreeNodeType.FOLDER_TRANSFER_CAPABILITIES)
						|| type.equals(ConfigurationTreeNode.TreeNodeType.FOLDER_FORWARD_RULES)
						|| type.equals(ConfigurationTreeNode.TreeNodeType.FOLDER_FORWARD_SCHEDULES)
						|| type.equals(ConfigurationTreeNode.TreeNodeType.FOLDER_RETRIES)
						|| type.equals(ConfigurationTreeNode.TreeNodeType.FOLDER_COERCIONS))
					image = ImageManager.IMAGE_WIZARD_COMMON_ADD;
				else
					image = ImageManager.IMAGE_WIZARD_COMMON_EDIT;

				if (type.equals(ConfigurationTreeNode.TreeNodeType.FOLDER_TRANSFER_CAPABILITY_TYPE))
					cellItem.add(new Label(componentId));
				else
					cellItem.add(new LinkPanel(componentId, ajaxLink, image, removeConfirmation))
						.add(new AttributeAppender("style", Model.of("width: 50px; text-align: center;")));
			}
		});
		
		deviceColumns.add(new AbstractColumn<ConfigurationTreeNode>(Model.of("Delete")) {
			
			private static final long serialVersionUID = 1L;

			public void populateItem(Item<ICellPopulator<ConfigurationTreeNode>> cellItem, String componentId, 
					final IModel<ConfigurationTreeNode> rowModel) {

				final TreeNodeType type = rowModel.getObject().getType();
				if (type == null)
					throw new RuntimeException("Error: Unknown node type, cannot create delete modal window");
				else if (type.equals(ConfigurationTreeNode.TreeNodeType.FOLDER_CONNECTIONS)
						|| type.equals(ConfigurationTreeNode.TreeNodeType.FOLDER_APPLICATION_ENTITIES)
						|| type.equals(ConfigurationTreeNode.TreeNodeType.FOLDER_TRANSFER_CAPABILITIES)
						|| type.equals(ConfigurationTreeNode.TreeNodeType.FOLDER_TRANSFER_CAPABILITY_TYPE)
						|| type.equals(ConfigurationTreeNode.TreeNodeType.FOLDER_FORWARD_RULES)
						|| type.equals(ConfigurationTreeNode.TreeNodeType.FOLDER_FORWARD_SCHEDULES)
						|| type.equals(ConfigurationTreeNode.TreeNodeType.FOLDER_RETRIES)
						|| type.equals(ConfigurationTreeNode.TreeNodeType.FOLDER_COERCIONS)) {
					cellItem.add(new Label(componentId));
					return;
				}

				AjaxLink<Object> ajaxLink = 
						new AjaxLink<Object>("wickettree.link") { 

				            private static final long serialVersionUID = 1L;

				            @Override
				            public void onClick(AjaxRequestTarget target) {
				                removeConfirmation
				                	.confirm(target, 
				                			new StringResourceModel("dicom.confirmDelete", DicomConfigurationPanel.this, null), 
				                					rowModel.getObject());
				            }
				        };
				        cellItem.add(new LinkPanel(componentId, ajaxLink, ImageManager.IMAGE_WIZARD_COMMON_REMOVE, removeConfirmation))
							.add(new AttributeAppender("style", Model.of("width: 50px; text-align: center;")));
			}
		});

    }
    
	public void refreshTree() throws ConfigurationException {

		IModel<Set<ConfigurationTreeNode>> currentState = configTree.getModel();

		configTree = new DefaultTableTree<ConfigurationTreeNode>("configTree", deviceColumns,
				DeviceTreeProvider.get(), 
				Integer.MAX_VALUE);

		if (currentState != null) 
			for (ConfigurationTreeNode newNode : DeviceTreeProvider.get().getNodeList()) 
				preserveState(currentState, newNode);
		
		form.addOrReplace(configTree);
	}
	
	private void preserveState(IModel<Set<ConfigurationTreeNode>> currentState, ConfigurationTreeNode newNode) {
		if (newNode.hasChildren()) {
			Iterator<ConfigurationTreeNode> iterator = currentState.getObject().iterator();
			while (iterator.hasNext()) {
				ConfigurationTreeNode currentNode = iterator.next();
				if (currentNode.equals(newNode)) {
					configTree.expand(newNode);
					for (ConfigurationTreeNode child : newNode.getChildren()) 
							preserveState(currentState, child);
				}
			}
		}
	}

    public static String getModuleName() {
        return MODULE_NAME;
    }
}
