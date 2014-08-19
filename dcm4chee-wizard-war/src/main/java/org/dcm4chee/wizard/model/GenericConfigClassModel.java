package org.dcm4chee.wizard.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.api.generic.ReflectiveConfig;
import org.dcm4che3.conf.api.generic.adapters.ReflectiveAdapter;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DeviceExtension;
import org.dcm4che3.net.DeviceInfo;
import org.dcm4chee.wizard.tree.ConfigTreeProvider;

import java.security.cert.X509Certificate;
import java.util.Map;

public class GenericConfigClassModel<T extends DeviceExtension> implements IModel<String>, IValidator<String> {

    private static final long serialVersionUID = -8533244684805795443L;
    private Map<String, Object> node;
    ObjectMapper om;

    T confObj;
    T retConfObj;


    /**
     * Not using a field because dont want to make it serializable
     *
     * @return
     */
    private ReflectiveConfig getReflectiveConfig() {
        try {
            return new ReflectiveConfig(null, ConfigTreeProvider.getDicomConfigurationManager().getDicomConfiguration());

        } catch (WicketRuntimeException e) {
            // dummy for tests
            return new ReflectiveConfig(null, new DicomConfiguration() {

                @Override
                public void unregisterAETitle(String aet) throws ConfigurationException {
                    // TODO Auto-generated method stub

                }

                @Override
                public void sync() throws ConfigurationException {
                    // TODO Auto-generated method stub

                }

                @Override
                public void removeDevice(String name) throws ConfigurationException {
                    // TODO Auto-generated method stub

                }

                @Override
                public void removeCertificates(String ref) throws ConfigurationException {
                    // TODO Auto-generated method stub

                }

                @Override
                public boolean registerAETitle(String aet) throws ConfigurationException {
                    // TODO Auto-generated method stub
                    return false;
                }

                @Override
                public boolean purgeConfiguration() throws ConfigurationException {
                    // TODO Auto-generated method stub
                    return false;
                }

                @Override
                public void persistCertificates(String ref, X509Certificate... certs) throws ConfigurationException {
                    // TODO Auto-generated method stub

                }

                @Override
                public void persist(Device device) throws ConfigurationException {
                    // TODO Auto-generated method stub

                }

                @Override
                public void merge(Device device) throws ConfigurationException {
                    // TODO Auto-generated method stub

                }

                @Override
                public String[] listRegisteredAETitles() throws ConfigurationException {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public String[] listDeviceNames() throws ConfigurationException {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public DeviceInfo[] listDeviceInfos(DeviceInfo keys) throws ConfigurationException {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public <T> T getDicomConfigurationExtension(Class<T> clazz) {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public Device findDevice(String name) throws ConfigurationException {
                    return new Device(name);
                }

                @Override
                public X509Certificate[] findCertificates(String dn) throws ConfigurationException {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public ApplicationEntity findApplicationEntity(String aet) throws ConfigurationException {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public String deviceRef(String name) {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public boolean configurationExists() throws ConfigurationException {
                    // TODO Auto-generated method stub
                    return false;
                }

                @Override
                public void close() {
                    // TODO Auto-generated method stub

                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    public GenericConfigClassModel(T confObj) {
        super();

        this.confObj = confObj;

        om = new ObjectMapper();
    }

    @Override
    public void detach() {
    }

    @Override
    public String getObject() {

        ReflectiveAdapter ad = new ReflectiveAdapter<T>((Class<T>) confObj.getClass());

        try {
            // serialize to confignode
            Map<String, Object> cn = ad.serialize(confObj, getReflectiveConfig(), null);

            // serialize to pretty json
            return om.writerWithDefaultPrettyPrinter().writeValueAsString(cn);

        } catch (Exception e) {
            throw new RuntimeException("Cannot serialize config for " + confObj.getClass().getSimpleName(), e);
        }
    }

    private T deserialize(String arg0) {

        try {
            @SuppressWarnings("unchecked")
            ReflectiveAdapter<T> ad = new ReflectiveAdapter<T>((Class<T>) confObj.getClass());

            // read json
            Map<String, Object> serialized = om.readValue(arg0, Map.class);

            // deserialize full obj
            return ad.deserialize(serialized, getReflectiveConfig(), null);

        } catch (Exception e) {
            throw new RuntimeException("Cannot deserialize config for " + confObj.getClass().getSimpleName(), e);
        }

    }

    @Override
    public void setObject(String arg0) {
        retConfObj = deserialize(arg0);
    }

    public T getModifiedConfigObj() {
        return retConfObj;
    }

    private String getNiceErrorMessage(Throwable e) {
        String s = "";
        boolean first = true;
        while (e != null) {
            if (first)
                first = false;
            else
                s += ". Cause: \n";

            s += e.getMessage();
            e = e.getCause();
        }
        return s;
    }

    @Override
    public void validate(IValidatable<String> validatable) {
        try {
            deserialize(validatable.getValue());
        } catch (RuntimeException e) {
            ValidationError error = new ValidationError();
            error.setMessage(getNiceErrorMessage(e));
            validatable.error(error);
        }

    }

}
