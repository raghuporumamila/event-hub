# Use the official AlloyDB Omni image
FROM google/alloydbomni:latest

# Optional: Add custom initialization scripts
# Any .sql or .sh scripts in this folder run on first container start
# COPY ./init-db/ /docker-entrypoint-initdb.d/

# Set default environment variables (can be overridden in Compose)
ENV POSTGRES_PASSWORD=change_me_in_compose
ENV DATA_DIR=/var/lib/postgresql/data

# Expose the standard PostgreSQL port
EXPOSE 5432