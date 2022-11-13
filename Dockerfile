FROM clojure:lein-2.9.3

# ARG ARG_DEV_USER=developer
# AWS creds to able fetch libs from S3
# ARG AWS_ACCESS_KEY_ID
# ARG AWS_SECRET_ACCESS_KEY

# ENV DEV_USER $ARG_DEV_USER
# ENV HOME_DIR /home/$DEV_USER
# ENV APP_DIR $HOME_DIR/minoro
ENV NO_LAUNCH 0
ENV HOSTNAME puelle.me

RUN apt update && \
    apt install -y vim
    # Add user
    # useradd -ms /bin/bash $DEV_USER

COPY project.clj app/

RUN cd app && \
    lein deps
    # Apply owner permissions
    # chown -R $DEV_USER:$DEV_USER $HOME_DIR

# RUN mkdir -p /root/.ssh && chmod 0700 /root/.ssh && touch /root/.ssh/known_hosts

# RUN mkdir -p /opt/jmx_exporter
# RUN wget -O /opt/jmx_exporter/jmx_prometheus_javaagent-0.16.1.jar https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/0.16.1/jmx_prometheus_javaagent-0.16.1.jar
# COPY docker/jmx/config.yaml /opt/jmx_exporter/config.yaml

COPY src app/src
COPY public app/public
COPY scripts/entrypoint.sh app/

# COPY docker/prod/entrypoint.sh app/
# COPY docker/prod/storecerts.sh app/
WORKDIR app

RUN mkdir -p /var/log/exported && touch /var/log/exported/notion-commands.log
VOLUME ["/var/log/exported"]

EXPOSE 80

CMD ./entrypoint.sh -l /var/log/exported/notion-commands.log -n ${NO_LAUNCH}
