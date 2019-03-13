# Android-AC-Remote
This project is intended to be an android app to control any Air conditioning unit with an unkown IR protocol once the protocol of the device has been reverse engineered.  
For this initial commit the project only supports a single Carrier device but it shouldn't be very hard to edit the values to fit any AC protocol. 
Future commits will add some support for protocols that are found on online archives as well as some way to use your own protocol either in IRP notation or an easier parameter notation within the application or using a file.

## Changing the protocol
For now the only way to change the default protocol used is by editing the source code. Namely the MainActivity.java file. But having already analysed and reverse engineered a protocol will make this process much easier as you get to understand IR signals better.

These are the lines you will most likely need to edit:
* The values for High and Low in microseconds  
![](/Screenshots/HighLow.PNG)
* The Sample raw signal which is later edited to correspond to the current values of parameters  
![](/Screenshots/Raw.PNG)
* The code which edit the sample raw signal to put the parameter values in (notice that my protocol was in LSB)  
![](/Screenshots/UpdateRaw.PNG)
* And the algorithm for calculating the checksum (if any)  
![](/Screenshots/Checksum.PNG)
