#Overlay Network
# Introduction #

Our Universe overlay management protocols are inspired by proximity-based
groups and by the group abstractions developed within the A-3 initiative.
A-3 provides an architectural style and a supporting middleware for the development of high-volume and highly-volatile distributed component-based systems.
A-3 allows designers to concentrate on groups of components instead of on single
components, so that they can reason on entities that are less dynamic, and less
prone to enter or leave the system unexpectedly.
In A-3 groups are created by aggregating components that have similar
application-specific needs. Each group contains a single supervisor component
and multiple follower components. Supervisors represent local-management points
that can be used to govern the application’s local behavior. A-3 also allows groups
to be composed by allowing components to belong to more than one group at
a time. This gives A-3 designers the means to tackle system-wide reasoning and
management in a compositional fashion, by focusing first on smaller and more
digestible problems. One of A-3’s most important characteristics is that the
components in a group are updated every time there is a change in the members of the group. This provides supervisors with crucial information for their
management logic, and allows followers to react to supervisor failures.



# Details #

In \trunk\peersim-rtag:
We have 5 protocols and the protocol stack is:
  * 5 - Grouping
  * 4 - Routing
  * 3 - UniverseProtocol
  * 2 - TupleSpaceProtocol
  * 1 - MockChannel

All above protocols are **[PeerSim](http://peersim.sourceforge.net/)-based protocols**

# Configuration file #

**protocol.geolocation it.polimi.peersim.protocols.GeoLocation:**

generating random (x,y) for each peer;

**protocol.discovery it.polimi.peersim.protocols.DiscoveryProtocol:**

the communication range should be define here;
also the limitation in the neighborhood list should be adjusted here.

**protocol.channel it.polimi.peersim.protocols.MockChannel:**

this protocol is simulate a 1 to 1 transport protocol such as udp, tcp, zigbee or Bluetooth. This can be replaced with any transport protocol in real scenarios;

**protocol.tuplespace it.polimi.peersim.protocols.TupleSpaceProtocol:**

creates a tuple space as a shared memory for messages.

**protocol.universe it.polimi.peersim.protocols.UniverseProtocol**

this is our overlay network;
the designer setting, thresholds and rates should be adjusted here;

**protocol.routing it.polimi.peersim.protocols.RoutingProtocol**

this protocol is still under implementation;

**protocol.grouping it.polimi.peersim.protocols.grouping.GroupingProtocol**

this protocol is the actual A-3, that creates logical group on top of the overlay network;

We have some initializers , there are controllers to initialize our universe, geolocation, and discovery protocol.