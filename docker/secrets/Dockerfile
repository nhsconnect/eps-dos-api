FROM dos-api/certbot

VOLUME /etc/letsencrypt/

ENTRYPOINT aws s3 cp s3://${ENV}-secrets/letsencrypt/live /etc/letsencrypt/live --recursive