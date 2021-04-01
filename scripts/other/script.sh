mosquitto_sub -h 127.0.0.1 -p 1883 -u username -P password -t test

mosquitto_pub -h 127.0.0.1 -p 1883 -u username -P password -t test -m "$(date)"
