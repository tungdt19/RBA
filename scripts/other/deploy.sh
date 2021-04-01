#!/bin/bash

# To run this script from anywhere in the console, run the following commands:
# chmod +x $(pwd)/fm
# echo -e "\nalias fm='$(pwd)/fm'\nalias fma='fm a'\nalias fmg='fm g'\nalias fmp='fm p'" >> ~/.bashrc
# source ~/.bashrc

FOLDER="/home/vht/be"
PROFILE="production"
PORT="8080"
BASE_NAME="iot-vtag"
CONTAINER="vtag/be"

if [ $# -eq 0 ]; then
  docker ps -a | grep $BASE_NAME
  exit 0;
fi

file=$(ls "$FOLDER" | grep "$BASE_NAME[-]")
regex="^$BASE_NAME-(.*?)\.jar$"
[[ $file =~ $regex ]]
read -r _ VERSION <<< "${BASH_REMATCH[@]}"


dockerfile() {
  echo "Create Dockerfile at '$FOLDER/$MODULE'"
  {
    echo "FROM openjdk"
    echo 'LABEL maintainer="duclm22@viettel.com.vn"'
    echo "COPY ./$BASE_NAME-$VERSION.jar ./[a]pplication.yml /be"
    echo "RUN echo \"Asia/Ho_Chi_Minh\" > /etc/timezone"
    echo "CMD [\"java\", \"-jar\", \"/be/$BASE_NAME-$VERSION.jar\", \"-Dspring.profiles.active=$PROFILE\"]"
  } > "$FOLDER/$MODULE/Dockerfile"
}

build() {
  docker load < /k8s/install/image/java.tar
  docker build -t "$CONTAINER:$VERSION" "$FOLDER"
}

run() {
  docker run -dp "$PORT:$PORT" --name $CONTAINER -e "SPRING_PROFILES_ACTIVE=$PROFILE" "$CONTAINER:$VERSION"
}

remove() {
  docker rm $CONTAINER
}

stop() {
  docker stop $CONTAINER
}

restart() {
  docker restart $CONTAINER
}

log_follow() {
  docker logs -f --since "10m" $CONTAINER
}

log_history() {
  docker logs $CONTAINER 2>&1 | less -R
}

ssh() {
  docker exec -it $CONTAINER bash
}

for i in "${@:2}"; do
  case "$i" in
    r | run)              run ;;
    S | ssh)              ssh ;;
    s | stop)             stop ;;
    b | build)            build ;;
    d | rm | remove)      remove ;;
    R | re | restart)     restart ;;
    f | '' | lf | follow) log_follow ;;
    h | lh | history)     log_history ;;
    u | up | upgrade)     dockerfile; build; stop; remove; run ;;
    *)                    echo "Do not recognize command '$2'" ;;
  esac
done
