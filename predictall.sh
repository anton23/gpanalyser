#!/usr/bin/bash
for model in data*/*.plain; do
  modelOutFile="${model/plain/dat}"
  bash build/dist/gpa-0.9.3b-probes-linux-amd64.sh $model --noGUI --plain > $modelOutFile
done
