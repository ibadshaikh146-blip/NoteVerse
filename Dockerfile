FROM eclipse-temurin:11-jdk AS build
WORKDIR /app
COPY WEB-INF/src ./src
COPY WEB-INF/lib ./lib
COPY compile-only-lib ./compile-only-lib
RUN rm -f src/com/noteverse/Main.java
RUN mkdir -p classes && \
    javac -cp "lib/*:compile-only-lib/*" -d classes $(find src -name "*.java")

FROM tomcat:9.0-jdk11
RUN rm -rf /usr/local/tomcat/webapps/ROOT
RUN mkdir -p /usr/local/tomcat/webapps/ROOT/WEB-INF/lib
COPY WEB-INF/web.xml /usr/local/tomcat/webapps/ROOT/WEB-INF/web.xml
COPY --from=build /app/classes /usr/local/tomcat/webapps/ROOT/WEB-INF/classes
COPY WEB-INF/lib/*.jar /usr/local/tomcat/webapps/ROOT/WEB-INF/lib/
COPY *.html /usr/local/tomcat/webapps/ROOT/
COPY *.css /usr/local/tomcat/webapps/ROOT/
EXPOSE 8080
CMD ["catalina.sh", "run"]