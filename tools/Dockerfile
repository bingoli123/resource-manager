FROM 192.168.202.61/idp/base/openjdk-log:1.8.0_275
MAINTAINER  zcj
ENV TZ Asia/Shanghai
RUN ln -fs /usr/share/zoneinfo/${TZ} /etc/localtime     && echo ${TZ} > /etc/timezone
RUN mkdir -p /opt/components/
RUN mkdir -p /var/log/components/resource-manager
ADD resource-manager_3.0.0-beta.14_linux_x64_java.tar.gz /opt/components/
WORKDIR /opt/components/resource-manager
CMD /usr/sbin/crond start && ./cmds/start.sh
