FROM d-201603244.ufsc.br/oracle-java8:latest

COPY java /java/lib

ENV JAVA_OPTS="-Xmx2048m -XX:+UseG1GC -Dclean=true"
ENV JAVA_MAIN_CLASS="br.ufsc.gsigma.binding.bootstrap.RunBindingService"
ENV JAVA_ARGS="0.0.0.0 8080 bindingservice.d-201603244.ufsc.br 9000"