Introduction
============

Project start
-------------

This report documents the creation of an Android app for personal location sharing. I have often found
myself in situations where I needed the knowledge of real time updated locations of other individuals.
The cases could be a festival where I would be building stages for live performance. You often 
need to know the location of Your supervisor so you can evaluate if you can reach him within minutes or tens of minutes.
Normally this would be done by sending text messages but I wanted something that automatically would ping my location to my friends and vice versa.

Ripple is an app that seeks to fulfil exactly this through transmitting my location and showing the locations of my friends and myself on a map.

Problem formulation
-------------------

I want to create an app, Ripple, that lets people share their locations in a fixed frequency.
Ripple has to be structured as a container activity with different fragments occupying the container view.
Ripple will use the XMPP protocol for transmission of locations and it will use the Quickblox as a BAAS and xmpp server.
Ripple will focus on decoupled identities in such a way that your Ripple logins are disposable and decoupled from
your established personal accounts like Facebook etc. This will reduce the chance of a being compromised as
you can easily dump your account and create a new one.
Ripple will seek to be secure by letting you decide which contacts you want to transmit to and a stretch
goal is to implement Off-The-Record encryption on all XMPP traffic.
Ripple shall plot all locations of enabled contacts on a map and all markers will be colored according to the
time since their last location ping. If an enabled contact doesn't transmit pings for a certain period of time it will
automatically be removed from the map for less cluttering.

Requirements
------------


+-------+------------------------------------------------------------------------------+
| Name  |Description                                                                   |
+=======+==============================================================================+
|R1     |Use Quickblox BaaS SDK for XMPP and user account managing                     |
+-------+------------------------------------------------------------------------------+
|R2     |Use a container activity and fragments                                        |
+-------+------------------------------------------------------------------------------+
|R3     |Option to dispose account and create new account                              |
+-------+------------------------------------------------------------------------------+
|R4     |Add contacts to contact list and enable/disable transmission to them          |
+-------+------------------------------------------------------------------------------+
|R5     |Automatically zoom in and show all enabled contacts on                        |
|       |map                                                                           |
+-------+------------------------------------------------------------------------------+
|R6     |Show all contacts on a map, if a contact does not ping its location within    |
|       |the allowed time frame it will be removed from the map. Within the time frame |
|       |a color change will represent the freshness of its location ping.             |
+-------+------------------------------------------------------------------------------+
|R7     |Enable end to end encryption using Off-The-Record encryption                  |
+-------+------------------------------------------------------------------------------+
|R8     |Delete contacts                                                               |
+-------+------------------------------------------------------------------------------+
|R9     |Brug lister og adaptere, netværkskommunikation, SharedPreferences             |
+-------+------------------------------------------------------------------------------+

.. raw:: pdf

   PageBreak
