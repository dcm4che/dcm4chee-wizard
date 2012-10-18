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

package org.dcm4chee.wizard.war.configuration.simple.tree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.json.JSONObject;

import org.apache.wicket.Application;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.WindowClosedCallback;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.repeater.IItemReuseStrategy;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.time.Duration;
import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Connection;
import org.dcm4che.net.Device;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.icons.behaviours.ImageSizeBehaviour;
import org.dcm4chee.proxy.conf.ProxyApplicationEntity;
import org.dcm4chee.wizard.common.behavior.MaskingAjaxCallBehavior;
import org.dcm4chee.wizard.common.behavior.TooltipBehavior;
import org.dcm4chee.wizard.common.component.ConfirmationWindow;
import org.dcm4chee.wizard.common.component.ExtendedForm;
import org.dcm4chee.wizard.common.component.MessageWindow;
import org.dcm4chee.wizard.war.WizardApplication;
import org.dcm4chee.wizard.war.common.component.ExtendedPanel;
import org.dcm4chee.wizard.war.configuration.model.source.DicomConfigurationSourceModel;
import org.dcm4chee.wizard.war.configuration.simple.edit.ApplyTransferCapabilityProfilePage;
import org.dcm4chee.wizard.war.configuration.simple.edit.CreateOrEditApplicationEntityPage;
import org.dcm4chee.wizard.war.configuration.simple.edit.CreateOrEditCoercionPage;
import org.dcm4chee.wizard.war.configuration.simple.edit.CreateOrEditConnectionPage;
import org.dcm4chee.wizard.war.configuration.simple.edit.CreateOrEditDevicePage;
import org.dcm4chee.wizard.war.configuration.simple.edit.CreateOrEditForwardRulePage;
import org.dcm4chee.wizard.war.configuration.simple.edit.CreateOrEditForwardSchedulePage;
import org.dcm4chee.wizard.war.configuration.simple.edit.CreateOrEditRetryPage;
import org.dcm4chee.wizard.war.configuration.simple.edit.CreateOrEditTransferCapabilityPage;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.ApplicationEntityModel;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.ConnectionModel;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.DeviceModel;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.TransferCapabilityModel;
import org.dcm4chee.wizard.war.configuration.simple.model.proxy.CoercionModel;
import org.dcm4chee.wizard.war.configuration.simple.model.proxy.ForwardRuleModel;
import org.dcm4chee.wizard.war.configuration.simple.model.proxy.ForwardScheduleModel;
import org.dcm4chee.wizard.war.configuration.simple.model.proxy.RetryModel;
import org.dcm4chee.wizard.war.configuration.simple.tree.ConfigTreeNode.TreeNodeType;
import org.dcm4chee.wizard.war.configuration.simple.tree.ConfigTreeProvider.ConfigurationType;
import org.dcm4chee.wizard.war.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wickettree.TableTree;
import wickettree.content.Folder;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class BasicConfigurationPanel extends ExtendedPanel {

    private static final long serialVersionUID = 1L;

    private static final String MODULE_NAME = "dicom";

    private static Logger log = LoggerFactory.getLogger(BasicConfigurationPanel.class);

    private ExtendedForm form;

    private ModalWindow editWindow;
    private ModalWindow echoWindow;
    private ConfirmationWindow<ConfigTreeNode> removeConfirmation;
    private MessageWindow refreshMessage;
    private MessageWindow reloadMessage;
    public WindowClosedCallback windowClosedCallback;

    List<IColumn<ConfigTreeNode>> deviceColumns;
    TableTree<ConfigTreeNode> configTree;

    private String connectedDeviceName;
    
    public BasicConfigurationPanel(final String id) {
        super(id);

        try {
        	connectedDeviceName = 
        			((WizardApplication) this.getApplication()).getConnectedDeviceName();
        	add(new Label("reload-message"));
        	
	        add(new AbstractAjaxTimerBehavior(Duration.seconds(
	        		Integer.parseInt(((WebApplication) 
        			this.getApplication()).getInitParameter("CheckForChangesInterval")))) {
	            
	            private static final long serialVersionUID = 1L;

	            @Override
	            protected void onTimer(AjaxRequestTarget target) {
					if (ConfigTreeProvider.get().getLastModificationTime()
							.before(getDicomConfigurationManager().getLastModificationTime())) {
						log.warn("Configuration needs to be reloaded because of concurrent modification");
						refreshMessage.show(target);
					}
	            }
	        });
        } catch (Exception e) {
        	log.error("Error creating timer for checking changes", e);
        }
        
        windowClosedCallback = new ModalWindow.WindowClosedCallback() {

            private static final long serialVersionUID = 1L;

            public void onClose(AjaxRequestTarget target) {
                try {
	            	boolean refresh = false;
	                for (ConfigTreeNode deviceNode : ConfigTreeProvider.get().getNodeList())
	                    if (deviceNode.getModel() == null) {
	                    	ConfigTreeProvider.get().loadDevice(deviceNode);
	                    	refresh = true;
	                    }
	                if (refresh || ConfigTreeProvider.get().resync())
                        renderTree();
                } catch (ConfigurationException ce) {
                	log.error(this.getClass().toString() + ": " + "Error refreshing tree: " + ce.getMessage());
                	log.debug("Exception", ce);
                	throw new RuntimeException(ce);
                }
                target.add(form);
            }
        };

        echoWindow = new ModalWindow("echo-window");
        echoWindow.setInitialWidth(600).setInitialHeight(400);
        echoWindow.setWindowClosedCallback(windowClosedCallback);
        add(echoWindow);

        editWindow = new ModalWindow("edit-window");
        editWindow.setInitialWidth(600).setInitialHeight(400);
        editWindow.setWindowClosedCallback(windowClosedCallback);
        add(editWindow);

        removeConfirmation = new ConfirmationWindow<ConfigTreeNode>("remove-confirmation") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onConfirmation(AjaxRequestTarget target, ConfigTreeNode node) {
                try {
                    if (node.getNodeType().equals(ConfigTreeNode.TreeNodeType.DEVICE)) {
                        ConfigTreeProvider.get().removeDevice(node);
                    } else {
                    	ConfigTreeNode deviceNode = node.getRoot();

                        if (node.getNodeType().equals(ConfigTreeNode.TreeNodeType.CONNECTION)) {
                            ((DeviceModel) deviceNode.getModel()).getDevice()
                                    .removeConnection(((ConnectionModel) node.getModel()).getConnection());

                        } else if (node.getNodeType().equals(ConfigTreeNode.TreeNodeType.APPLICATION_ENTITY)) {
                        	ApplicationEntity applicationEntity = 
                        			((ApplicationEntityModel) node.getModel()).getApplicationEntity();
                        	((DeviceModel) deviceNode.getModel()).getDevice()
                                    .removeApplicationEntity(applicationEntity);
                        	ConfigTreeProvider.get().unregisterAETitle(applicationEntity.getAETitle());

                        } else if (node.getNodeType().equals(ConfigTreeNode.TreeNodeType.TRANSFER_CAPABILITY)) {
                        	((ApplicationEntityModel) node.getAncestor(3).getModel()).getApplicationEntity()
                            	.removeTransferCapability(((TransferCapabilityModel) node.getModel())
                            			.getTransferCapability());

                        } else if (node.getNodeType().equals(ConfigTreeNode.TreeNodeType.FORWARD_RULE)) {
                        	((ProxyApplicationEntity) 
                        			((ApplicationEntityModel) node.getAncestor(2).getModel()).getApplicationEntity())
                        			.getForwardRules().remove(((ForwardRuleModel) node.getModel())
                        					.getForwardRule());

                        } else if (node.getNodeType().equals(ConfigTreeNode.TreeNodeType.FORWARD_SCHEDULE)) {
                        	((ProxyApplicationEntity) 
                        			((ApplicationEntityModel) node.getAncestor(2).getModel()).getApplicationEntity())
                        			.getForwardSchedules().remove(((ForwardScheduleModel) node.getModel())
                        					.getDestinationAETitle());

                        } else if (node.getNodeType().equals(ConfigTreeNode.TreeNodeType.RETRY)) {
                        	((ProxyApplicationEntity) 
                        			((ApplicationEntityModel) node.getAncestor(2).getModel()).getApplicationEntity())
                        				.getRetries().remove(((RetryModel) node.getModel())
                        						.getRetry());

                        } else if (node.getNodeType().equals(ConfigTreeNode.TreeNodeType.COERCION)) {
                        	((ProxyApplicationEntity) 
                        			((ApplicationEntityModel) node.getAncestor(2).getModel()).getApplicationEntity())
                        				.getAttributeCoercions().remove(((CoercionModel) node.getModel())
                        						.getCoercion());

                        } else {
                            log.error("Missing type of ConfigurationTreeNode");
                            return;
                        }
                        ConfigTreeProvider.get().mergeDevice(((DeviceModel) deviceNode.getModel()).getDevice());
                    }
                } catch (Exception e) {
                	log.error(this.getClass().toString() + ": " + "Error deleting configuration object: " + e.getMessage());
                	e.printStackTrace();
                	log.debug("Exception", e);
                	throw new RuntimeException(e);
				}
            }
        };
        add(removeConfirmation.setInitialHeight(150)
        		.setWindowClosedCallback(windowClosedCallback));

        refreshMessage = new MessageWindow("refresh-message", 
        		new StringResourceModel("dicom.confirmRefresh", this, null)) {

            private static final long serialVersionUID = 1L;

            @Override
            public void onOk(AjaxRequestTarget target) {
                try {
					log.warn("Reloading device list from configuration");
					ConfigTreeProvider.get().loadDeviceList();				
					for (Iterator<ConfigTreeNode> i = 
							configTree.getModel().getObject().iterator(); i.hasNext(); ) {
						ConfigTreeNode root = i.next().getRoot();
						for (ConfigTreeNode deviceNode : ConfigTreeProvider.get().getNodeList())
							if (deviceNode.equals(root) && 
									(((DeviceModel) deviceNode.getModel()).getDevice() == null))								
							ConfigTreeProvider.get().loadDevice(deviceNode);
					}
					renderTree();
				} catch (ConfigurationException ce) {
					log.error("Error reloading configuration after concurrent modification", ce);
				}
            }
        };
        add(refreshMessage.setInitialHeight(150)
        		.setWindowClosedCallback(windowClosedCallback));
        
        add(form = new ExtendedForm("form"));
        form.setResourceIdPrefix("dicom.");

        AjaxLink<Object> createDevice = new AjaxLink<Object>("createDevice") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                editWindow.setPageCreator(new ModalWindow.PageCreator() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public Page createPage() {
                        return new CreateOrEditDevicePage(editWindow, null);
                    }
                }).show(target);
            }
        };
        createDevice.add(new Image("createDeviceImg", ImageManager.IMAGE_WIZARD_DEVICE_ADD).add(new ImageSizeBehaviour(
                "vertical-align: middle;")));
        createDevice.add(new TooltipBehavior("dicom."));
        createDevice.add(new Label("createDeviceText", new ResourceModel("dicom.createDevice.text"))
                .add(new AttributeAppender("style", Model.of("vertical-align: middle"), " ")));
        form.add(createDevice);

        List<IColumn<ConfigTreeNode>> deviceColumns = new ArrayList<IColumn<ConfigTreeNode>>();
        deviceColumns.add(new CustomTreeColumn(Model.of("Devices")));

        try {
            createColumns();
        	configTree =
            		new ConfigTableTree("configTree", deviceColumns, 
            				ConfigTreeProvider.init(BasicConfigurationPanel.this), Integer.MAX_VALUE);
        	renderTree();
        } catch (ConfigurationException ce) {
        	log.error(this.getClass().toString() + ": " + "Error creating tree: " + ce.getMessage());
        	log.debug("Exception", ce);
        	throw new RuntimeException(ce);
        }
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.renderOnDomReadyJavaScript("Wicket.Window.unloadConfirmation = false");
    }

    public void createColumns() {

    	deviceColumns = new ArrayList<IColumn<ConfigTreeNode>>();
    	
    	deviceColumns.add(new CustomTreeColumn(Model.of("Devices")));

		deviceColumns.add(new AbstractColumn<ConfigTreeNode>(Model.of("ConfigurationType")) {

			private static final long serialVersionUID = 1L;

			public void populateItem(final Item<ICellPopulator<ConfigTreeNode>> cellItem, final String componentId, 
					final IModel<ConfigTreeNode> rowModel) {
				
				final ConfigurationType configurationType = rowModel.getObject().getConfigurationType();
				cellItem.add(new Label(componentId, 
						Model.of(configurationType == null ? "" : configurationType.toString())));
			}
		});

		deviceColumns.add(new AbstractColumn<ConfigTreeNode>(Model.of("Connections")) {
			
			private static final long serialVersionUID = 1L;

			public void populateItem(Item<ICellPopulator<ConfigTreeNode>> cellItem, String componentId, 
					IModel<ConfigTreeNode> rowModel) {
				ConfigTreeNode configTreeNode = (ConfigTreeNode) rowModel.getObject();
				RepeatingView connectionsView = new RepeatingView(componentId);
				cellItem.add(connectionsView);
				try {
					if (configTreeNode.getNodeType().equals(ConfigTreeNode.TreeNodeType.APPLICATION_ENTITY)) {
						ApplicationEntity applicationEntity = 
								((ApplicationEntityModel) configTreeNode.getModel()).getApplicationEntity();
						if (applicationEntity != null)					
							for (Connection connection : applicationEntity.getConnections())
								connectionsView.add(new ConnectionPanel(connectionsView.newChildId(), 
										ImageManager.IMAGE_WIZARD_CONNECTION, 
										Model.of(connection.getCommonName() == null ? 
												connection.getHostname() + ":" + connection.getPort() :
													connection.getCommonName()), 
										Model.of(connection.toString()))
								);
					}
		        } catch (ConfigurationException ce) {
		        	log.error(this.getClass().toString() + ": " + "Error listing connections for application entity: " + ce.getMessage());
		        	log.debug("Exception", ce);
		        	throw new RuntimeException(ce);
		        }
			}
		});

		if (connectedDeviceName != null)
			deviceColumns.add(new AbstractColumn<ConfigTreeNode>(Model.of("Send")) {
	
				private static final long serialVersionUID = 1L;
	
				public void populateItem(final Item<ICellPopulator<ConfigTreeNode>> cellItem, final String componentId, 
						final IModel<ConfigTreeNode> rowModel) {
					
					final TreeNodeType type = rowModel.getObject().getNodeType();
					if (type == null)
						throw new RuntimeException("Error: Unknown node type, cannot create edit modal window");
	
					if (!type.equals(ConfigTreeNode.TreeNodeType.DEVICE) || 
							!connectedDeviceName.equals(rowModel.getObject().getName())) {
						cellItem.add(new Label(componentId));
						return;
					}
						
					AjaxLink<Object> ajaxLink = 
							new AjaxLink<Object>("wickettree.link") { 
	
				            private static final long serialVersionUID = 1L;
	
				            @Override
				            public void onClick(AjaxRequestTarget target) {

				            	reloadMessage = new MessageWindow("reload-message", 
				                		new StringResourceModel("dicom.confirmReload", this, null)) {

				                    private static final long serialVersionUID = 1L;

				                    @Override
				                    public void onOk(AjaxRequestTarget target) {
				                        log.info("Reloading configuration for connected device");
				                    }
				                };
				                BasicConfigurationPanel.this.
				                	addOrReplace(reloadMessage.setInitialHeight(150)
				                		.setWindowClosedCallback(windowClosedCallback));
				                reloadMessage.show(target);
				            }
					};
					cellItem.add(new LinkPanel(componentId, ajaxLink, ImageManager.IMAGE_WIZARD_RELOAD, reloadMessage))
						.add(new AttributeAppender("style", Model.of("width: 50px; text-align: center;")));
				}
			});

		deviceColumns.add(new AbstractColumn<ConfigTreeNode>(Model.of("Echo")) {

			private static final long serialVersionUID = 1L;

			public void populateItem(final Item<ICellPopulator<ConfigTreeNode>> cellItem, final String componentId, 
					final IModel<ConfigTreeNode> rowModel) {
				
				final TreeNodeType type = rowModel.getObject().getNodeType();
				if (type == null)
					throw new RuntimeException("Error: Unknown node type, cannot create edit modal window");

				AjaxLink<Object> ajaxLink = 
						new AjaxLink<Object>("wickettree.link") { 

			            private static final long serialVersionUID = 1L;

			            @Override
			            public void onClick(AjaxRequestTarget target) {
			            	
			            	if (type.equals(ConfigTreeNode.TreeNodeType.APPLICATION_ENTITY)) {
								echoWindow
					                .setPageCreator(new ModalWindow.PageCreator() {
					                    
					                    private static final long serialVersionUID = 1L;
					                      
					                    @Override
					                    public Page createPage() {
					                    	try {
					                    	return new DicomEchoPage(echoWindow, 
					                    			((ApplicationEntityModel) rowModel.getObject().getModel()).getApplicationEntity());
					                    	} catch (Exception e) {
					        		        	log.error(this.getClass().toString() + ": " + "Error creating DicomEchoPage: " + e.getMessage());
					        		        	log.debug("Exception", e);
					        		        	throw new RuntimeException(e);
					                    	}
					                    }
					                });
			            	}
			            	echoWindow
			            		.setWindowClosedCallback(windowClosedCallback)
			            		.show(target);
			            }
				};
				if (type.equals(ConfigTreeNode.TreeNodeType.APPLICATION_ENTITY))
					cellItem.add(new LinkPanel(componentId, ajaxLink, ImageManager.IMAGE_WIZARD_ECHO, removeConfirmation))
						.add(new AttributeAppender("style", Model.of("width: 50px; text-align: center;")));
				else
					cellItem.add(new Label(componentId));
			}
		});
		
		deviceColumns.add(new AbstractColumn<ConfigTreeNode>(Model.of("Edit")) {
			
			private static final long serialVersionUID = 1L;

			public void populateItem(final Item<ICellPopulator<ConfigTreeNode>> cellItem, final String componentId, 
					final IModel<ConfigTreeNode> rowModel) {
				
				final TreeNodeType type = rowModel.getObject().getNodeType();
				if (type == null)
					throw new RuntimeException("Error: Unknown node type, cannot create edit modal window");

				AjaxLink<Object> ajaxLink = 
						new AjaxLink<Object>("wickettree.link") { 

			            private static final long serialVersionUID = 1L;

			            @Override
			            public void onClick(AjaxRequestTarget target) {
			            	
			            	if (type.equals(ConfigTreeNode.TreeNodeType.CONTAINER_CONNECTIONS)) {
								editWindow
					                .setPageCreator(new ModalWindow.PageCreator() {
					                    
					                    private static final long serialVersionUID = 1L;

					                    @Override
					                    public Page createPage() {
					                    	return new CreateOrEditConnectionPage(
					                    			editWindow, 
					                    			null, 
					                    			rowModel.getObject().getParent());
					                    }
					                });
			            	}
							else if (type.equals(ConfigTreeNode.TreeNodeType.CONTAINER_APPLICATION_ENTITIES)) {
								editWindow
					                .setPageCreator(new ModalWindow.PageCreator() {
					                    
					                    private static final long serialVersionUID = 1L;
					                      
					                    @Override
					                    public Page createPage() {
				                    		 return new CreateOrEditApplicationEntityPage(
				                    				 editWindow, 
				                    				 null,  
				                    				 rowModel.getObject().getParent());
					                    }
					                });
							} else if (type.equals(ConfigTreeNode.TreeNodeType.CONTAINER_TRANSFER_CAPABILITIES)) {
								editWindow
					                .setPageCreator(new ModalWindow.PageCreator() {
					                    
					                    private static final long serialVersionUID = 1L;
					                      
					                    @Override
					                    public Page createPage() {
					                    	return new CreateOrEditTransferCapabilityPage(
					                    			editWindow, 
					                    			null, 
					                    			rowModel.getObject().getParent()); 
					                    }
					                });
							} else if (type.equals(ConfigTreeNode.TreeNodeType.CONTAINER_FORWARD_RULES)) {
								editWindow
					                .setPageCreator(new ModalWindow.PageCreator() {
					                    
					                    private static final long serialVersionUID = 1L;
					                      
					                    @Override
					                    public Page createPage() {
					                    	return new CreateOrEditForwardRulePage(
					                    			editWindow, 
					                    			null, 
					                    			rowModel.getObject().getParent()); 
					                    }
					                });
							} else if (type.equals(ConfigTreeNode.TreeNodeType.CONTAINER_FORWARD_SCHEDULES)) {
								editWindow
					                .setPageCreator(new ModalWindow.PageCreator() {
					                    
					                    private static final long serialVersionUID = 1L;
					                      
					                    @Override
					                    public Page createPage() {
					                    	return new CreateOrEditForwardSchedulePage(
					                    			editWindow, 
					                    			null, 
					                    			rowModel.getObject().getParent()); 
					                    }
					                });
							} else if (type.equals(ConfigTreeNode.TreeNodeType.CONTAINER_RETRIES)) {
								editWindow
					                .setPageCreator(new ModalWindow.PageCreator() {
					                    
					                    private static final long serialVersionUID = 1L;
					                      
					                    @Override
					                    public Page createPage() {
					                    	return new CreateOrEditRetryPage(
					                    			editWindow, 
					                    			null, 
					                    			rowModel.getObject().getParent()); 
					                    }
					                });
							} else if (type.equals(ConfigTreeNode.TreeNodeType.CONTAINER_COERCIONS)) {
								editWindow
					                .setPageCreator(new ModalWindow.PageCreator() {
					                    
					                    private static final long serialVersionUID = 1L;
					                      
					                    @Override
					                    public Page createPage() {
					                    	return new CreateOrEditCoercionPage(
					                    			editWindow, 
					                    			null, 
					                    			rowModel.getObject().getParent()); 
					                    }
					                });
							} else {
								editWindow
					                .setPageCreator(new ModalWindow.PageCreator() {
					                    
					                    private static final long serialVersionUID = 1L;
					                      
					                    @Override
					                    public Page createPage() {
					        				if (type.equals(ConfigTreeNode.TreeNodeType.DEVICE)) {
					        					try {
						        					ConfigTreeProvider.get().loadDevice(rowModel.getObject());
						        	                return new CreateOrEditDevicePage(editWindow, 
						        	                		(DeviceModel) rowModel.getObject().getModel());
					        					} catch (Exception e) {
					        						log.error("Error loading device on edit", e);
					        						return null;
					        					}
					        				} else if (type.equals(ConfigTreeNode.TreeNodeType.CONNECTION)) {
					        					return new CreateOrEditConnectionPage(
					        							editWindow, 
					        							(ConnectionModel) rowModel.getObject().getModel(), 
					        							rowModel.getObject().getAncestor(2));
					        				} else if (type.equals(ConfigTreeNode.TreeNodeType.APPLICATION_ENTITY)) {
					        					return new CreateOrEditApplicationEntityPage(
					        							editWindow, 
					        							(ApplicationEntityModel) rowModel.getObject().getModel(), 
					        		            		rowModel.getObject().getAncestor(2)); 
					        				} else if (type.equals(ConfigTreeNode.TreeNodeType.TRANSFER_CAPABILITY)) {
					        		            return new CreateOrEditTransferCapabilityPage(editWindow, 
					        		            		(TransferCapabilityModel) rowModel.getObject().getModel(), 
					        		            		rowModel.getObject().getAncestor(3));
					        				} else if (type.equals(ConfigTreeNode.TreeNodeType.FORWARD_RULE)) {
					        		            return new CreateOrEditForwardRulePage(editWindow, 
					        		            		(ForwardRuleModel) rowModel.getObject().getModel(), 
					        		            		rowModel.getObject().getAncestor(2));
					        				} else if (type.equals(ConfigTreeNode.TreeNodeType.FORWARD_SCHEDULE)) {
					        		            return new CreateOrEditForwardSchedulePage(editWindow, 
					        		            		(ForwardScheduleModel) rowModel.getObject().getModel(), 
					        		            		rowModel.getObject().getAncestor(2));
					        				} else if (type.equals(ConfigTreeNode.TreeNodeType.RETRY)) {
					        		            return new CreateOrEditRetryPage(editWindow, 
					        		            		(RetryModel) rowModel.getObject().getModel(), 
					        		            		rowModel.getObject().getAncestor(2));
					        				} else if (type.equals(ConfigTreeNode.TreeNodeType.COERCION)) {
					        		            return new CreateOrEditCoercionPage(editWindow, 
					        		            		(CoercionModel) rowModel.getObject().getModel(), 
					        		            		rowModel.getObject().getAncestor(2));
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
				ajaxLink.setVisible(!type.equals(ConfigTreeNode.TreeNodeType.CONTAINER_APPLICATION_ENTITIES)
						|| rowModel.getObject().getParent().getChildren().get(0).hasChildren());
				try {
					if (type.equals(ConfigTreeNode.TreeNodeType.CONTAINER_FORWARD_SCHEDULES))
						ajaxLink.setVisible(ConfigTreeProvider.get().getUniqueAETitles().length > 0);
				} catch (ConfigurationException ce) {
					log.error("Error listing Registered AE Titles", ce);
				}

				ResourceReference image;
				if (type.equals(ConfigTreeNode.TreeNodeType.CONTAINER_CONNECTIONS)
						|| type.equals(ConfigTreeNode.TreeNodeType.CONTAINER_APPLICATION_ENTITIES)
						|| type.equals(ConfigTreeNode.TreeNodeType.CONTAINER_TRANSFER_CAPABILITIES)
						|| type.equals(ConfigTreeNode.TreeNodeType.CONTAINER_FORWARD_RULES)
						|| type.equals(ConfigTreeNode.TreeNodeType.CONTAINER_FORWARD_SCHEDULES)
						|| type.equals(ConfigTreeNode.TreeNodeType.CONTAINER_RETRIES)
						|| type.equals(ConfigTreeNode.TreeNodeType.CONTAINER_COERCIONS))
					image = ImageManager.IMAGE_WIZARD_COMMON_ADD;
				else
					image = ImageManager.IMAGE_WIZARD_COMMON_EDIT;

				if (type.equals(ConfigTreeNode.TreeNodeType.CONTAINER_TRANSFER_CAPABILITY_TYPE))
					cellItem.add(new Label(componentId));
				else
					cellItem.add(new LinkPanel(componentId, ajaxLink, image, removeConfirmation))
						.add(new AttributeAppender("style", Model.of("width: 50px; text-align: center;")));
			}
		});
		
		deviceColumns.add(new AbstractColumn<ConfigTreeNode>(Model.of("Profile")) {
			
			private static final long serialVersionUID = 1L;

			public void populateItem(Item<ICellPopulator<ConfigTreeNode>> cellItem, String componentId, 
					final IModel<ConfigTreeNode> rowModel) {

				final TreeNodeType type = rowModel.getObject().getNodeType();
				if (type == null)
					throw new RuntimeException("Error: Unknown node type, cannot create profile modal window");
				else if (!type.equals(ConfigTreeNode.TreeNodeType.CONTAINER_TRANSFER_CAPABILITIES)) {
					cellItem.add(new Label(componentId));
					return;
				}

				AjaxLink<Object> ajaxLink = 
						new AjaxLink<Object>("wickettree.link") { 

				            private static final long serialVersionUID = 1L;

				            @Override
				            public void onClick(AjaxRequestTarget target) {
								editWindow
				                .setPageCreator(new ModalWindow.PageCreator() {
				                    
				                    private static final long serialVersionUID = 1L;
				                      
				                    @Override
				                    public Page createPage() {
				                    	return new ApplyTransferCapabilityProfilePage(
				                    			editWindow, 
				                    			null, 
				                    			rowModel.getObject().getParent()); 
				                    }
				                });
				            	editWindow
			            		.setWindowClosedCallback(windowClosedCallback)
			            		.show(target);
				            }
				        };
				        cellItem.add(new LinkPanel(componentId, ajaxLink, ImageManager.IMAGE_WIZARD_COMMON_PROFILE,removeConfirmation))
							.add(new AttributeAppender("style", Model.of("width: 50px; text-align: center;")));
			}
		});

		deviceColumns.add(new AbstractColumn<ConfigTreeNode>(Model.of("Delete")) {
			
			private static final long serialVersionUID = 1L;

			public void populateItem(Item<ICellPopulator<ConfigTreeNode>> cellItem, String componentId, 
					final IModel<ConfigTreeNode> rowModel) {

				final TreeNodeType type = rowModel.getObject().getNodeType();
				if (type == null)
					throw new RuntimeException("Error: Unknown node type, cannot create delete modal window");
				else if (type.equals(ConfigTreeNode.TreeNodeType.CONTAINER_CONNECTIONS)
						|| type.equals(ConfigTreeNode.TreeNodeType.CONTAINER_APPLICATION_ENTITIES)
						|| type.equals(ConfigTreeNode.TreeNodeType.CONTAINER_TRANSFER_CAPABILITIES)
						|| type.equals(ConfigTreeNode.TreeNodeType.CONTAINER_TRANSFER_CAPABILITY_TYPE)
						|| type.equals(ConfigTreeNode.TreeNodeType.CONTAINER_FORWARD_RULES)
						|| type.equals(ConfigTreeNode.TreeNodeType.CONTAINER_FORWARD_SCHEDULES)
						|| type.equals(ConfigTreeNode.TreeNodeType.CONTAINER_RETRIES)
						|| type.equals(ConfigTreeNode.TreeNodeType.CONTAINER_COERCIONS)) {
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
				                			new StringResourceModel("dicom.confirmDelete", 
				                					this, null, 
				                					new Object[] {rowModel.getObject().getNodeType(), rowModel.getObject().getName()}), 
				                					rowModel.getObject());
				            }
				        };
				        cellItem.add(new LinkPanel(componentId, ajaxLink, ImageManager.IMAGE_WIZARD_COMMON_REMOVE, removeConfirmation))
							.add(new AttributeAppender("style", Model.of("width: 50px; text-align: center;")));
				        if (type.equals(ConfigTreeNode.TreeNodeType.CONNECTION)) {
				        	Connection connection = null;
							try {
								connection = ((ConnectionModel) rowModel.getObject().getModel()).getConnection();
					        	for (ApplicationEntity ae : connection.getDevice().getApplicationEntities())
					        		if (ae.getConnections().contains(connection))
					        			ajaxLink.setEnabled(false)
					        				.add(new AttributeModifier("title", new ResourceModel("dicom.delete.connection.notAllowed")));
							} catch (ConfigurationException ce) {
					        	log.error(this.getClass().toString() + ": " + "Error checking used connections of application entities: " + ce.getMessage());
					        	log.debug("Exception", ce);
					        	throw new RuntimeException(ce);
							}
				        }
			}
		});
    }
    
    public void renderTree() throws ConfigurationException {
		IModel<Set<ConfigTreeNode>> currentState = configTree.getModel();
		configTree = new ConfigTableTree("configTree", deviceColumns,
				ConfigTreeProvider.get(), 
				Integer.MAX_VALUE);
		configTree.setModel(currentState);
		form.addOrReplace(configTree);
	}

    public static String getModuleName() {
        return MODULE_NAME;
    }
}
