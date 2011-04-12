#!/bin/sh

java -Xmx512m -Djava.library.path=lib/linux-amd64 -jar build/dist/gpa-0.9.jar "$@"
