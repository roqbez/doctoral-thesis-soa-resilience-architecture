FROM d-201603244.ufsc.br/oracle-java8:latest

COPY java /java/lib

ENV JAVA_OPTS="-Xmx256m"
ENV JAVA_MAIN_CLASS="br.ufsc.gsigma.services.bpelexport.bootstrap.RunBPELExportService"
ENV JAVA_ARGS="0.0.0.0 8080 bpelexporter.d-201603244.ufsc.br 8083"