# LSystems
kotlin code for rendering LSystems to SVG files

example run:

>gradle run -PlsArgs="['-h']"
Starting a Gradle Daemon (subsequent builds will be faster)

> Task :run
Init
usage: [-h] [-i ITERATIONS] [-S OUTPUTIMAGESIZE] [-b USEBEZIERCURVES]
       [-t THEMENAME] [-p IMAGENAME] [-s SYSTEM] [-r PALETTEREPEAT]
       [-w LINEWIDTH]

optional arguments:
  -h, --help                          show this help message and exit

  -i ITERATIONS,                      Number of iterations of the L system
  --iterations ITERATIONS

  -S OUTPUTIMAGESIZE,                 Size of the output svg image (in pixels)
  --outputImageSize OUTPUTIMAGESIZE

  -b USEBEZIERCURVES,                 Draw the L system using bezier curves,
  --useBezierCurves USEBEZIERCURVES   not straight lines

  -t THEMENAME,                       The name of the theme, default
  --themeName THEMENAME               "hsv_gradient_checkered", look in
                                      Themes.kt for more themes

  -p IMAGENAME,                       The path to the input image
  --imageName IMAGENAME

  -s SYSTEM, --system SYSTEM          What L system to use, default:
                                      "DragonCurve" other curves:
                                      KochSnowFlakeLSystem, HilbertCurve,
                                      Line, SierpinskiCurve, SnowFlake1Curve

  -r PALETTEREPEAT,                   The repeat frequency of the palette (1)
  --paletteRepeat PALETTEREPEAT

  -w LINEWIDTH,                       The width of the line
  --lineWidth LINEWIDTH


BUILD SUCCESSFUL in 10s
2 actionable tasks: 1 executed, 1 up-to-date
