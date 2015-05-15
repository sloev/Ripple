Implementation
==============

Persistence
-----------

I decided to use a singleton pattern for persistence of data.

ApplicationSingleton
	Responsible for info in the current logged in user, the contact list and how to save/load this information from the preference manager.

It is implemented so that you ask them for an instance and if they internally have not populated their instance they will do so before returning it.

.. code-block:: java

    public static synchronized ApplicationSingleton getDataHolder() {
        if (dataHolder == null) {
            dataHolder = new ApplicationSingleton();
        }
        return dataHolder;
    }

The ApplicationSingleton uses json to save load and settings as a single string from the preference manager.
It packages all the contacts together with their *"UserDataStructure's"* and dumps them in a preference key.

Ripple will save current settings using json at a few occations:

1: Sign up
	Ripple will clear the current list of contacts and save an empy one.
	Ripple will also set the *"current user"* on the ApplicationSingleton which will also be saved.
2: Sign in
	Ripple will load the contacts list from json and set the *"current user"* on the ApplicationSingleton.
3: Contact list manipulations
	The contact list will be saved everytime a contact is added, enabled or disabled, currently the deletion of a contact is buggy and disabled. (see explanation in the *"delete contact bug"* section)
3: Delete account
	When you delete your account Ripple will clear all settings from the preference manager

Contacts
--------

All users are quickblox users. But quickblox users are designed to be personal and therefore have a lot of meta data i did'nt need. I also didn't want to serialize the quickblox user objects since i only needed a few of their fields and some extra fields they didn't provide.
So i chose to implement my own user data structure.

UserDataStructure
'''''''''''''''''

The UserDataStructure is a property holding class with a few functions mostly in the getter/setter category:
It has two fields:

.. code-block:: java

    private LatLng position;
    private IconGenerator iconGenerator = null;

The position is geolocation of the contact and the iconGegenerator is an instance of a Google Maps Utilities IconGenerator for customizing the look of the contact marker.
I used this specific library in order to easily change the color of each marker on the fly as well as generating the marker icon from a text string.

Contact list
''''''''''''

The contacts are stored in the ApplicationSingleton in a hashmap and an extra arraylist serves the purpose of assigning an index to each contact for use by the *"ContactListAdaptor"*.

.. code-block:: java

    private Map<Integer, UserDataStructure> userContacts = 
    	new HashMap<Integer, UserDataStructure>();

    private List<Integer> indexToUserId = 
    	new ArrayList<Integer>();

  The ContactListAdaptor is written so instead of a fixed set of entries it is going through the IndexTouserId arraylist every time it is updated.


Receive locations
-----------------

The PrivateChatManager class is responsible for receiving all chat messages. It will discrimate incomming messages based on their appearance in the contact list.
If the sender of an incomming message is a contact then the PrivateChatManager will update that specific contact with its new location info.
It will also update the contact with the username of the contact since this is not known when you add a contact using only the contact user-id.

Update map markers and transmit location
----------------------------------------

The MapFragment executes a runnable when it receives focus. This runnable will reschedule itself on a fixed interval.
The runnable is called *"locationsUpdatedRunnable"* and it is responsible for the following actions:

Update the map view
'''''''''''''''''''

It will start by clearing all markers from the map. Then it will go through all contacts and add a marker if they fulfil a list of criteria:

* The contact should have sended a location update within a defined period of time, else regarded as offline)

Three colors for the markers are used:

Red
	The color of your own marker
Green
	The color of a contact marker with fresh location update.
Yellow
	The color of a contact marker with old location update

The runnable will focus the map view so it is centered and zoomed in such a way that the markers are all viewable and that they are not overlapping with the edge of the map, (padding)

Send position
'''''''''''''

The runnable is also responsible for transmitting the current location of the logged in user.
The runnable will go through all contacts and send a *ChatMessage* to them if they are enabled.

The ChatMessage is a string formatted LatLon position.


Delete contact bug
------------------

When i had rewritten the application from a focus on activities to the use of a container activity i didn't test for regression bugs on the *"delete contact"* functionality.
The result is that i have created a scenario where the implementation of such a feature will require a reqrite of major parts of the application.

As You can see in the preceding sections im referring to the Contacts hashmap and the indexToUserId from a lot of different threads.
This is no problem if im just appending to these data structures but if im removing from them then problems arise.
In general what i experienced could be described as a deadlock problem. At the same time these three actions could happen:

* The ContactListAdaptor tries to remove a contact from the userContacts hashmap and reorganize the indexToUserId arraylist.
* The PrivateChatManager tries to update the same contact with a new received update
* The MapFragment goes through the whole userContacts hashmap to update the map and transmit current location to contacts.
  
These three actions cannot happen together  and the result is long stack traces and asynchronous debugging for nights without end.

The solution
''''''''''''

I propose two solutions to solve the deadlock issue at hand:

1. Implement the monitor design pattern so the userContacts and indexToUserId become governed by a monitor and only one 
   entity can manipulate it at any given time.
2. Change the application so all resources and threads used during map view are freed when entering the contact list view.
   This would involve stopping the "PrivateChatManager" from listening and the "locationsUpdatedRunnable" from running. This would propably be the easiest solution.


