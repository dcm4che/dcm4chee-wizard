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
package org.dcm4chee.wizard.validator;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.StringValidator;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class ScheduleValidator extends StringValidator {

    private static final long serialVersionUID = 1L;

    private final String days = "(Sun|Mon|Tue|Wed|Thu|Fri|Sat)";
    private final String hours = "(0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23)";

    public enum Type {
        DAYS, HOURS
    }

    private Type type;

    public ScheduleValidator(ScheduleValidator.Type type) {
        this.type = type;
    }

    @Override
    public void validate(IValidatable<String> v) {
        String testType = null;
        if (type.equals(Type.DAYS))
            testType = days;
        else if (type.equals(Type.HOURS))
            testType = hours;
        String regex = "^(" + testType + "{1,1}(\\-" + testType + "){0,1})(,(" + testType + "{1,1}(\\-" + testType
                + "){0,1}))*$";
        if (!v.getValue().matches(regex)) {
            if (type.equals(Type.DAYS))
                v.error(new ValidationError().addKey("DaysScheduleValidator.invalid"));
            if (type.equals(Type.HOURS))
                v.error(new ValidationError().addKey("HoursScheduleValidator.invalid"));
        }
    }
}
