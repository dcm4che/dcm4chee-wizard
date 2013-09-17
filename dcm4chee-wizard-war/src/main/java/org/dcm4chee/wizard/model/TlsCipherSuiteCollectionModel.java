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
import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.model.IModel;
import org.dcm4che.net.Connection;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class TlsCipherSuiteCollectionModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private String[] tlsCipherSuites;

    public TlsCipherSuiteCollectionModel(Connection connection, int size) {
        tlsCipherSuites = new String[size];
        if (connection != null)
            try {
                for (int i = 0; i < connection.getTlsCipherSuites().length; i++) {
                    tlsCipherSuites[i] = connection.getTlsCipherSuites()[i];
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Connection contains too many tls cipher suites, only "
                        + tlsCipherSuites.length + " are supported");
            }
    }

    public TlsCipherSuiteModel getTlsCipherSuiteModel(int index) {
        if (index < tlsCipherSuites.length)
            return new TlsCipherSuiteModel(this, index);
        else
            throw new IllegalArgumentException("Wrong index, must be less than " + tlsCipherSuites.length);
    }

    public class TlsCipherSuiteModel implements IModel<String> {

        private static final long serialVersionUID = 1L;

        TlsCipherSuiteCollectionModel model;
        int index;

        public TlsCipherSuiteModel(TlsCipherSuiteCollectionModel model, int index) {
            this.model = model;
            this.index = index;
        }

        public String getObject() {
            return index < tlsCipherSuites.length ? tlsCipherSuites[index] : null;
        }

        public void setObject(String tlsCipherSuite) {
            model.tlsCipherSuites[index] = tlsCipherSuite;
        }

        public void detach() {
        }
    }

    public Set<String> getTlsCipherSuites() {
        Set<String> result = new HashSet<String>();
        for (String tlsCipherSuite : tlsCipherSuites)
            if (tlsCipherSuite != null)
                result.add(tlsCipherSuite);
        return result;
    }
}
