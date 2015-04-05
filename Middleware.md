# A-3 (_it.polimi_) #
**A-3**, is a reconfigurable
middleware for designing and implementing high-volume and
highly volatile distributed systems.

### Introduction ###

Traditional distributed systems perform distribution relying upon stable network infrastructures where hosts are permanently connected to the main network through high-bandwidth and high-quality links. However, for mobile and
pervasive systems most of the distribution occurring among embedded devices
communicating with each other through radio medium, lacks of such reliable
communication links and it is thus more prone to congestion, collision, and interference.
Publish/subscribe (P/S) is an appealing communication primitive for such
networks due to the loosely coupled interaction between the publishers and subscriber.

Pervasive distributed systems are usually supported by a middleware that is
in charge of keeping track of subscriptions and of dispatching the published messages to the interested subscribers. Middleware with multi layers can be installed on every node of a distributed system to provide a homogeneous high-level interface to applications for many aspects of a distributed computing environment, such as communication, concurrency, synchronization, replication, persistence,and access control.

The coordination is easier if, instead of attempting to coordinate large amounts of elements, the designer can concentrate on coordinating “groups” of elements that are less dynamic in their
behavior. Elements can be grouped together for a number of reasons: because
they are conceptually similar, because they have common goals, because they
are physically close, etc. Once the elements have been grouped, the designer
can exploit special communication abstractions, receive updated views of the
elements that are in the group, and perform state management.


For more information about A-3 please read the A-3 papers:
  * L. Baresi, S. Guinea, A-3: Self-Adaptation Capabilities through Groups and Coordination. In Proceedings of India Software Engineering Conference (ISEC) , pages 11–20, India, 2011.

  * L. Baresi, S. Guinea, A-3: an Architectural Style for Coordinating Distributed Components. In Proceedings of IEEE/IFIP Conference on Software Architecture (WICSA), Colorado, 2011

  * S. Guinea, P. Saeedi: Coordination of Distributed Systems through Self-Organizing Group Topologies, In: Proc. ACM/IEEE Inter. Symposium of Software Eng. for Self-Adaptive Systems (SEAMS), 2012. (to appear)



## RTAG: An Extension to A-3 ##

The A-3 middleware implementation is completely decentralized, and is built as an extension to [REDS](http://zeus.ws.dei.polimi.it/reds/), a distributed
publish and subscribe message dispatching middleware.

REDS implements distributed algorithms that support
the dynamic reconfiguration of its infrastructure, making it
resilient to changes in the infrastructure’s own topology. In
A-3 we employ REDS for group communication, and for
managing the group topology. In our previous version of
[A-3](http://code.google.com/p/a3-polimi/),  [JGroups ](http://www.jgroups.org/)framework creates a channel in which
all the participants were connected with each other.

Although REDS is more capable in managing a dynamic
network and JGroups is superior in terms of multicast,
however, if are used together they duplicate the number
of active links. We extend A-3 middleware to be able to
reduce the active links and avoid bottlenecks.

The main
extension we introduce is a distributed tuple space that keeps
track of how the system’s application and coordination group
topologies change as elements enter or leave the system.
This is instrumental, since it allows for various optimizations
in how the middleware searches for a group in which to add
a new element, in how it decides to create a new group and
compose it with the rest of the system, and in finding the
correct destinations for when application messages are sent.
It is also key in providing A-3’s views and state management
features. The tuple space is implemented using the [LighTS ](http://lights.sourceforge.net/)
framework.