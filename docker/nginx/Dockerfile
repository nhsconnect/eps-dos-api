FROM nginx

COPY dos-api.conf /etc/nginx/conf.d/dos-api.conf 

RUN mkdir -p -m 0755 /var/www/dos-api

RUN rm /etc/nginx/conf.d/default.conf

VOLUME /var/www/dos-api

CMD ["nginx", "-g", "daemon off;"]