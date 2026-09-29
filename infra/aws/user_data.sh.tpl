#!/bin/bash
set -euxo pipefail

# --- swap: t3.micro only has 1GB RAM, Maven builds OOM without this ---
if [ ! -f /swapfile ]; then
  fallocate -l 1G /swapfile
  chmod 600 /swapfile
  mkswap /swapfile
  swapon /swapfile
  echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi

# --- docker ---
curl -fsSL https://get.docker.com | sh
usermod -aG docker ubuntu
systemctl enable --now docker

mkdir -p /opt/divvy/repo/backend
chown -R ubuntu:ubuntu /opt/divvy

cat > /opt/divvy/docker-compose.yml <<'COMPOSE'
services:
  postgres:
    image: postgres:16-alpine
    restart: always
    environment:
      POSTGRES_DB: ${db_name}
      POSTGRES_USER: ${db_user}
      POSTGRES_PASSWORD: ${db_password}
    volumes:
      - pgdata:/var/lib/postgresql/data
    networks: [divvy]

  backend:
    build:
      context: ./repo/backend
    restart: always
    depends_on: [postgres]
    environment:
      datasource_url: jdbc:postgresql://postgres:5432/${db_name}
      datasource_username: ${db_user}
      datasource_password: ${db_password}
      driver_class: org.postgresql.Driver
      jwt_secret: ${jwt_secret}
      jwt_expiration: "${jwt_expiration}"
      SPRING_PROFILES_ACTIVE: prod
    networks: [divvy]

  caddy:
    image: caddy:2-alpine
    restart: always
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./Caddyfile:/etc/caddy/Caddyfile
      - caddy_data:/data
      - caddy_config:/config
    depends_on: [backend]
    networks: [divvy]

volumes:
  pgdata:
  caddy_data:
  caddy_config:

networks:
  divvy:
COMPOSE

cat > /opt/divvy/Caddyfile <<CADDY
${api_domain} {
	reverse_proxy backend:8080
}
CADDY

chown ubuntu:ubuntu /opt/divvy/docker-compose.yml /opt/divvy/Caddyfile

cat > /etc/systemd/system/divvy.service <<'UNIT'
[Unit]
Description=Divvy backend stack (docker compose)
Requires=docker.service
After=docker.service network-online.target
Wants=network-online.target

[Service]
Type=oneshot
RemainAfterExit=true
WorkingDirectory=/opt/divvy
ExecStart=/usr/bin/docker compose up -d
ExecStop=/usr/bin/docker compose down

[Install]
WantedBy=multi-user.target
UNIT

systemctl enable divvy.service
