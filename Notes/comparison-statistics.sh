#!/bin/bash

for p in '| D          |' '|            |' '| ok         |' '| ?          |' '| X          |' '| XX         |' '| XXX        |' '| XXXX       |' ; do echo "$p  `grep comparison.md -e "$p" | wc -l`"; done; echo -n "Total           "; expr `grep comparison.md -e '|............|' | wc -l` - 2

