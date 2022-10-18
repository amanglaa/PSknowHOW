FROM openjdk:8-jre-slim-buster

# There are environment variables with periods in the names so change bash as default
RUN ln -sf /bin/bash /bin/sh

VOLUME /tmp
VOLUME /app
VOLUME /app/properties
VOLUME /app/offline_data
VOLUME /app/certs

ARG ZEPHYR_JAR_FILE=processors/zephyr/target/zephyr-processor.jar
ARG BITBUCKET_JAR_FILE=processors/bitbucket/target/bitbucket-processor.jar
ARG JENKINS_JAR_FILE=processors/jenkins/target/jenkins-processor.jar
ARG SONAR_JAR_FILE=processors/sonar/target/sonar-processor.jar
ARG BAMBOO_JAR_FILE=processors/bamboo/target/bamboo-processor.jar
ARG TEAMCITY_JAR_FILE=processors/teamcity/target/teamcity-processor.jar
ARG GITLAB_JAR_FILE=processors/gitlab/target/gitlab-processor.jar
ARG GITHUB_JAR_FILE=processors/github/target/github-processor.jar

ARG JENKINS_PROPERTIES_FILE_NAME=jenkins.properties
ARG BAMBOO_PROPERTIES_FILE_NAME=bamboo.properties
ARG BITBUCKET_PROPERTIES_FILE_NAME=bitbucket.properties
ARG SONAR_PROPERTIES_FILE_NAME=sonar.properties
ARG ZEPHYR_PROPERTIES_FILE_NAME=zephyr.properties
ARG TEAMCITY_PROPERTIES_FILE_NAME=teamcity.properties
ARG GITLAB_PROPERTIES_FILE_NAME=gitlab.properties
ARG GITHUB_PROPERTIES_FILE_NAME=github.properties

ADD ${ZEPHYR_JAR_FILE} /app/zephyr.jar
ADD ${JENKINS_JAR_FILE} /app/jenkins.jar
ADD ${SONAR_JAR_FILE} /app/sonar.jar
ADD ${BAMBOO_JAR_FILE} /app/bamboo.jar
ADD ${BITBUCKET_JAR_FILE} /app/bitbucket.jar
ADD ${TEAMCITY_JAR_FILE} /app/teamcity.jar
ADD ${GITLAB_JAR_FILE} /app/gitlab.jar
ADD ${GITHUB_JAR_FILE} /app/github.jar


WORKDIR /app
ENV JAVA_OPTS=""

EXPOSE 50001
EXPOSE 50002
EXPOSE 50003
EXPOSE 50007
EXPOSE 50011
EXPOSE 50012
EXPOSE 50014
EXPOSE 50019

ADD processors/nonjira_combined_processor_docker/start_combined_collector.sh start_combined_collector.sh
RUN ["chmod", "+x", "/app/start_combined_collector.sh"]
CMD ["sh", "start_combined_collector.sh"]
