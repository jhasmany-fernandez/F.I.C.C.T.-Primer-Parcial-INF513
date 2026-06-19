FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /app/lib /app/out/classes /app/dist \
    && curl -fsSL https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.7/postgresql-42.7.7.jar \
        -o /app/lib/postgresql-42.7.7.jar

COPY src /app/src

RUN find /app/src -name '*.java' | sort > /tmp/sources.list \
    && javac -encoding UTF-8 --release 25 -cp /app/lib/postgresql-42.7.7.jar -d /app/out/classes @/tmp/sources.list \
    && jar --create --file /app/dist/ProyectoEmail.jar --main-class proyectoemail.ProyectoEmail -C /app/out/classes .

FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/dist/ProyectoEmail.jar /app/ProyectoEmail.jar
COPY --from=build /app/lib/postgresql-42.7.7.jar /app/lib/postgresql-42.7.7.jar
COPY docker/app/entrypoint.sh /app/entrypoint.sh

RUN chmod +x /app/entrypoint.sh

ENTRYPOINT ["/app/entrypoint.sh"]
