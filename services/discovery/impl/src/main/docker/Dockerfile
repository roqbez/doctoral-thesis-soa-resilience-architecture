FROM d-201603244.ufsc.br/oracle-java8:latest

COPY java /java/lib

ENV JAVA_OPTS="-Xmx2048m"
ENV JAVA_MAIN_CLASS="br.ufsc.gsigma.servicediscovery.bootstrap.RunDiscoveryService"
ENV JAVA_ARGS="0.0.0.0 8080 discoveryservice.d-201603244.ufsc.br 8070"