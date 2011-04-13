#!/bin/sh

java -Xmx512m -Djava.library.path=lib/linux-amd64 -jar build/dist/gpa-@version.jar -3D "$@"
