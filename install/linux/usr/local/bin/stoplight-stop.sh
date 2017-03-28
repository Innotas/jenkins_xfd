#!/bin/bash
#pid=`ps aux | grep jenkins_xfd | grep -v grep | awk '{print $2}'`
#kill -9 $pid
ps aux | grep jenkins_xfd | grep -v grep | awk '{print $2}' | xargs sudo kill -9
