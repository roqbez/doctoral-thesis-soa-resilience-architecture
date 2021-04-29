FROM haproxy:1.8.13

COPY *.cfg /usr/local/etc/haproxy/

#RUN apt-get -y update && apt-get -y install wget && \
#	apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* && \
#	wget https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh && \
#	chmod +x wait-for-it.sh

CMD ["haproxy", "-f", "/usr/local/etc/haproxy/haproxy-failover.cfg"]