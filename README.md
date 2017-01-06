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

<img src="https://github.com/snxfz947/AdventureTrackerAndroid/blob/master/Images/Screenshot_2016-04-09-16-44-43.png" width="200"><br>_Menu_

## Compare Stats

Here the user is able to check their total lifetime stats, and compare with their friends. When this page is opened the user is immediately shown their own stats. From this point the user is able to reload their stats or compare them to their friends. If the user attempts to make a compare but there is no totals file received from another device a message is displayed asking them to make a transfer first. To make a transfer the users must tap their devices back to back, then allow Android to prompt them to begin the Android Beam transfer. The user’s stats are compared to the most recently received totals file from their friend.

<img src="https://github.com/snxfz947/AdventureTrackerAndroid/blob/master/Images/Screenshot_2016-04-09-16-44-52.png" width="200"> <img src="https://github.com/snxfz947/AdventureTrackerAndroid/blob/master/Images/Screenshot_2016-04-09-17-26-45.png" width="200"> <img src="https://github.com/snxfz947/AdventureTrackerAndroid/blob/master/Images/Screenshot_2016-04-09-16-44-58.png" width="200">  <br>_Comparing stats_

## Past Trips

Here the user is able to access each of the trips that they have received on their phone from Adventure Tracker. When a file is selected, they are brought to a new page which plots their trip on a Google Maps screen.

<img src="https://github.com/snxfz947/AdventureTrackerAndroid/blob/master/Images/Screenshot_2016-04-09-16-45-04.png" width="200"><br>_Past trips_

### Google Maps Page

The information about the trip is extracted from the information from Adventure Tracker i.e. the the gps positions, altitudes, times, and positions of points of interest. Using the Google Maps functionality, the coordinates are plotted onto the map to draw the path taken. Markers are added to the trip - yellow for Points of interest, green for the start location, and red for the end position.

<img src="https://github.com/snxfz947/AdventureTrackerAndroid/blob/master/Images/Screenshot_2016-04-09-16-45-12.png" width="200"><br>_Google Maps plot_

### Trip Statistics

Here the user is shown trip statistics for the chosen trip. Statistics include total distance travelled in kilometers, the duration of the trip, and the total change in altitude. The user can view the trip statistics page by selecting a trip from the past trip page, and then selecting "Trip Statistics" from the map page.

<img src="https://github.com/snxfz947/AdventureTrackerAndroid/blob/master/Images/Screenshot_2016-04-09-16-45-16.png" width="200"><br>_Google Maps plot_
