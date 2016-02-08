#!/bin/sh
# make-movie.sh

rm -f img????-s.png

for name in img????.png
do
  short="${name%.*}"
  echo $short
  #pngtopnm "$name" | pnmscale 20 | pnmtopng > "${short}-s.png"
  convert "$name" -scale 500x500 -define png:color-type=2 "${short}-s.png"
done

rm -f movie.mp4

avconv -r 20 -i img%04d-s.png movie.mp4

# eof



