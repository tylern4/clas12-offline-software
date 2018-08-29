FROM maven:3.5-jdk-8 as builder
ENV CLAS12DIR /coatjava/build
RUN apt-get update \
    && apt-get install --no-install-recommends -y openjfx unzip \
    && apt-get clean \
    && rm -f /var/lib/apt/lists/*_dists_*
COPY . /coatjava
WORKDIR /coatjava
RUN ./build-coatjava.sh

FROM openjdk:8-alpine
COPY --from=builder /coatjava /coatjava
