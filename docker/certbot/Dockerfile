FROM certbot/certbot

VOLUME /etc/letsencrypt/
VOLUME /var/www/html

RUN \
	apk -Uuv add groff less python py-pip && \
	pip install awscli && \
	apk --purge -v del py-pip && \
	rm /var/cache/apk/*

RUN mkdir -p /opt/dos-api/secrets

COPY push-s3.sh /opt/dos-api/secrets

RUN chmod 0644 /opt/dos-api/secrets/push-s3.sh

ENTRYPOINT certbot certonly --webroot --webroot-path /var/www/html -d $ENV.eps.digital.nhs.uk --email aled.greenhalgh@nhs.uk --agree-tos --no-eff-email --deploy-hook /opt/dos-api/secrets/push-s3.sh --staging