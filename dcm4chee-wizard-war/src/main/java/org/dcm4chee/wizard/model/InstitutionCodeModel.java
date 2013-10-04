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

package org.dcm4chee.wizard.model;

import java.io.Serializable;

import org.apache.wicket.model.IModel;
import org.dcm4che.data.Code;

/**
 * @author Robert David <robert.david@agfa.com>
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 */
public class InstitutionCodeModel implements Serializable {

    private static final long serialVersionUID = 1L;

    String[] fields = new String[4];

    public InstitutionCodeModel(Code code) {
        if (code == null)
            return;

        fields[0] = code.getCodeValue();
        fields[1] = code.getCodingSchemeDesignator();
        fields[2] = code.getCodingSchemeVersion();
        fields[3] = code.getCodeMeaning();
    }

    public Code getCode() {
        return (fields[0] != null && fields[1] != null && fields[3] != null) 
                ? new Code(fields[0], fields[1],fields[2], fields[3]) 
                : null;
    }

    public CodeFieldModel getCodeFieldModel(int idx) {
        return new CodeFieldModel(this, idx);
    }

    public class CodeFieldModel implements IModel<String> {

        private static final long serialVersionUID = 1L;

        InstitutionCodeModel model;
        int index;

        public CodeFieldModel(InstitutionCodeModel model, int index) {
            this.model = model;
            this.index = index;
        }

        public String getObject() {
            return model.fields[index];
        }

        public void setObject(String s) {
            model.fields[index] = s;
        }

        public void detach() {
        }
    }
}
