FROM haproxy:1.8.13

COPY *.cfg /usr/local/etc/haproxy/

CMD ["haproxy", "-f", "/usr/local/etc/haproxy/haproxy-failover.cfg"]