docker run -it -p 1883:1883 -p 9001:9001 -v mosquitto.conf:/mosquitto/config/mosquitto.conf eclipse-mosquitto

mosquitto_sub -h 127.0.0.1 -p 1883 -u testsub -t test

mosquitto_pub -h 127.0.0.1 -p 1883 -u testpub -t test -m alo123
