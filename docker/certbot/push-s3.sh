#!/bin/sh
aws s3 cp /etc/letsencrypt/live s3://${ENV}-secrets/letsencrypt/live --recursive --sse