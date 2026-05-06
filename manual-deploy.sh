#!/bin/zsh
scp -i ~/.ssh/tonefit-key.pem \
  build/libs/tonefit-server-0.0.1-SNAPSHOT.jar \
  ec2-user@[AWS 탄력 IP]:/app/app.jar

ssh -i ~/.ssh/tonefit-key.pem ec2-user@[AWS 탄력 IP] \
  "sudo systemctl restart tonefit"