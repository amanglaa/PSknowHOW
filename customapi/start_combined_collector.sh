#!/bin/bash
java -jar EncryptionUtility.jar &
BACK_PID=$!
wait $BACK_PID
java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar customapi.jar --spring.config.location=classpath:/BOOT-INF/classes/application.properties --spring.config.additional-location=optional:file:/app/properties/customapi.properties
