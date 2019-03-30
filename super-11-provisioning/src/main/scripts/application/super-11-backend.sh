#!/bin/sh
### BEGIN INIT INFO
# Provides:          super-11-backend
# Required-Start:    $local_fs $network ${NAME}d $time $syslog
# Required-Stop:     $local_fs $network ${NAME}d $time $syslog
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Description:       The Super 11 Backend collects information to enable a frontend to display standings and statistics for the Super 11 Uden League.
### END INIT INFO

NAME="super-11-backend"
JAR_LOCATION="/opt/super-11-backend/"
LOG_LOCATION="/var/log/super-11-backend/"
DATA_LOCATION="/var/super-11-backend/"
CONFIG_LOCATION="/etc/super-11-backend/"
JAR_NAME="super-11-backend.jar"
START_COMMAND="java -Dvertx-config-path=${CONFIG_LOCATION}config.json -Dlogback.configurationFile=${CONFIG_LOCATION}logback.xml -jar ${JAR_NAME} run me.piepers.super11.application.Super11Application"
STOP_COMMAND="ps ax | egrep 'super-11-backend\.jar' | egrep '${SE_PID}' | egrep -v 'egrep' | egrep -v 'bash' | awk '{ print $1 }' | xargs --no-run-if-empty kill -15"
SCRIPT="cd ${JAR_LOCATION} && ${START_COMMAND}"
STOP_SCRIPT="cd ${JAR_LOCATION} && ${STOP_COMMAND}"
# FIXME: should be a service or application account on the server.
RUNAS="bas"
SUPER_11_BACKEND_CACHE="${DATA_LOCATION}/.super-11-cache"

PIDFILE=/var/run/super-11-backend.pid
SUPER_11_BACKEND_LOG_FILE=${LOG_LOCATION}super-11-backend.log

start() {

  # First, check whether super-11-backend is already running.
  if [ ! -z `ps ax | egrep 'super-11-backend\.jar' | egrep -v 'egrep' | egrep -v 'bash' | awk '{ print $1 }'` ]; then
    echo "${NAME} is already running."
    return 1
  fi

  # Setting the permissions explicitly
  rm -rf ${SUPER_11_BACKEND_CACHE} ${PIDFILE}
  mkdir -p ${SUPER_11_BACKEND_CACHE}
  chown -R ${RUNAS} ${SUPER_11_BACKEND_CACHE}
  chgrp -R ${RUNAS} ${SUPER_11_BACKEND_CACHE}

  echo 'Starting super-11-backend…' >&2
  local CMD="$SCRIPT & echo \$!"
  su -c "$CMD" $RUNAS </dev/null >/dev/null 2>/dev/null

  sleep 2

  PID=`ps ax | egrep 'super-11-backend\.jar' | egrep -v 'egrep' | egrep -v 'bash' | awk '{ print $1 }'`;
  if [ ! -z "${PID}" ]; then
    echo ${PID} | tee -a ${PIDFILE}
    echo "${NAME} is now running. The PID is ${PID}."
  else
    echo "Error! Could not start ${NAME}."
  fi
}

stop() {
  # First check super-11-backend is still running
  if [ -z `ps ax | egrep 'super-11-backend\.jar' | egrep -v 'egrep' | egrep -v 'bash' | awk '{ print $1 }'` ]; then
    echo "${NAME} is not running";
    return 1
  fi

  echo 'Stopping service…' >&2

  SE_PID=`cat ${PIDFILE}`
  su -s /bin/sh "${RUNAS}" -c "${STOP_SCRIPT}"
  rm -f "${PIDFILE}";
  rm -rf ${SUPER_11_BACKEND_CACHE}

  # As a backup, in case it did not get killed due to a corrupt PID file
  if [ ! -z `ps ax | egrep 'super-11-backend\.jar' | egrep -v 'egrep' | egrep -v 'bash' | awk '{ print $1 }'` ]; then
    ps ax | egrep 'super-11-backend\.jar' | egrep -v 'egrep' | awk '{ print $1 }' | xargs --no-run-if-empty kill -15;
  fi

  echo 'Service stopped' >&2
}

status() {
    printf "%-50s" "Checking ${NAME}."
    if [ -f $PIDFILE ] && [ -s $PIDFILE ]; then
        PID=$(cat $PIDFILE)
            if [ -z "$(ps axf | grep ${PID} | grep -v grep)" ]; then
                printf "%s\n" "super-11-backend appears to be dead but pidfile still exists"
            else
                echo "super-11-backend is running, the PID is $PID"
            fi
    else
        printf "%s\n" "Service not running"
    fi
}


case "$1" in
  start)
    start
    ;;
  stop)
    stop
    ;;
  status)
    status
    ;;
  restart)
    stop
    sleep 3
    start
    ;;
  *)
    echo "Usage: $0 {start|stop|status|restart}"
esac
