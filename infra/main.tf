terraform {
  required_version = ">= 1.5.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.5"
    }
  }
}

provider "aws" {
  region = "us-east-1"
}

# Stack: 4 EC2 t2.micro.
#  - frontend: nginx + build React
#  - usuario:  micro + MySQL (BD en la misma maquina = 1 EC2 menos)
#  - reserva / servicio: micro
# Para probar rapido
# NOTA: el contenido de los heredocs user_data va a columna 0 a proposito.
# terraform fmt + espacios antes del shebang rompen la ejecucion en cloud-init.
locals {
  micros = {
    usuario  = { port = 8083 }
    reserva  = { port = 8081 }
    servicio = { port = 8082 }
  }
  app_micros = {
    reserva  = { port = 8081 }
    servicio = { port = 8082 }
  }
  micro_public_ip = merge(
    { usuario = aws_instance.usuario.public_ip },
    { for k, v in aws_instance.micro : k => v.public_ip }
  )
}

data "aws_ami" "al2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-2023.*-x86_64"]
  }
}

# key_name vockey: par de llaves del lab de AWS Academy
variable "key_name" {
  default = "vockey"
}

resource "random_password" "db_pass" {
  length  = 20
  special = false
}

# VPC propia: 1 VPC + 1 subred publica + IGW + ruta por defecto
resource "aws_vpc" "this" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = { Name = "cloudnative-vpc" }
}

resource "aws_internet_gateway" "this" {
  vpc_id = aws_vpc.this.id
  tags   = { Name = "cloudnative-igw" }
}

resource "aws_subnet" "public" {
  vpc_id                  = aws_vpc.this.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = "us-east-1a"
  map_public_ip_on_launch = true

  tags = { Name = "cloudnative-subnet-public" }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.this.id
  tags   = { Name = "cloudnative-rt-public" }
}

resource "aws_route" "to_internet" {
  route_table_id         = aws_route_table.public.id
  destination_cidr_block = "0.0.0.0/0"
  gateway_id             = aws_internet_gateway.this.id
}

resource "aws_route_table_association" "public" {
  subnet_id      = aws_subnet.public.id
  route_table_id = aws_route_table.public.id
}

# Security Group unico para las 4 EC2
resource "aws_security_group" "ec2" {
  name        = "cloudnative-sg-ec2"
  description = "EP1: 8081-8083 publicos (API Gateway + verificacion), 80 frontend, 22 SSH lab, 3306 interno entre EC2."
  vpc_id      = aws_vpc.this.id

  ingress {
    from_port   = 8081
    to_port     = 8083
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "API Gateway + acceso directo de verificacion"
  }

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Frontend nginx"
  }

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "SSH para subir jars y dist (vockey)"
  }

  ingress {
    from_port   = 3306
    to_port     = 3306
    protocol    = "tcp"
    self        = true
    description = "MySQL: solo conexion entre las EC2 del grupo"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# EC2 frontend: nginx + build React (se sube dist/ por scp)
resource "aws_instance" "frontend" {
  ami                    = data.aws_ami.al2023.id
  instance_type          = "t2.micro"
  key_name               = var.key_name
  subnet_id              = aws_subnet.public.id
  vpc_security_group_ids = [aws_security_group.ec2.id]

  user_data = <<-EOT
#!/bin/bash
exec > /var/log/frontend-bootstrap.log 2>&1
set -exuo pipefail
dnf install -y nginx
mkdir -p /var/www/grandhotel
cat > /etc/nginx/conf.d/grandhotel.conf <<'NGINX'
server {
    listen 80;
    server_name _;
    root /var/www/grandhotel;
    index index.html;
    location / {
        try_files $uri $uri/ /index.html;
    }
}
NGINX
systemctl enable --now nginx
echo "Listo: sube el contenido de Frontend/dist a /var/www/grandhotel por scp"
EOT

  tags = { Name = "cn-frontend", Project = "CloudNative01" }
}

# EC2 usuario: micro + MySQL (los demas micros apuntan a su IP privada)
resource "aws_instance" "usuario" {
  ami                    = data.aws_ami.al2023.id
  instance_type          = "t2.micro"
  key_name               = var.key_name
  subnet_id              = aws_subnet.public.id
  vpc_security_group_ids = [aws_security_group.ec2.id]

  user_data = <<-EOT
#!/bin/bash
exec > /var/log/cn-usuario-bootstrap.log 2>&1
set -exuo pipefail
dnf install -y java-21-amazon-corretto-devel
mkdir -p /opt/apps
dnf install -y https://dev.mysql.com/get/mysql80-community-release-el9-4.noarch.rpm
dnf config-manager --enable mysql80-community
dnf install -y mysql-community-server
systemctl enable --now mysqld
DB_HOST='127.0.0.1'
DB_USER='admin'
DB_PASS='${random_password.db_pass.result}'
for i in $(seq 1 60); do
mysqladmin ping --silent >/dev/null 2>&1 && break
sleep 5
done
MYSQL_ROOT_PASSWORD=$(grep -oP "(?<=temporary password is generated for root@localhost: )\S+" /var/log/mysqld.log | tail -1)
MYSQL_TMP_PASS="$DB_PASS!"
mysql -u root -p"$MYSQL_ROOT_PASSWORD" --connect-expired-password -e "ALTER USER 'root'@'localhost' IDENTIFIED BY '$MYSQL_TMP_PASS';"
mysql -u root -p"$MYSQL_TMP_PASS" -e "SET GLOBAL validate_password.policy = LOW;"
mysql -u root -p"$MYSQL_TMP_PASS" -e "ALTER USER 'root'@'localhost' IDENTIFIED BY '$DB_PASS';"
mysql -u root -p"$DB_PASS" -e "
CREATE USER IF NOT EXISTS '$DB_USER'@'%' IDENTIFIED BY '$DB_PASS';
GRANT ALL PRIVILEGES ON *.* TO '$DB_USER'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;
"
for db in h_usuario h_reserva h_servicio; do
mysql -u root -p"$DB_PASS" -e "CREATE DATABASE IF NOT EXISTS $db CHARACTER SET utf8mb4;"
done
cat > /etc/systemd/system/cn-usuario.service <<UNIT
[Unit]
Description=CloudNative microservicio usuario
After=network-online.target
[Service]
WorkingDirectory=/opt/apps
Environment=DB_HOST=$DB_HOST
Environment=DB_PORT=3306
Environment=DB_NAME=h_usuario
Environment=DB_USERNAME=$DB_USER
Environment=DB_PASSWORD=$DB_PASS
Environment=SERVER_PORT=8083
ExecStart=/usr/bin/java -jar /opt/apps/usuario-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=5
[Install]
WantedBy=multi-user.target
UNIT
systemctl daemon-reload
systemctl enable cn-usuario
echo "Listo: sube /opt/apps/usuario-0.0.1-SNAPSHOT.jar por scp y ejecuta: systemctl start cn-usuario"
EOT

  tags = { Name = "cn-usuario", Project = "CloudNative01" }
}

# EC2 reserva/servicio: solo micro, BD en la instancia usuario
resource "aws_instance" "micro" {
  for_each               = local.app_micros
  ami                    = data.aws_ami.al2023.id
  instance_type          = "t2.micro"
  key_name               = var.key_name
  subnet_id              = aws_subnet.public.id
  vpc_security_group_ids = [aws_security_group.ec2.id]

  user_data = <<-EOT
#!/bin/bash
exec > /var/log/cn-${each.key}-bootstrap.log 2>&1
set -exuo pipefail
dnf install -y java-21-amazon-corretto-devel
mkdir -p /opt/apps
DB_HOST='${aws_instance.usuario.private_ip}'
DB_USER='admin'
DB_PASS='${random_password.db_pass.result}'
cat > /etc/systemd/system/cn-${each.key}.service <<UNIT
[Unit]
Description=CloudNative microservicio ${each.key}
After=network-online.target
[Service]
WorkingDirectory=/opt/apps
Environment=DB_HOST=$DB_HOST
Environment=DB_PORT=3306
Environment=DB_NAME=h_${each.key}
Environment=DB_USERNAME=$DB_USER
Environment=DB_PASSWORD=$DB_PASS
Environment=SERVER_PORT=${each.value.port}
ExecStart=/usr/bin/java -jar /opt/apps/${each.key}-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=5
[Install]
WantedBy=multi-user.target
UNIT
systemctl daemon-reload
systemctl enable cn-${each.key}
echo "Listo: sube /opt/apps/${each.key}-0.0.1-SNAPSHOT.jar por scp y ejecuta: systemctl start cn-${each.key}"
EOT

  tags = { Name = "cn-${each.key}", Project = "CloudNative01" }
}

# API Gateway HTTP API v2 con JWT authorizer (Entra)
resource "aws_apigatewayv2_api" "http" {
  name          = "cloudnative-ep1"
  protocol_type = "HTTP"
  cors_configuration {
    allow_origins = ["*"]
    allow_methods = ["GET", "POST", "PUT", "DELETE", "OPTIONS"]
    allow_headers = ["authorization", "content-type"]
    max_age       = 300
  }
}

resource "aws_apigatewayv2_authorizer" "jwt" {
  api_id           = aws_apigatewayv2_api.http.id
  authorizer_type  = "JWT"
  name             = "entra-jwt"
  identity_sources = ["$request.header.Authorization"]
  authorizer_result_ttl_in_seconds = 0

  jwt_configuration {
    audience = ["46cda96c-1ab7-42af-a838-5ad46de31226", "api://46cda96c-1ab7-42af-a838-5ad46de31226"]
    issuer   = "https://login.microsoftonline.com/e34a6311-e69d-4a97-bedf-2ad7cc5455a0/v2.0"
  }
}

resource "aws_apigatewayv2_integration" "http" {
  for_each = local.micros

  api_id             = aws_apigatewayv2_api.http.id
  integration_type   = "HTTP_PROXY"
  integration_method = "ANY"
  integration_uri    = "http://${local.micro_public_ip[each.key]}:${each.value.port}/api/v1/${each.key}/{proxy}"
}

resource "aws_apigatewayv2_route" "http" {
  for_each = local.micros

  api_id             = aws_apigatewayv2_api.http.id
  route_key          = "ANY /api/v1/${each.key}/{proxy+}"
  authorization_type = "JWT"
  authorizer_id      = aws_apigatewayv2_authorizer.jwt.id
  target             = "integrations/${aws_apigatewayv2_integration.http[each.key].id}"
}

# Preflight CORS sin auth: el micro responde OPTIONS (igual que en local)
resource "aws_apigatewayv2_route" "options" {
  for_each = local.micros

  api_id             = aws_apigatewayv2_api.http.id
  route_key          = "OPTIONS /api/v1/${each.key}/{proxy+}"
  authorization_type = "NONE"
  target             = "integrations/${aws_apigatewayv2_integration.http[each.key].id}"
}

resource "aws_apigatewayv2_stage" "default" {
  api_id      = aws_apigatewayv2_api.http.id
  name        = "$default"
  auto_deploy = true
}

# Outputs
output "frontend_public_ip" {
  value = aws_instance.frontend.public_ip
}

output "usuario_public_ip" {
  value = aws_instance.usuario.public_ip
}

output "micros_public_ip" {
  value = { for k, v in aws_instance.micro : k => v.public_ip }
}

output "api_url" {
  value = aws_apigatewayv2_api.http.api_endpoint
}

output "db_password" {
  value     = random_password.db_pass.result
  sensitive = true
}