FROM d-201603244.ufsc.br/oracle-java8:latest

COPY java /java/lib

ENV JAVA_OPTS="-Xmx2048m"
ENV JAVA_MAIN_CLASS="br.ufsc.gsigma.services.resilience.bootstrap.RunResilienceService"
ENV JAVA_ARGS="0.0.0.0 8080 resilienceservice.d-201603244.ufsc.br 6500"