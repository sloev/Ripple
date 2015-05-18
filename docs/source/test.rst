Testing
=======

I have only used informal testing in the develoment of Ripple.
What I mean by informal testing is that I defined a list of actions I did every time I flashed the app to the emulator and based 
on the outcome being either: regression, accepted, failed, I debugged until the bugs were fixed.

I used these actions during development to test the condition of the app:

* Open and close the app repetitively 
* Sign up and delete account
* Sign in and close app using task switch from android, open app and sign in again
* Sign up, close app, open app and sign in
* Sign in and
    - Open contact list and close contact list
    - Sign in, open contact list and add contact
    - Open contacts list, enable, disable contact
    - View MapFragment while a contact logs into another instance of the app. (check for communication)