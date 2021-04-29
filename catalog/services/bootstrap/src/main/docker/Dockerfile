FROM d-201603244.ufsc.br/oracle-java8:latest

COPY java /java/lib

ENV JAVA_OPTS="-Xmx512m"
ENV JAVA_MAIN_CLASS="br.ufsc.gsigma.catalog.services.bootstrap.BootstrapCatalogService"
ENV JAVA_ARGS="0.0.0.0 8080 catalogservice.d-201603244.ufsc.br 8081"