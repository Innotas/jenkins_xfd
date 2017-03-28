#!/bin/bash

cd /var/log/stoplight
java -jar /usr/share/java/jenkins_xfd-all.jar >> stdout_stoplight.log 2>> stderr_stoplight.log
