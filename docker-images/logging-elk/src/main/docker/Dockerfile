FROM sebp/elk:630

#Logstash
COPY 01-syslog-input.conf /etc/logstash/conf.d
COPY 10-syslog.conf /etc/logstash/conf.d
COPY 30-output.conf /etc/logstash/conf.d
COPY elasticsearch-template.json /etc/logstash
RUN rm /etc/logstash/conf.d/02-beats-input.conf

#Kibana
COPY kibana.yml ${KIBANA_HOME}/config

EXPOSE 10514