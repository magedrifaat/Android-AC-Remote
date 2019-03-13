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

## Analyzing an unknown protocol
IR protocols of Air conditioners are one of the hardest out there because when you press e.g. Temprature Up it doesn't just send a "Temprature Up" signal. Instead, it changes the state of the remote to the new temprature and sends a signal that that includes the whole state of the remote including the Tempratue, Fan, Mode, Time etc. and possibly also sends checksum bits and those bits are usually the hardest to analyze.  
I have only ever analysed one device (the device used in this project) so I don't have a very good experience in that matter but here is some sources that could definitely help:
* [Reverse engineering Hitachi air conditioner infrared remote commands](https://perhof.wordpress.com/2015/03/29/reverse-engineering-hitachi-air-conditioner-infrared-remote-commands/)
* [Reverse Engineering Air Conditioner IR Remote Control Protocol](https://www.instructables.com/id/Reverse-engineering-of-an-Air-Conditioning-control/)
* [Decoding Samsung AC IR Remote](https://github.com/markszabo/IRremoteESP8266/issues/505)
* Tools like [IrScrutinizer](http://www.harctoolbox.org/IrScrutinizer.html) and [IrpTransmogrifier](https://github.com/bengtmartensson/IrpTransmogrifier) by [bengtmartensson](https://github.com/bengtmartensson) will make your life much easier once you get used to them.  
I found stroring the different signals as categories in a [text file](/Protocol/analysis.txt) and then forming a [spreadsheet file](/Protocol/analysis.xlsx) from the data really helpful.
