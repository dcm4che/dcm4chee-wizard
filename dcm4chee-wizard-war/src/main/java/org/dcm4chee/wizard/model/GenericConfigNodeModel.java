package org.dcm4chee.wizard.model;

import java.security.cert.X509Certificate;
import java.util.Map;

import org.apache.wicket.model.IModel;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.api.generic.ReflectiveConfig;
import org.dcm4che3.conf.api.generic.adapters.ReflectiveAdapter;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DeviceExtension;
import org.dcm4che3.net.DeviceInfo;
import org.dcm4chee.xds2.conf.XdsRepository;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.fasterxml.jackson.databind.util.NameTransformer;

public class GenericConfigNodeModel<T extends DeviceExtension> implements IModel<String> {

    private static final long serialVersionUID = -8533244684805795443L;
    private Map<String,Object> node;
    ObjectMapper om;
    ReflectiveConfig rconfig;
    ReflectiveAdapter<T> ad;

    T confObj;
    T retConfObj;
    
    String fieldName;

    @SuppressWarnings("unchecked")
    public GenericConfigNodeModel(T confObj, String fieldName) {
        super();

        this.confObj = confObj;
        this.fieldName = fieldName;

        om = new ObjectMapper();
        rconfig = new ReflectiveConfig(null, new DicomConfiguration() {
            
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
        ad = new ReflectiveAdapter<T>((Class<T>) confObj.getClass());

    }

    @Override
    public void detach() {
    }

    @Override
    public String getObject() {

        try {
            // serialize to confignode
            Map<String,Object> cn = ad.serialize(confObj, rconfig, null);

            // serialize to pretty json
            //return om.writeValueAsString(cn.get(fieldName));
            return om.writerWithDefaultPrettyPrinter().writeValueAsString(cn.get(fieldName));

        } catch (Exception e) {
            throw new RuntimeException("Cannot serialize config for " + confObj.getClass().getSimpleName(), e);
        }
    }

    @Override
    public void setObject(String arg0) {
        try {

            // deserialize json
            Map<String,Object> propNode = om.readValue(arg0, Map.class);

            // serialize full obj
            Map<String,Object> confNode = ad.serialize(confObj, rconfig, null);

            // set field
            confNode.put(fieldName, propNode);

            // deserialize full obj
            retConfObj = ad.deserialize(confNode, rconfig, null);
            
        } catch (Exception e) {
            throw new RuntimeException("Cannot deserialize config for " + confObj.getClass().getSimpleName(), e);
        }

    }
    
    public T getModifiedConfigObj() {
        return retConfObj;
    }

}
