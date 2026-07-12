#!/bin/bash

echo "Deploying to localhost"

## Remove existing deployment
rm -rf ~/wildfly-26.1.3/standalone/deployments/meowmurr-1.0.war
# add new deployment
cp ./target/meowmurr-1.0.war ~/wildfly-26.1.3/standalone/deployments