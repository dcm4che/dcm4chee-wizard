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

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.StringValidator;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4chee.wizard.tree.ConfigTreeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @author Robert David <robert.david@agfa.com>
 */
public class AETitleValidator extends StringValidator {

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(AETitleValidator.class);

    private static final int MAX_AET_LEN = 16;

    String ignore;

    public AETitleValidator(String ignore) {
        this.ignore = ignore;
    }

    /**
     * Check AET constrains (1-16 chars, ISO 646/G0 exclude 5C(backslash)).
     */
    @Override
    public void validate(IValidatable<String> v) {
        String s = v.getValue();
        if (s.equals("*"))
            return;

        if (s.length() > MAX_AET_LEN || s.trim().length() < 1) {
            v.error(new ValidationError().addKey("StringValidator.range").setVariables(getLengthVarMap(v, MAX_AET_LEN)));
        }
        if (!validateAEChars(s)) {
            v.error(new ValidationError().addKey("PatternValidator").setVariables(getPatternVarMap(v)));
        }

        if (!s.equals(ignore))
            try {
                for (String aeTitle : ConfigTreeProvider.get().getUniqueAETitles())
                    if (s.equals(aeTitle))
                        v.error(new ValidationError().addKey("AETitleValidator.alreadyExists"));
            } catch (ConfigurationException e) {
                v.error(new ValidationError().addKey("AETitleValidator"));
                log.error("Error validating AE Title", e);
            }
    }

    /*
     * AE valid characters: DICOM DEFAULT CHARACTER REPERTOIRE ENCODING without
     * backslash(5C), LF, FF, CR and ESC (i.e. ISO 646 G0 excluding '\')
     */
    private boolean validateAEChars(String s) {
        for (char c : s.toCharArray()) {
            if (c < 0x20 || c > 0x7e || c == 0x5c) {
                return false;
            }
        }
        return true;
    }

    private Map<String, Object> getLengthVarMap(IValidatable<String> validatable, int max) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("maximum", new Integer(max));
        map.put("minimum", new Integer(1));
        map.put("length", new Integer(((String) validatable.getValue()).length()));
        return map;
    }

    private Map<String, Object> getPatternVarMap(IValidatable<String> validatable) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("pattern", "DICOM DEFAULT CHARACTER REPERTOIRE exclude '\\', LF, FF, CR and ESC");
        return map;
    }
}
