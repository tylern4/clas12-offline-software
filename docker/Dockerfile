FROM maven:3.5-jdk-8 as builder
ENV CLAS12DIR /coatjava
RUN apt-get update \
    && apt-get install --no-install-recommends -y openjfx unzip \
    && apt-get clean \
    && rm -f /var/lib/apt/lists/*_dists_*
COPY . /tmp
WORKDIR /tmp
RUN ./build-coatjava.sh

FROM openjdk:8-alpine
COPY --from=builder /coatjava /coatjava
ENV CLAS12DIR /coatjava
RUN /sbin/apk add bash
RUN echo "export CLAS12DIR=/coatjava" > /root/.bashrc
RUN echo "export PATH=$PATH:$CLAS12DIR/bin:$JAVA_HOME/bin" > /root/.bashrc
CMD /bin/bash
