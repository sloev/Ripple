Testing
=======

I have only used informal testing in the develoment of Ripple.
What i mean by informal testing is that i defined a list of actions i did every time i flashed the app to the emulator and based on the outcome beign either: regression, accepted, failed, i debugged until the bugs went away.

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