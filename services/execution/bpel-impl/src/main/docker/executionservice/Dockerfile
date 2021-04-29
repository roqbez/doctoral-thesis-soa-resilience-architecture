FROM d-201603244.ufsc.br/oracle-java8:latest

COPY java /java

ENV JAVA_OPTS="-Xmx7168m -XX:+UseG1GC -Dclean=true"
ENV JAVA_MAIN_CLASS="br.ufsc.gsigma.services.execution.bpel.bootstrap.RunExecutionService"
ENV JAVA_ARGS="0.0.0.0 8080 executionservice.d-201603244.ufsc.br 7000 webapp"