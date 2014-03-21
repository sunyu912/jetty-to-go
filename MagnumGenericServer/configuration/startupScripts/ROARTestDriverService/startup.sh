#!/bin/bash

nohup java \
      -Xms512m \
      -Xmx1024m \
      -Ds3cmd.cred.path=/etc/.s3cfg \
      $* \
      -jar /home/ubuntu/ROARTestDriverService/server.war &
