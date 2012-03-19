#!/bin/sh

java -Xmx512m -Djava.library.path=lib/linux-i586 -jar build/dist/gpa-@version.jar -3D "$@"
