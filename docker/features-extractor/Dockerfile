FROM openjdk:15-jdk-alpine3.11
RUN apk add --no-cache python3
ADD docker/features-extractor/files/* /
RUN python3 mapping_tests.py && python3 tests_methods_vps.py && python3 jsonfilter_tests.py
ENTRYPOINT sh extract.sh