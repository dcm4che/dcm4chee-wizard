Security configuration:

Put the files 
"dcm4chee-wizard/dcm4chee-wizard-war/src/main/etc/roles.json"
"dcm4chee-wizard/dcm4chee-wizard-war/src/main/etc/tls-protocols.txt"
"dcm4chee-wizard/dcm4chee-wizard-war/src/main/etc/tls-ciphersuites.txt"
into a "dcm4chee-wizard/" directory relative to the configuration directory of your jboss 7
or specify the directory path with the "dcm4chee-wizard.cfg.path" system property.
Strings specified with "${property.name}" will be evaluated accordingly.