<p align="center">
  <a href="https://github.com/BrettBearden/Boxed/blob/master/Images/box_icon.png?raw=true" target="_blank"><img alt="Boxed Icon" src="https://github.com/BrettBearden/Boxed/blob/master/Images/box_icon.png?raw=true" style="max-width:10%;"></a>
</p>

About

Boxed is an organizer Android app for moving. It allows creating a move to keep track of each box and the contents that are packed. An image can be captured of each box and each item. A special feature allows placing a Radio Frequency Identification tag on boxes. The RFID allows scanning the tag and a list of the box content will immediately be displayed.


Code

Boxed was created in Android Studio and uses SQLite, NFC, Google Maps, and a custom camera preview. Once a move is created, an activity will show the move details with a Google Maps displaying markers and a line between the starting and final destination addresses. An activity will display a list of boxes which can be searched by items or RFID tag scanning. Most activities allow scanning an RFID tag at any time and the box activity will immediately be displayed. RFID is scanned using Near Field Communication. The custom camera preview will allow turning the flash on or off and snaps pictures by the press of a button. Once an image is captured it is rotated, scaled, and stored in the database as a blob. Upon querying an image, it is converted to a bitmap and displayed in a view.
