#!/bin/sh

if [ -z "$ENV" ]; then
  echo >&2 'error: missing ENV environment variable'
  exit 1
fi
echo 'Starting nginx'
# TODO: some checking of success here
aws s3 cp s3://${ENV}-secrets/letsencrypt/live /etc/letsencrypt/live --recursive
#eval $(aws s3 cp s3://${ENV}-secrets/secrets.txt - | sed 's/^/export /')

nginx -g "daemon off;"