FROM d-201603244.ufsc.br/oracle-java8:latest

COPY java /java/lib

ENV JAVA_OPTS="-Xmx512m"
ENV JAVA_MAIN_CLASS="br.ufsc.gsigma.services.specifications.ubl.bootstrap.RunUBLServices"
ENV JAVA_ARGS="0.0.0.0 8080 ${HOST:-ublservices.d-201603244.ufsc.br} ${PORT:-11000} "