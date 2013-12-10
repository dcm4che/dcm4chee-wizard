# Introduction

The dcm4chee-wizard is a web-application that provides a graphical user interface for
configuration of DICOM devices compliant to the *DICOM Application Configuration Management Profile*,
specified in [DICOM 2011, Part 15][1], Annex H.

[1]: ftp://medical.nema.org/medical/dicom/2011/11_15pu.pdf

In addition to the configuration of basic DICOM devices, the application has been
extended to allow for the configuration of dcm4che specific DICOM implementations, such
as the DICOM proxy (dcm4chee-proxy) and XDS.b applications (dcm4chee-xds).

Supported configuration backends include *LDAP* and *Java Preferences*.

# Building

## Prerequisites

Building the dcm4chee-wizard and dependencies requires [Maven 3](http://maven.apache.org).

Before building dcm4chee-wizard, check out and build the following projects:
* [dcm4che-3.x DICOM Toolkit] (http://github.com/dcm4che/dcm4che).
* [dcm4chee-proxy] (https://github.com/dcm4che/dcm4chee-proxy)
* [dcm4chee-xds] (https://github.com/dcm4che/dcm4chee-xds)

## dcm4chee-wizard

    > mvn install -P {standalone|proxy|jdbc|jdbc-proxy}

*standalone*: basic web-app with configuration options for supported devices
*proxy*: in addition to *standalone* includes dcm4chee-proxy web-application (bundle)
*jdbc*: in addition to *standalone* contains a deployment dependency for use with [dcm4che-jdbc-prefs] 
(https://github.com/dcm4che/dcm4che-jdbc-prefs), a wrapper for storage of Java Preferences data in a SQL backend.
*jdbc-proxy*: combination of *proxy* and *jdbc* profiles

# Setup

## JBoss

### Dependencies

To run dcm4chee-wizard within JBoss AS7 requires dcm4che-jboss-modules to be installed,
which can be found in the dcm4che-3.x DICOM Toolkit (https://github.com/dcm4che/dcm4che).
Unpack `dcm4che-jboss-modules-<version>.zip` into the JBoss AS7 folder.

### Container Configuration

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

### Deployment

To run dcm4chee-wizard in a JBoss AS7 instance, deploy
`dcm4chee-wizard/dcm4chee-wizard-war/target/dcm4chee-wizard-war-<version>.war`
via the JBoss command line interface or by copying it into e.g. `<jbossDir>/standalone/deployments/`.

*Example:* 

i) make sure the JBoss instance is running

ii) start the command line interface: `<jbossDir>/bin/jboss-cli.sh -c`

iii) call the deploy procedure: `deploy <buildPath>/dcm4chee-wizard-war-<version>.war`

### Configuration of supported device service

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

# Configuration

Use the "New Device" button to create a configuration for a network entity.
Supported configuration types are described below.

## Basic Device

A basic device can be used to describe network entities such as modalities or workstation.
The available configuration options are based on the DICOM standard and include 
DICOM device parameters, network connection parameters, 
DICOM network application entity parameters including transfer capabilities supported by the AET,
as well as HL7 application parameters. 

## Proxy Device

A proxy device extends the basic device with support for dcm4chee-proxy specific configuration options.

### Additional Device Attributes

* *Scheduler Interval*: interval to check for scheduled c-stores and logging
* *Forward Threads*: maximum number of concurrent forward threads
* *Stale Timeout*: Stale timeout of cached configuration in seconds 0 (=never invalidate cached configuration) if absent

### Additional Device Sub-Nodes

* *Audit Loggers*: create a local audit logger that will send messages to a configured audit record repository 

### Additional Network AE Attributes

* *Spool Directory*: directory for temporary DICOM file
* *Accept Data On Failed Association*: temporary store data on failed negotiation with Called AET
* *Enable Audit Log*: enable audit logging
* *Delete Failed Data Without Retry Configuration*: delete files based on error without retry configuration after first failed forwarding
* *Proxy PIX Consumer*: Application^Facility name of local PIX Consumer Application
* *Remote PIX Manager*: Application^Facility name of remote PIX Manager Application
* *Fallback Destination AET*: send DICOM objects to target Called AET as fallback option

### Additional Network AE Sub-Nodes

#### Forward Rules

Allows configuration of filter rules to be applied on incoming data

**Attributes:**

* *Common Name*: Name of forward rule
* *Destination URI*: List of aets to forward the request to, alternatively can be a stylesheet (see tooltip) 
* *Accepted Calling AE Title*: Match rule to calling AET of incoming request
* *Accepted DIMSE(s)*: Match rule to DIMSE of incoming request
* *Match SOP Classes*: Match rule to SOP Class of DIMSE request (sub-condition of the above) 
* *Match Transfer Capabilities*: Create sub-set of supported transfer capabilities (tcs) and tcs supported by the destination AET
* *Receive Schedule Days*: days this rule is active
* *Receive Schedule Hours*: hours this rule is active
* *Use Calling AE Title*: Calling AE Title to be used in forward association
* *Run PIX Query*: execute PIX query to configured PIX manager
* *MPPS to Dose SR Template URI*: URI of conversion template
* *Description*: free text field

#### Forward Options

Allows configuration of sending options for destination AET

**Attributes**:

* *Destination AE Title*: AET this Forward Option applies to
* *Send Schedule Days*: days for sending data to this AET (will be cached otherwise)
* *Send Schedule Hours*: hours for sending data to this AET (will be cached otherwise)
* *Description*: free text field
* *Convert Enhanced Multi-frame to Single-frame*: enable enhanced multi-frame to single-frame conversion

#### Retries

Allows configuration of retry conditions

**Attributes**:

* *Type*: exception type this retry applies to
* *Delay*: delay before trying to resend
* *Number of Retries*: max number of times to re-send data in case of previous failure
* *Delete after final retry*: delete files after final retry, otherwise will be moved to directory _noRetry_

#### Coercions 

Definition of coercion templates

**Attributes**:

* *Common Name*: Name for this item
* *DIMSE*: Match DIMSE type
* *Match DIMSE Service SOP Classes*: sub-condition of the above
* *Transfer Role*: Match incoming (SCP) or outgoing (SCU) connections
* *Labeled URI*: Path to template file
* *Use for AETitle*: Match to incoming or outgoing AET

## Audit Record Repository

Provides a network entity for audit loggers to send data to.