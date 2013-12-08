scratch4arduino
===============

software and firmware to control Arduino with Scratch integrated by Claudio Becchetti.

v1.2 8-12-2013

It is used for primary school children (9 years) to build and control robot cars 
starting from commercial cheap remote controlled cars and arduino boards

Children are building their own cars with arduino boards to perform  a
football match among robots.
Scratch controls
1) horn, lights, motors(brake, acceleration, direction), ultrasound to avoid walls 

The directory includes

1) firmware based on firmata for arduino
2) extension for scratch with blocks for digital -analog read write pwm
3) a a4s modified java program to connect scratch to arduino


this software-firmware extends A4S project and adds
1) fully compilable java library (existing A4S missed a file for compilation and needed some modifications to compile)
2) pwm management 
3) smarter serial management  (To be added)
4) ultrasonic sensor managed by scratch (To be added)



Original A4S is an experimental extension for [Scratch 2](http://scratch.mit.edu) that allows it to communicate with [Arduino](http://www.arduino.cc) boards using [Firmata](http://firmata.org/). It consists of a Java server that Scratch connects to over HTTP and which communicates with an Arduino board over (USB) serial. 
Original A4S is being developed by David Mellis, based on documentation and code from the Scratch team. For updates, see: <https://github.com/damellis/A4S/>


## Instructions to connect Scratch with Arduino

1. Install the [Scratch 2 offline editor](http://scratch.mit.edu/scratch2download/). 
2. Install the [Arduino software](http://arduino.cc/en/Main/Software). Instructions: [Windows](http://arduino.cc/en/Guide/Windows), [Mac OS X](http://arduino.cc/en/Guide/MacOSX).
3. Upload the StandardFirmata firmware to your Arduino board. (It's in "Examples > Firmata".) (the standard firmata will be modified to include ultrasonic sensor management)
4. [Download the A4S code](https://github.com/cbecc/scratch4arduino/archive/master.zip ) from GitHub and unzip it.
5. Launch the A4S server using the "run.sh" script on the command line. Pass the name of the serial port corresponding to your Arduino board as the first argument to the script, e.g. "./run.sh /dev/tty.usbmodem411". 
for windows change the "runa4scom13 usb direct.bat" with the proper serial port and then lanch it.
 You should see a message like: 

		Stable Library
		=========================================
		Native lib Version = RXTX-2.1-7
		Java lib Version   = RXTX-2.1-7
		HTTPExtensionExample helper app started on Mellis.local/18.189.9.217
		
6. Run the Scratch 2 offline editor.
7. While holding the shift key on your keyboard, click on the "File" menu in Scratch. You should see "Import Experimental Extension" at the bottom of the menu. Click on it.
8. Navigate to the directory containing A4S and select the A4S.s2e file.
9. You should see the A4S extension and blocks appear in the "More Blocks" category in the Scratch editor. If the A4S server is running, there will be a green dot next to the "A4S" title. 
10. in the example directory you will find an example of pwm on pin 13

## Instructions to to recompile a4s 
the a4s is compiled in a jar file but if you like to change
the source then you need to recompile as follows:


1) download and install java sdk (http://www.oracle.com/technetwork/java/javase/downloads/index.html)
  so that to have javac and jar executables
2)change the file "Compile_jar.bat" so that the java directory is correct
3) change a4s as needed and run "Compile_jar.bat"
