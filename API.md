# Application Programming Interface (API) #

rtag provides
a consistent APIs for application developers to make the developed
applications independent across underlying communication protocols.

There are two main tasks in the development of an A-3 system. First we
need to define what groups should exist within the system, and second, we need
to define our components and their behaviors.

To define a component we start
with a map of application-specic keys and values. This map is used by the
middleware to discover in what groups the component belongs. Then we code
its behaviors using the APIs provided to us by the middleware.

## How to generate APIs ##

package:it.polimi.rtag.app
there are 5 classes that enable us to generate APIs and invoke the system.
The designer needs to extend the AbstractApp.java for each API.


TupleSpaceManager will internally manage the APIs:
@class TupleSpaceManager
```xml

public void storeHandleAndForward(TupleMessage message, NodeDescriptor sender) {
if (message.isExpired()) {
// the tuple is expired
System.out.println(message + "is expired. skipping");
return;
}

ITuple tuple = createTuple(message.getScope(),
new Long(message.getExpireTime()), message.getRecipient(),
message.getSubject(), message.getCommand(), message);

if (containsMessage(tuple)) {
// Already in tuple space
return;
}

System.out.println("storeHandleAndForward " + message.getSubject() + " " +
message.getCommand());

try {
tupleSpace.out(tuple);
} catch (TupleSpaceException e) {
e.printStackTrace();
}
switch (message.getScope()) {
case NODE: {
if (message instanceof TupleNodeNotification) {
handleNodeMessage(
(NodeDescriptor)message.getRecipient(),
(TupleNodeNotification)message,
sender);
} else if (message instanceof TupleMessageAck) {
handleNodeMessageAck(
(NodeDescriptor)message.getRecipient(),
(TupleMessageAck)message,
sender);
} else {
if (application != null) {
application.handleMessageReceived(sender, message);
}
}
break;
}
}
```