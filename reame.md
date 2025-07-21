docker compose down -v --remove-orphans && docker compose build --no-cache && docker compose up


docker exec -it kafka-1 kafka-topics.sh --create \
--bootstrap-server localhost:9093 \
--topic config-updates \
--config cleanup.policy=compact \
--partitions 1 \
--replication-factor 1