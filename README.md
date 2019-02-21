# LSystems
kotlin code for rendering LSystems to SVG files

get help:

[09:36][:~/source/LSystems(master)]$ ./gradlew run -PlsArgs="['-h']"

help output:

usage: [-h] [-i ITERATIONS] [-o OUTPUTSIZE] [-u HUE] [-b BRI] [-s LSYSTEM]
       [-w LINEWIDTH] [-v OUTLINEWIDTH]

optional arguments:
  -h, --help                    show this help message and exit

  -i ITERATIONS,                Number of iterations of the L system
  --iterations ITERATIONS

  -o OUTPUTSIZE,                Size of the output svg image (in pixels)
  --outputSize OUTPUTSIZE

  -u HUE, --hue HUE             The path to the hue input image

  -b BRI, --bri BRI             The path to the brightness input image

  -s LSYSTEM,                   What L system to use: , , , , ,
  --lsystem LSYSTEM

  -w LINEWIDTH,                 The width of the line
  --lineWidth LINEWIDTH

  -v OUTLINEWIDTH,              The width of the outline of the line
  --outlineWidth OUTLINEWIDTH

[09:36][:~/source/LSystems(master)]$ ./gradlew run -PlsArgs="['-s SnowFlake', '-i 3', '-o 400', '-w 1', '-v 1', '-b che_b.png', '-u che_h.png' ]"

> Task :run
Init
Rendering SnowFlake.
Generated fractal in: 2ms
Convert to XY in: 1ms
Scale XY list in: 1ms
Generate midpoints: 1ms
Draw spline outlines: 238ms
Draw splines: 123ms
Draw total: 393ms
Done

Deprecated Gradle features were used in this build, making it incompatible with Gradle 5.0.
Use '--warning-mode all' to show the individual deprecation warnings.
See https://docs.gradle.org/4.10.1/userguide/command_line_interface.html#sec:command_line_warnings

BUILD SUCCESSFUL in 2s
3 actionable tasks: 1 executed, 2 up-to-date
