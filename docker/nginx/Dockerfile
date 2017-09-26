FROM nginx:alpine

COPY dos-api.conf /etc/nginx/conf.d/dos-api.conf 

RUN mkdir -p -m 0755 /var/www/dos-api

RUN mkdir -p -m 0600 /etc/letsencrypt

RUN rm /etc/nginx/conf.d/default.conf

RUN \
	apk -Uuv add groff less python py-pip && \
	pip install awscli && \
	apk --purge -v del py-pip && \
	rm /var/cache/apk/*
  
VOLUME /var/www/dos-api

RUN mkdir -p /opt/dos-api

COPY entrypoint.sh /opt/dos-api/entrypoint.sh

RUN chmod +x /opt/dos-api/entrypoint.sh

ENTRYPOINT ["/opt/dos-api/entrypoint.sh"]