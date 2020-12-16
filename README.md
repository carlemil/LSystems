# LSystems
Kotlin code for rendering LSystems to PNG files. 
Systems are described in LSystem.SYSTEMS
Some params can be edited for how the system should be rendered.
See LSTest for a example of how to use/run this lib.

Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.carlemil:LSystems:-SNAPSHOT'
	}
# VWLine
