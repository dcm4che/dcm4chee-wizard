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
 * Java(TM), hosted at https://github.com/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2012
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
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

package org.dcm4chee.wizard.icons;

import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * @author Robert David <robert.david@agfa.com>
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 */
public class ImageManager {

    private static final String schemeDependentPrefix = "GREEN_";

    public static int defaultWidth = 16;
    public static int defaultHeight = 16;

    public static final ResourceReference IMAGE_WIZARD_COMMON_EXPAND = new PackageResourceReference(ImageManager.class,
            "common/" + schemeDependentPrefix + "expand.png");
    public static final ResourceReference IMAGE_WIZARD_COMMON_SEARCH = new PackageResourceReference(ImageManager.class,
            "common/search.png");
    public static final ResourceReference IMAGE_WIZARD_COMMON_ADD = new PackageResourceReference(ImageManager.class,
            "common/add.png");
    public static final ResourceReference IMAGE_WIZARD_COMMON_EDIT = new PackageResourceReference(ImageManager.class,
            "common/edit.png");
    public static final ResourceReference IMAGE_WIZARD_COMMON_PROFILE = new PackageResourceReference(
            ImageManager.class, "common/profile.png");
    public static final ResourceReference IMAGE_WIZARD_COMMON_REMOVE = new PackageResourceReference(ImageManager.class,
            "common/remove.png");

    public static final ResourceReference IMAGE_WIZARD_DEVICE = new PackageResourceReference(ImageManager.class,
            "dicom/device.png");
    public static final ResourceReference IMAGE_WIZARD_DEVICE_ADD = new PackageResourceReference(ImageManager.class,
            "dicom/device_add.png");
    public static final ResourceReference IMAGE_WIZARD_EXPORT = new PackageResourceReference(ImageManager.class,
            "dicom/export.png");
    public static final ResourceReference IMAGE_WIZARD_CONNECTION = new PackageResourceReference(ImageManager.class,
            "dicom/connection.png");
    public static final ResourceReference IMAGE_WIZARD_TRANSFER_CAPABILITY = new PackageResourceReference(
            ImageManager.class, "dicom/transfer_capability.png");
    public static final ResourceReference IMAGE_WIZARD_APPLICATION_ENTITY = new PackageResourceReference(
            ImageManager.class, "dicom/application_entity.png");
    public static final ResourceReference IMAGE_WIZARD_AUDIT_LOGGER = new PackageResourceReference(ImageManager.class,
            "dicom/audit_logger.png");
    public static final ResourceReference IMAGE_WIZARD_FOLDER_CONNECTIONS = new PackageResourceReference(
            ImageManager.class, "dicom/folder_connections.png");
    public static final ResourceReference IMAGE_WIZARD_FOLDER_APPLICATION_ENTITIES = new PackageResourceReference(
            ImageManager.class, "dicom/folder_application_entities.png");
    public static final ResourceReference IMAGE_WIZARD_FOLDER_AUDIT_LOGGERS = new PackageResourceReference(
            ImageManager.class, "dicom/folder_audit_loggers.png");
    public static final ResourceReference IMAGE_WIZARD_FOLDER_TRANSFER_CAPABILITIES = new PackageResourceReference(
            ImageManager.class, "dicom/folder_transfer_capabilities.png");
    public static final ResourceReference IMAGE_WIZARD_FOLDER_TRANSFER_CAPABILITY_TYPE = new PackageResourceReference(
            ImageManager.class, "dicom/folder_transfer_capability_type.png");

    public static final ResourceReference IMAGE_WIZARD_FOLDER_HL7_APPLICATIONS = new PackageResourceReference(
            ImageManager.class, "proxy/folder_hl7_applications.png");
    public static final ResourceReference IMAGE_WIZARD_HL7_APPLICATION = new PackageResourceReference(
            ImageManager.class, "proxy/hl7_application.png");
    public static final ResourceReference IMAGE_WIZARD_FOLDER_PROXY_RULES = new PackageResourceReference(
            ImageManager.class, "proxy/folder_proxy_rules.png");
    public static final ResourceReference IMAGE_WIZARD_FORWARD_RULE = new PackageResourceReference(ImageManager.class,
            "proxy/forward_rule.png");
    public static final ResourceReference IMAGE_WIZARD_FORWARD_SCHEDULE = new PackageResourceReference(
            ImageManager.class, "proxy/forward_schedule.png");
    public static final ResourceReference IMAGE_WIZARD_RETRY = new PackageResourceReference(ImageManager.class,
            "proxy/retry.png");
    public static final ResourceReference IMAGE_WIZARD_COERCION = new PackageResourceReference(ImageManager.class,
            "proxy/coercion.png");

    public static final ResourceReference IMAGE_WIZARD_ECHO = new PackageResourceReference(ImageManager.class,
            "functions/echo.png");
    public static final ResourceReference IMAGE_WIZARD_RUNNING = new PackageResourceReference(ImageManager.class,
            "functions/running.png");
    public static final ResourceReference IMAGE_WIZARD_NOT_RUNNING = new PackageResourceReference(ImageManager.class,
            "functions/not_running.png");
    public static final ResourceReference IMAGE_WIZARD_RUNNING_DEACTIVATED = new PackageResourceReference(ImageManager.class,
            "functions/running_deactivated.png");
    public static final ResourceReference IMAGE_WIZARD_RELOAD = new PackageResourceReference(ImageManager.class,
            "functions/reload.png");
    public static final ResourceReference IMAGE_WIZARD_RELOAD_WARNING = new PackageResourceReference(
            ImageManager.class, "functions/reload_warning.png");
}
