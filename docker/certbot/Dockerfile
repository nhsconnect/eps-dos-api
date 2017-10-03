FROM certbot/certbot

VOLUME /etc/letsencrypt/

RUN \
	apk -Uuv add groff less python py-pip && \
	pip install awscli && \
	apk --purge -v del py-pip && \
	rm /var/cache/apk/*

COPY push-s3.sh /usr/local/bin

RUN chmod u+x /usr/local/bin/push-s3.sh

ENTRYPOINT certbot certonly --standalone --preferred-challenges http -d $ENV.eps.digital.nhs.uk --email aled.greenhalgh@nhs.uk --agree-tos --no-eff-email --deploy-hook /usr/local/bin/push-s3.sh --staging