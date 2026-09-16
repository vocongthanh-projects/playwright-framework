FROM mcr.microsoft.com/playwright/java:v1.61.0-noble

ENV PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 \
    PLAYWRIGHT_BROWSERS_PATH=/ms-playwright \
    MAVEN_OPTS="-Dmaven.repo.local=/cache/m2 -Dfile.encoding=UTF-8"

RUN install -d -m 0775 /cache/m2

WORKDIR /builds

RUN java -version && mvn -version
