#!/bin/sh

java -Xmx512m -Djava.library.path=lib/macosx -jar build/dist/gpa-@version.jar -3D "$@"
