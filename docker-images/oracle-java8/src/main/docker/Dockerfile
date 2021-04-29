##https://runnable.com/docker/java/dockerize-your-java-application
FROM  phusion/baseimage:0.9.17

ENV JAVA_VER="8"
ENV JAVA_HOME="/usr/lib/jvm/java-8-oracle"
ENV JAVA_CLASSPATH="/java/bin:/java/lib/*"

ENV \ 
 DEBIAN_FRONTEND="noninteractive" \
 LANG="pt_BR.UTF-8" \
 LANGUAGE="pt_BR.UTF-8" \
 LC_ALL="pt_BR.UTF-8" \
 TZ="America/Sao_Paulo"

COPY run-java.sh /run-java.sh
COPY inspectit-agent.jar /inspectit-agent.jar

RUN \
	echo "deb http://archive.ubuntu.com/ubuntu trusty main universe" > /etc/apt/sources.list && \
	apt-get -y update && \
	DEBIAN_FRONTEND=noninteractive apt-get install -y -q \
		python-software-properties \
		software-properties-common \
		tzdata \
		wget \
		nano \
		unzip \
		lsof \
		rsync \
		net-tools \
		dnsutils \
		iproute2 \ 
		iputils-ping \
		telnet \
		iperf \
		gettext-base \
		htop \
		jq && \
	echo 'deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main' >> /etc/apt/sources.list && \
    echo 'deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main' >> /etc/apt/sources.list && \
    apt-key adv --keyserver keyserver.ubuntu.com --recv-keys C2518248EEA14886 && \
    apt-get update && \
    echo oracle-java${JAVA_VER}-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections && \
    apt-get install -y --force-yes --no-install-recommends oracle-java${JAVA_VER}-installer oracle-java${JAVA_VER}-set-default && \
    apt-get clean && \
    rm -rf /var/cache/oracle-jdk${JAVA_VER}-installer && \
	update-java-alternatives -s java-8-oracle && \
	echo "export JAVA_HOME=/usr/lib/jvm/java-8-oracle" >> ~/.bashrc && \
	apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* && \
	chmod +x /run-java.sh  && \
 	locale-gen pt_BR.UTF-8 && \
 	update-locale LANGUAGE=pt_BR.UTF-8 LANG=pt_BR.UTF-8 LC_CTYPE=pt_BR.UTF-8 LC_ALL=pt_BR.UTF-8 
	

WORKDIR /java
	
CMD ["/run-java.sh"]