#!/bin/sh
aws s3 cp /etc/letsencrypt/live s3://${ENV}_secrets/letsencrypt/live --recursive --sse