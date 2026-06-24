
## Deploy

First, install the dependencies.

```shell
apt update && apt install nano certbot nodejs npm nginx python3-certbot-nginx
```

Ensure your domain DNS is pointed to the server's IP address.

### Upload the UI

Place the contents of the UI in `/root/ui`.

### HTTP → HTTPS

Replace the contents of `/etc/nginx/sites-enabled/default` with the following.

```
server {
    server_name ferrero.dev;
    root /root/ui;
    gzip on;
    gzip_types text/plain application/javascript;

    listen 80;

    location / {
        index index.html;
        try_files $uri $uri/ /index.html =404;
    }
}
```

### Finally, configure certbot and nginx

```shell
chmod 755 /root
certbot --nginx
nginx -t
service nginx restart
```
