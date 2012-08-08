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

package org.dcm4chee.wizard.war.common.model;

import java.io.Serializable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 */
public abstract class AbstractDicomModel implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public static final int NO_LEVEL = -1;
    public static final int PATIENT_LEVEL = 0;
    public static final int STUDY_LEVEL = 1;
    public static final int PPS_LEVEL = 2;
    public static final int SERIES_LEVEL = 3;
    public static final int INSTANCE_LEVEL = 4;
    
    public static final String[] LEVEL_STRINGS = {"Patient", "Study", "PPS", "Series", "Instance"};
    
    private long pk;
    private boolean selected;
    private boolean details;
//    protected DicomObject dataset;
    private AbstractDicomModel parent;
    
    protected static Logger log = LoggerFactory.getLogger(AbstractDicomModel.class);
            
    public long getPk() {
        return pk;
    }

    public void setPk(long pk) {
        this.pk = pk;
    }
    
//    public DicomObject getDataset() {
//        return dataset;
//    }

    public AbstractDicomModel getParent() {
        return parent;
    }

    public void setParent(AbstractDicomModel parent) {
        this.parent = parent;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isDetails() {
        return details;
    }

    public void setDetails(boolean details) {
        this.details = details;
    }
    
//    public String getAttributeValueAsString(int tag) {
//        return dataset.getString(tag);
//    }

    public abstract int getRowspan();
    
    public abstract void expand();
    
    public abstract void collapse();

    public abstract boolean isCollapsed();
    
    public abstract List<? extends AbstractDicomModel> getDicomModelsOfNextLevel();
    
    public abstract int levelOfModel();
    
//    protected String getCodeString(DicomElement codeSq) {
//        if (codeSq == null || codeSq.isEmpty())
//            return null;
//        StringBuilder sb = new StringBuilder();
//        DicomObject item = codeSq.getDicomObject();
//        sb.append(item.getString(Tag.CodeMeaning))
//            .append('[').append(item.getString(Tag.CodeValue)).append(']');
//        return sb.toString();
//    }
//    
//    public Date toDate(int tag) {
//        try {
//            return dataset.getDate(tag);
//        } catch (Exception x) {
//            log.warn("DicomObject contains wrong value in date attribute!:"+dataset);
//            return null;
//        }
//    }
//
//    public Date toDate(int dateTag, int timeTag) {
//        try {
//            return dataset.getDate(dateTag, timeTag);
//        } catch (Exception x) {
//            log.warn("DicomObject contains wrong value in date attribute!:"+dataset);
//            return null;
//        }
//    }
    
    public String replace(String s, char oldChar, char newChar) {
        return s == null ? s : s.replace(oldChar, newChar);
    }
    
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o.getClass() != getClass()) {
            return false;
        }
        return ((AbstractDicomModel) o).getPk() == pk;
    }
    
    public int hashCode() {
        int hashCode = 1;
        hashCode = 31 * hashCode + (int) (+pk ^ (pk >>> 32));
        return hashCode;
    }
}
