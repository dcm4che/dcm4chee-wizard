package org.dcm4chee.wizard.model;

import org.apache.wicket.model.IModel;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.generic.ReflectiveConfig;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.ConfigNode;
import org.dcm4che3.conf.api.generic.adapters.ReflectiveAdapter;
import org.dcm4che3.net.DeviceExtension;
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
    private ConfigNode node;
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
        rconfig = new ReflectiveConfig(null, null);
        ad = new ReflectiveAdapter<T>((Class<T>) confObj.getClass());

    }

    @Override
    public void detach() {
    }

    @Override
    public String getObject() {

        try {
            // serialize to confignode
            ConfigNode cn = ad.serialize(confObj, rconfig, null);

            // serialize to pretty json
            return om.writerWithDefaultPrettyPrinter().writeValueAsString(cn.get(fieldName));

        } catch (Exception e) {
            throw new RuntimeException("Cannot serialize config for " + confObj.getClass().getSimpleName(), e);
        }
    }

    @Override
    public void setObject(String arg0) {
        try {

            // deserialize json
            ConfigNode propNode = om.readValue(arg0, ConfigNode.class);

            // serialize full obj
            ConfigNode confNode = ad.serialize(confObj, rconfig, null);

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
