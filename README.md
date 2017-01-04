# AdventureTrackerAndroid

Hardware device can be found here: https://github.com/snxfz947/AdventureTrackerDE2

Android application to complement the Adventure Tracker hardware platform. Application is called "Adventure Logger" to distinguish it from the Adventure Tracker device.

Adventure Logger pairs with the hardware device over a bluetooth connection, and allows the user to download trip data to review statistics, and share trips with other users. The Android application complements the hardware device as it allows the user to back up trip logs to remote storage and provides a familiar, clean interface, while the more rugged Adventure Tracker logs trip data in harder environments where a mobile phone may lose reception or become damaged.

## Features

* NFC pairing functionality to compare trip statistics
* Trip log transfer between hardware component and Android application
* Trip data plotting in Google Maps
* Statistics page for individual trips, and running total across all trips

## Communication

<img src="https://github.com/snxfz947/AdventureTrackerAndroid/blob/master/Images/Communication.png" width="400"><br>_Communication between Adventure Tracker and Adventure Logger_

## App Pages

<img src="https://github.com/snxfz947/AdventureTrackerAndroid/blob/master/Images/Flowchart.png" width="600"><br>_Page transitions_

## Menu Page

Here the user is able to discover and pair to Adventure Tracker using the connect button. The user is also able to sync their files with Adventure Tracker. If the user tried to do this before pairing, a snackbar message would appear asking them to connect first. A popup would also appear if: a) the DE2 timed out (no characters in the InputStream buffer within 100ms) b) the files are already synced. Pairing status is shown and reflects whether the device is not paired, attempting to find the DE2, pairing or paired. The user can fling left and right to open the other pages. 

## Compare Stats


