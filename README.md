Introduction
============
The dcm4chee-wizard is a web-application that provides a graphical user interface for
configuration of DICOM devices compliant to the *DICOM Application Configuration Management Profile*,
specified in [DICOM 2011, Part 15][1], Annex H.

[1]: ftp://medical.nema.org/medical/dicom/2011/11_15pu.pdf

In addition to the configuration of basic DICOM devices, the application has been
extended to allow for the configuration of dcm4che specific DICOM implementations, such
as the DICOM proxy (dcm4chee-proxy) and XDS.b applications (dcm4chee-xds).

Supported configuration backends include *LDAP* and *Java Preferences*.

Building
========

Prerequisites
-------------

Building the dcm4chee-wizard and dependencies requires [Maven 3](http://maven.apache.org).

Before building dcm4chee-wizard, check out and build the following projects:
* [dcm4che-3.x DICOM Toolkit] (http://github.com/dcm4che/dcm4che).
* [dcm4chee-proxy] (https://github.com/dcm4che/dcm4chee-proxy)
* [dcm4chee-xds] (https://github.com/dcm4che/dcm4chee-xds)

dcm4chee-wizard
---------------

    > mvn install -P {standalone|proxy|jdbc|jdbc-proxy}

*standalone*: basic web-app with configuration options for supported devices
*proxy*: in addition to *standalone* includes dcm4chee-proxy web-application (bundle)
*jdbc*: in addition to *standalone* contains a deployment dependency for use with [dcm4che-jdbc-prefs] 
(https://github.com/dcm4che/dcm4che-jdbc-prefs), a wrapper for storage of Java Preferences data in a SQL backend.
*jdbc-proxy*: combination of *proxy* and *jdbc* profiles

Setup
=====

JBoss
-----

**Dependencies**

To run dcm4chee-wizard within JBoss AS7 requires dcm4che-jboss-modules to be installed,
which can be found in the dcm4che-3.x DICOM Toolkit (https://github.com/dcm4che/dcm4che).
Unpack `dcm4che-jboss-modules-<version>.zip` into the JBoss AS7 folder.

**Container Configuration**

Create a directory `dcm4chee-wizard` inside the container configuration directory 
(e.g. `<jbossDir>/standalone/configuration/dcm4chee-wizard`)
and copy all files from `dcm4chee-wizard/dcm4chee-wizard-war/src/main/etc/` into it.
Alternatively, specify the directory path with the "dcm4chee-wizard.cfg.path" system property.
Strings specified with "${property.name}" will be evaluated accordingly

If planned to use Java Preferences as configuration backend, delete the file
`ldap.properties` from `<jbossDir>/standalone/configuration/dcm4chee-wizard/`.

If planned to use a LDAP configuration backend, edit the file
`<jbossDir>/standalone/configuration/dcm4chee-wizard/ldap.properties`
and set the connection and authentication parameters according
to the LDAP server configuration.

**Deployment**

To run dcm4chee-wizard in a JBoss AS7 instance, deploy
`dcm4chee-wizard/dcm4chee-wizard-war/target/dcm4chee-wizard-war-<version>.war`
via the JBoss command line interface or by copying it into e.g. `<jbossDir>/standalone/deployments/`.

*Example:* 

i) make sure the JBoss instance is running

ii) start the command line interface: `<jbossDir>/bin/jboss-cli.sh -c`

iii) call the deploy procedure: `deploy <buildPath>/dcm4chee-wizard-war-<version>.war`

**Configuration of supported device service**

For each device with network services supported by the wizard (currently dcm4chee-proxy and dcm4chee-xds),
configure the following system property in the container configuration
(e.g. `<jbossDir>/standalone/configuration/standalone.xml`)
```
<system-properties>
    <property name="org.dcm4chee.device.<device-name>" value="http://<device-url>"/>
</system-properties>
```

*Example:*
In case the wizard was compiled with the profile "proxy", the bundled proxy application 
can be configured as followed:
```
<system-properties>
    <property name="org.dcm4chee.device.dcm4chee-proxy" value="http://${jboss.bind.address}:${jboss.bind.port}/dcm4chee-wizard/proxy-application"/>
</system-properties>
```

Configuration
=============

Basic Device
------------

Proxy Device
------------

Audit Logger
------------