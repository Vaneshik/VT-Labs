#!/bin/bash

echo "Deploying to Helios"

## Remove existing deployment
ssh -p 2222 s409858@se.ifmo.ru "rm -rf wildfly-26.1.3/standalone/deployments/meowmurr-1.0.war"
# add new deployment
scp -P 2222 ./target/meowmurr-1.0.war s409858@se.ifmo.ru:wildfly-26.1.3/standalone/deployments