# Spring MQTT Client

Contains 3 modules:

- Message broker core with MQTT client.
- Local (Consumer).
- Cloud (Producer).

## How to start?

### 1. Start Mosquitto from docker.

- Create mosquitto.conf:
  ```
  persistence false
  allow_anonymous true
  connection_messages true
  log_type all
  listener 1883
  ```
- Create docker-compose.yml:
  ```yaml
  version: "3"
  services:
    mqtt:
      image: eclipse-mosquitto
      volumes:
        - mqtt-data:/mosquitto/data
        - /home/ec2-user/mosquitto/mosquitto.conf:/mosquitto/config/mosquitto.conf
      ports:
        - 1883:1883
      restart: always
  volumes:
    mqtt-data: {}
  ```
- Start by command:
  ```shell
  docker-compose up
  ```
  
### 2. Start Redis

- Create docker-compose.yml:
  ```yaml
  version: '2'

  services:
    redis:
      image: docker.io/bitnami/redis:6.2
      environment:
        - ALLOW_EMPTY_PASSWORD=yes
        - REDIS_DISABLE_COMMANDS=FLUSHDB,FLUSHALL
      ports:
        - '6379:6379'
      volumes:
        - 'redis_data:/bitnami/redis/data'

  volumes:
    redis_data:
      driver: local
  ```
- Start by command:
  ```shell
  docker-compose up
  ```
  
### 3. Start Producer/Consumer with multi instance

- Change config of Mosquitto address to your local ip.
- Build and start Producer and Consumer.
  ```shell
  mvn clean package
  ```
- Trigger some API to produce message from Local to Cloud:
  ```shell
  curl --location --request POST 'localhost:8083/produce/class?studentSize=1'
  ```

  ```shell
  curl --location --request POST 'localhost:8083/produce/big-message'
  ```
