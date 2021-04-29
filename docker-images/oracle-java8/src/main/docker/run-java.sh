#!/bin/sh
set -e

if [ "$JAVA_JMX" = "1" ]; then
	if [ "$JAVA_JMX_OPTS" = "" ]; then
		HOST_IP="$(curl -f http://ws.ufsc.br/ip 2> /dev/null)"
		JAVA_JMX_OPTS="
				-Dcom.sun.management.jmxremote
				-Dcom.sun.management.jmxremote.port=${JMX_PORT:-12345}
				-Dcom.sun.management.jmxremote.rmi.port=${JMX_PORT:-12345}
				-Dcom.sun.management.jmxremote.ssl=false
				-Dcom.sun.management.jmxremote.authenticate=false
				-Djava.rmi.server.hostname=${HOST_IP:-0.0.0.0}"
	fi
fi

if [ "$JAVA_DEBUG" = "1" ]; then
	if [ "$JAVA_DEBUG_OPS" = "" ]; then
		JAVA_DEBUG_OPS="-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=18000"
	fi
fi

if [ "$JAVA_NET_OPTS" = "" ]; then
	JAVA_NET_OPTS="-Djava.net.preferIPv4Stack=true"
fi

if [ "$INSPECTIT_ENABLED" = "1" ]; then
	JAVA_APM_OPTS="-Xbootclasspath/p:/inspectit-agent.jar -javaagent:/inspectit-agent.jar -Dinspectit.repository=150.162.6.131:9070 -Dinspectit.agent.name=$(hostname)"
fi

exec $JAVA_HOME/bin/java $JAVA_OPTS $JAVA_NET_OPTS $JAVA_JMX_OPTS $JAVA_DEBUG_OPS $JAVA_APM_OPTS -cp "$JAVA_CLASSPATH" $JAVA_MAIN_CLASS $JAVA_ARGS $JAVA_ARGS_EXTRA
