# EPS DoS API Docker Images

## certbot - letsencrypt client
Needs the `ENV` environment variable set to the environment name, e.g. `ENV=dos-dev`.

Uses the volumes: 
 
* `webroot` - mapped to the nginx web root in order for certbot to put the challenge response files
* `cert` - mapped to /etc/letsencrypt in nginx storing certificate and key material

## dos-api-nginx
Needs the `ENV` environment variable set to the environment name, e.g. `ENV=dos-dev`.

Uses the volumes: 
 
* `webroot`
* `cert`

## dos-api-app
Needs the dos-api-VERSION.jar copied from dos-api-app/target