## rtag : Tuple Assisted Grouping for REDS ##


rtag enables a distributed system to perform self-organized configuration, monitoring, discovery, and recovery of its groups.
We applied a shared
data-space model (i.e. Tuple Space) not even to inform components about adaptations of the environment also to let them gather information, interact, and coordinate with each
other.

The key idea is to use a tuple space for clustering the network. The system is build on top of a spatially
distributed data space. Components can benefit from  simple APIs to define and push
new tuples in the tuple space and to locally sense both tuples and events associated with
changes in the tuple' distributed structures.

Transparently, the middleware allows components to subscribe
to data published both in the future and the past. For future publications,
uses the standard publish and subscribe messaging, while the past
information are stored in the tuple space and they represent part of
our current system configuration.