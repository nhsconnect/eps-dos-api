#!/bin/sh
aws s3 cp /etc/letsencrypt/live/*.eps.digital.nhs.uk/ s3://${ENV}-secrets/letsencrypt/live/ --recursive --sse