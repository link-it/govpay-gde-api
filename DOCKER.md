# GovPay GDE (Giornale degli Eventi) API - Docker Setup

Docker containerization for GovPay GDE REST API service that provides event logging and journal access for GovPay applications.

## Overview

This Docker setup provides:
- Multi-database support (PostgreSQL, MySQL/MariaDB, Oracle)
- Lightweight REST API service
- Health checks and monitoring
- Configurable connection pooling
- ORM mapping for different database vendors
- Integration-ready for GovPay A.C.A. and other services

## Quick Start

### 1. Build the Docker Image

```bash
# Build with PostgreSQL support (default)
./build_image.sh

# Build with specific database
./build_image.sh -v 3.8.0 -d postgresql
./build_image.sh -v 3.8.0 -d mysql
./build_image.sh -v 3.8.0 -d oracle
```

### 2. Configure Environment

```bash
# Copy the template and edit
cp .env.template .env
nano .env
```

**Required configuration:**
- `DB_PASSWORD`: Database password

### 3. Start the Services

```bash
# Start with docker-compose
docker-compose up -d

# View logs
docker-compose logs -f govpay-gde-api

# Check status
docker-compose ps

# Test API
curl http://localhost:8080/actuator/health
```

## Architecture

```
┌─────────────────────────────────────┐
│      GovPay GDE REST API            │
│                                     │
│  ┌──────────────────────────────┐  │
│  │   Spring Boot Application    │  │
│  │   - REST Controllers         │  │
│  │   - Event Service            │  │
│  │   - JPA Repositories         │  │
│  └──────────────────────────────┘  │
│              │                      │
│              ▼                      │
│  ┌──────────────────────────────┐  │
│  │   HikariCP Connection Pool   │  │
│  └──────────────────────────────┘  │
└─────────────────┬───────────────────┘
                  │
                  ▼
      ┌───────────────────────┐
      │   Database (RDBMS)    │
      │  - PostgreSQL         │
      │  - MySQL/MariaDB      │
      │  - Oracle             │
      └───────────────────────┘
```

## Database Configuration

### PostgreSQL (Default)

```env
DB_JDBC_URL=jdbc:postgresql://postgres:5432/govpay
DB_DRIVER=org.postgresql.Driver
DB_HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect
DB_ORM_MAPPING=META-INF/orm-postgres.xml
```

### MySQL/MariaDB

```env
DB_JDBC_URL=jdbc:mysql://mysql:3306/govpay?zeroDateTimeBehavior=convertToNull
DB_DRIVER=com.mysql.cj.jdbc.Driver
DB_HIBERNATE_DIALECT=org.hibernate.dialect.MySQLDialect
DB_ORM_MAPPING=META-INF/orm-mysql.xml
```

Edit `docker-compose.yml` to uncomment the MySQL service.

### Oracle

```env
DB_JDBC_URL=jdbc:oracle:thin:@//oracle:1521/XE
DB_DRIVER=oracle.jdbc.OracleDriver
DB_HIBERNATE_DIALECT=org.hibernate.dialect.OracleDialect
DB_ORM_MAPPING=META-INF/orm-oracle.xml
```

For TNS Names:
```env
DB_JDBC_URL=jdbc:oracle:thin:@TNSNAME
ORACLE_TNS_ADMIN=/etc/govpay
```

Mount `tnsnames.ora`:
```yaml
volumes:
  - ./tnsnames.ora:/etc/govpay/tnsnames.ora:ro
```

## Integration with GovPay A.C.A.

To use GDE with the A.C.A. batch processor:

### 1. Share the Database

Both services can use the same database:

```yaml
# In docker-compose.yml for both services
networks:
  - govpay-network
```

### 2. Configure A.C.A. to Use GDE

In A.C.A.'s `.env` file:
```env
GDE_ENABLED=true
GDE_BASE_URL=http://govpay-gde-api:8080
```

### 3. Combined docker-compose.yml Example

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    # ... shared database config

  govpay-gde-api:
    image: linkitaly/govpay-gde:3.8.0_postgresql
    depends_on:
      - postgres
    # ... GDE config

  govpay-aca:
    image: linkitaly/govpay-aca:3.8.0_postgresql
    depends_on:
      - postgres
      - govpay-gde-api
    environment:
      IT_GOVPAY_GDE_ENABLED: "true"
      IT_GOVPAY_GDE_CLIENT_BASEURL: "http://govpay-gde-api:8080"
    # ... ACA config

networks:
  govpay-network:
```

## Configuration Reference

### Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | Yes | - | JDBC connection URL |
| `SPRING_DATASOURCE_USERNAME` | Yes | - | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Yes | - | Database password |
| `SPRING_JPA_MAPPING_RESOURCES` | No | auto-detected | ORM mapping file |
| `SERVER_PORT` | No | `8080` | API listening port |
| `DB_POOL_MIN_IDLE` | No | `2` | Min idle connections |
| `DB_POOL_MAX_SIZE` | No | `5` | Max pool size |
| `JAVA_MIN_HEAP` | No | `256m` | Min heap size |
| `JAVA_MAX_HEAP` | No | `512m` | Max heap size |
| `DEBUG` | No | `false` | Enable debug logging |

### Hardcoded Settings

These are baked into the Docker image:
- Timezone: `Europe/Rome`
- Connection timeout: `20000ms`
- Idle timeout: `10000ms`
- Log file: `/var/log/govpay/govpay-gde-api.log`

### ORM Mapping Files

The service automatically selects the correct ORM mapping based on database type:
- **PostgreSQL**: `META-INF/orm-postgres.xml`
- **MySQL/MariaDB**: `META-INF/orm-mysql.xml`
- **Oracle**: `META-INF/orm-oracle.xml`

## API Endpoints

Once running, the following endpoints are available:

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Metrics
```bash
curl http://localhost:8080/actuator/metrics
```

### Info
```bash
curl http://localhost:8080/actuator/info
```

## Monitoring

### Health Check

Docker Compose includes health checks:

```bash
# Check service health
docker-compose ps

# View health check logs
docker inspect govpay-gde-api | jq '.[0].State.Health'
```

### Logs

```bash
# Follow logs
docker-compose logs -f govpay-gde-api

# Inside container
docker exec -it govpay-gde-api tail -f /var/log/govpay/govpay-gde-api.log
```

### Resource Usage

```bash
# Monitor container resources
docker stats govpay-gde-api
```

## Troubleshooting

### Database Connection Issues

```bash
# Check database is reachable
docker-compose exec govpay-gde-api ping postgres

# Verify credentials
docker-compose exec postgres psql -U govpay -d govpay -c "SELECT 1"

# Check JDBC URL format
docker-compose exec govpay-gde-api env | grep DATASOURCE
```

### ORM Mapping Issues

```bash
# Verify ORM mapping file is set correctly
docker-compose exec govpay-gde-api env | grep MAPPING

# Check logs for JPA errors
docker-compose logs govpay-gde-api | grep -i "jpa\|hibernate\|orm"
```

### Port Conflicts

If port 8080 is already in use:

```env
# In .env file
SERVER_PORT=8081
```

Update docker-compose.yml:
```yaml
ports:
  - "8081:8080"
```

### Memory Issues

Adjust heap sizes in `.env`:
```env
JAVA_MIN_HEAP=512m
JAVA_MAX_HEAP=1024m
```

## Performance Tuning

### Connection Pool

For high-traffic scenarios:

```env
DB_POOL_MIN_IDLE=5
DB_POOL_MAX_SIZE=20
```

### JVM Options

For production deployments:

```yaml
environment:
  JAVA_OPTS: "-XX:+UseG1GC -XX:MaxGCPauseMillis=100"
```

## File Structure

```
govpay-gde-api/
├── Dockerfile.github       # Docker image definition
├── build_image.sh          # Build script
├── entrypoint.sh          # Container startup script
├── docker-compose.yml     # Orchestration configuration
├── .env.template          # Environment variables template
├── DOCKER.md             # This file
└── README.md             # Java development documentation
```

## Database Schema

GDE stores event journal entries. Ensure the database schema is initialized:

```sql
-- Example table structure (actual schema in GovPay)
CREATE TABLE eventi (
  id BIGSERIAL PRIMARY KEY,
  data_evento TIMESTAMP NOT NULL,
  tipo_evento VARCHAR(255),
  componente VARCHAR(255),
  categoria VARCHAR(255),
  ...
);
```

The schema is managed by GovPay's main installer.

## Security Considerations

1. **Credentials**: Never commit `.env` file to version control
2. **Secrets**: Use Docker secrets in production
3. **Network**: Restrict API access using Docker networks
4. **Authentication**: Implement API authentication for production
5. **Firewall**: Limit access to actuator endpoints
6. **Database**: Use read-only credentials if GDE is read-only

## Production Deployment

### Using Docker Secrets

```yaml
services:
  govpay-gde-api:
    secrets:
      - db_password
    environment:
      SPRING_DATASOURCE_PASSWORD_FILE: /run/secrets/db_password

secrets:
  db_password:
    external: true
```

### Using Docker Swarm

```bash
# Deploy as a stack
docker stack deploy -c docker-compose.yml govpay-gde
```

### High Availability

Run multiple replicas:

```yaml
deploy:
  replicas: 3
  update_config:
    parallelism: 1
    delay: 10s
  restart_policy:
    condition: on-failure
```

## Support

For issues related to:
- **GovPay**: https://github.com/link-it/govpay
- **Java development**: See `README.md`
- **Docker setup**: Contact your DevOps team

## License

This Docker setup follows the same license as GovPay.
