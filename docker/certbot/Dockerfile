FROM certbot/certbot

VOLUME /etc/letsencrypt/
VOLUME /var/www/html

ENTRYPOINT certbot certonly --webroot --webroot-path /var/www/html -d $ENV.eps.digital.nhs.uk --email aled.greenhalgh@nhs.uk --agree-tos --no-eff-email --staging