FROM nginx

COPY dos-api /etc/nginx/sites-available/dos-api

RUN mkdir /var/www/dos-api
RUN chmod 0755 /var/www/dos-api

RUN ln -s /etc/nginx/sites-enabled/dos-api /etc/nginx/sites-available/dos-api 
RUN rm /etc/nginx/sites-enabled/default

VOLUME /var/www/dos-api
