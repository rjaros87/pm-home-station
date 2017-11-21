#!/bin/bash

# first, you have to have sigal - a python script (command) installed (http://sigal.saimon.org/en/latest/installation.html)
# pip install sigal 
# or
# easy_install sigal

rm -rf *.JPG index.html thumbs static
sigal build

echo "Done!"

if [[ "$OSTYPE" == "darwin"* ]]; then
  open index.html
fi
