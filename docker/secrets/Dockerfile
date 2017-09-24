FROM dos-api/certbot

VOLUME /etc/letsencrypt/

ENTRYPOINT aws s3 cp s3://${ENV}_secrets/letsencrypt/live /etc/letsencrypt/live --recursive