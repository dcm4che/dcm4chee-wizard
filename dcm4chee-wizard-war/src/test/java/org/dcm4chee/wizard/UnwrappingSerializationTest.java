package org.dcm4chee.wizard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dcm4che3.net.Device;
import org.dcm4chee.wizard.model.GenericConfigNodeModel;
import org.dcm4chee.xds2.conf.XCAInitiatingGWCfg;
import org.dcm4chee.xds2.conf.XdsBrowser;
import org.dcm4chee.xds2.conf.XdsRegistry;
import org.dcm4chee.xds2.conf.XdsRepository;
import org.dcm4chee.xds2.conf.XCAInitiatingGWCfg.GatewayReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

/*@RunWith(BlockJUnit4ClassRunner.class)*/
public class UnwrappingSerializationTest {

    private static final String[] MIME_TYPES2 = new String[] { "application/xml", "application/dicom", "application/pdf", "text/plain",
            "text/xml" };
    private String[] AFFINITY_DOMAIN = { "1.2.3.4.5" };

    private XdsRepository createRepo() throws Exception {
        // create registry which will be referenced
        Device regd = new Device("RegDevice");

        Device srcd = new Device("source_device");

        XdsRepository rep = new XdsRepository();

        rep.setApplicationName("AppNNName");
        rep.setRepositoryUID("1.2.3");
        rep.setAcceptedMimeTypes(MIME_TYPES2);
        rep.setSoapLogDir(null);
        rep.setCheckMimetype(true);
        rep.setAllowedCipherHostname("*");
        rep.setLogFullMessageHosts(new String[] {});
        rep.setRetrieveUrl("http://retrieve");
        rep.setProvideUrl("http://provide");
        rep.setForceMTOM(true);

        // reference registry
        Map<String, Device> deviceBySrcUid = new HashMap<String, Device>();

        deviceBySrcUid.put("3.4.5", srcd);
        rep.setSrcDevicebySrcIdMap(deviceBySrcUid);

        XdsRepository repo = rep;
        return repo;
    }

    
    private XCAInitiatingGWCfg createInitGw(){
        XCAInitiatingGWCfg initGW = new XCAInitiatingGWCfg();

        initGW.setApplicationName("appn");
        initGW.setHomeCommunityID("1.2.3");
        initGW.setSoapLogDir("/dir");
        initGW.setLocalPIXConsumerApplication("pixapp");
        initGW.setRemotePIXManagerApplication("pixmgrapp");
        initGW.setAsync(false);
        initGW.setAsyncHandler(true);

        // registry
        Device regd = new Device("registry_device_rgw");
        initGW.setRegistry(regd);

        // repos

        Device repod1 = new Device("repo_device_1");
        Device repod2 = new Device("repo_device_2");

        Map<String, Device> repDevices = new HashMap<String, Device>();
        repDevices.put("123", repod1);
        repDevices.put("456", repod2);

        initGW.setRepositoryDeviceByUidMap(repDevices);


        Device rgwd2 = new Device("rgw_device_2");

        GatewayReference gwr1 = new GatewayReference();
        gwr1.setAffinityDomain("1.2.3.4.5");
        gwr1.setRespondingGWdevice(repod1);

        GatewayReference gwr2 = new GatewayReference();
        gwr2.setAffinityDomain("10.20.30.40.50");
        gwr2.setRespondingGWdevice(rgwd2);

        Map<String, GatewayReference> gws = new HashMap<String, GatewayReference>();
        gws.put("1001", gwr1);
        gws.put("2002", gwr2);

        initGW.setRespondingGWByHomeCommunityIdMap(gws);
        
        return initGW;
    }
    
   
    private XdsRegistry createRegistry() {
        XdsRegistry registry = new XdsRegistry();
        registry.setApplicationName("appReg");
        registry.setAffinityDomain(AFFINITY_DOMAIN);
        registry.setAffinityDomainConfigDir("${jboss.server.config.dir}/affinitydomain");
        registry.setAcceptedMimeTypes(MIME_TYPES2);
        registry.setSoapLogDir(null);
        registry.setCreateMissingPIDs(false);
        registry.setCreateMissingCodes(false);
        registry.setCheckAffinityDomain(true);
        registry.setCheckMimetype(true);
        registry.setPreMetadataCheck(false);
        registry.setRegisterUrl("http://localhost/registryregister");
        registry.setQueryUrl("http://localhost/registryquery");
        
        XdsBrowser xdsBrowser = new XdsBrowser();
        
        
        
        Set<Device> devs = new HashSet<Device>();
        devs.add(new Device("abcDev"));
        devs.add(new Device("another!Dev"));
        devs.add(new Device("AlsoControlledDev"));
        xdsBrowser.setControlledDevices(devs);
        
        registry.setXdsBrowser(xdsBrowser);
        return registry;
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testSerializeUnwrap() throws Exception {

        XCAInitiatingGWCfg initgw = createInitGw();
        GenericConfigNodeModel<XCAInitiatingGWCfg> nm = new GenericConfigNodeModel<XCAInitiatingGWCfg>(initgw, "xdsRespondingGateways", Map.class);
        System.out.println(nm.getObject());
        nm.setObject("{\"1301\":{\"xdsAffinityDomain\":\"1.2.5\",\"xdsRespondingGateway\":\"repo_dDDD\"},\"2002\":{\"xdsAffinityDomain\":\"10.20.30.40.50\",\"xdsRespondingGateway\":\"rgw_device_2\"}}");
        System.out.println(nm.getModifiedConfigObj().getRespondingGWByHomeCommunityIdMap());

        
        XdsRegistry reg = createRegistry();
        GenericConfigNodeModel<XdsRegistry> nm1 = new GenericConfigNodeModel<XdsRegistry>(reg, "xdsBrowser", Map.class);
        System.out.println(nm1.getObject());
        nm1.setObject("{\"xdsControlledDevices\" : [ \"BOODev\", \"TheThirdanotherDev\", \"AlsoControlledDev\" ]}");
        System.out.println(nm1.getModifiedConfigObj().getXdsBrowser().getControlledDevices());
        

    }

}
