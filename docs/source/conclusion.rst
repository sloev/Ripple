Conclusion
==========

I am quite happy with my app even if it didn't fulfill my ambitions.
I started out with the project with experience from developing in java and xcode for iphone apps and thought this class would be easy but I was wrong.

The Android SDK is very fragmented because of their, admittedly nice, insisting on providing support for older API levels.
This made it really hard to follow tutorials and code examples found on the internet because small changes to import statements could ruin the compiling of such, 
like examples depending on support libraries.

However I came up to speed and implemented the core of my ambitions.
All Requirements except R7 and R8 is implemented and very functional. You can create accounts, login, add, enable, disable contacts, view them on the auto zoomed map etc. 

Process
-------

I think I made a good decision to start out early on since I had problems getting to know the Android SDK.
I used github for version control even though the use was sparse and mostly for marking some progress as well as a history of "undo's".
I ended of with a good weight between research, programming and documentation.
I wrote all my documentation using RestructuredText mark-up so I could generate the PDF from structured text instead of doing layout. 
This gave me a fast documentation workflow where I was not harassed by weird behaviour of an Office Suite.
The source of both my documentation and the app are at the github repository. [#repository]_

Unmet requirements
-----------------

R7
''

I used too much time organizing my project into a more maintainable fragment solution and in the end there was not time left to implement encryption. 
Encryption is not an easy task and the java libraries for doing OTR [#otr_lib]_ would take much time to incorporate into using the Quickbox BaaS.

R8
''

As told about in the implementation section I had a deadlock bug in my application and the solution was outside the time frame of the project.

.. rubric:: Footnotes

.. [#repository] http://github.com/sloev/ripple
.. [#otr_lib] https://code.google.com/p/otr4j/wiki/QuickStart