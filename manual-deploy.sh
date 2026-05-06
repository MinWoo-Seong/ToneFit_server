#!/usr/bin/env bash
set -e

scp -i .ssh/tonfit-subkey.pem \
  build/libs/tonefit-server-0.0.1-SNAPSHOT.jar \
  ec2-user@54.180.177.37:/app/app.jar

ssh -i .ssh/tonfit-subkey.pem ec2-user@54.180.177.37 \
  "sudo systemctl restart tonefit"