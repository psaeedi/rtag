/***
 * * REDS - REconfigurable Dispatching System
 * * Copyright (C) 2003 Politecnico di Milano
 * * <mailto: cugola@elet.polimi.it> <mailto: picco@elet.polimi.it>
 * *
 * * This library is free software; you can redistribute it and/or modify it
 * * under the terms of the GNU Lesser General Public License as published by
 * * the Free Software Foundation; either version 2.1 of the License, or (at
 * * your option) any later version.
 * *
 * * This library is distributed in the hope that it will be useful, but
 * * WITHOUT ANY WARRANTY; without even the implied warranty of
 * * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * * General Public License for more details.
 * *
 * * You should have received a copy of the GNU Lesser General Public License
 * * along with this library; if not, write to the Free Software Foundation,
 * * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 ***/

package polimi.reds.broker.overlay;

import java.io.*;
import java.net.*;
import java.util.Enumeration;

/**
 * A transport using TCP to connect with neighboring brokers and standard Java serialization to
 * marshal and unmarshal data.
 */
public class TCPWorkingTransport extends AbstractTransport implements Runnable {
  public static final String PROTOCOL_ID = "reds-tcp";
  private static final String BEACON_SBJ = "__TCP_BEACON";
  private static int BEACONING_INTERVAL = 1000;
  private boolean running;
  private Thread acceptingThread;
  private int acceptingPort;
  private ServerSocket sSock;

  /**
   * Creates a new <code>TCPTransport</code> that can be used to open connections toward other
   * transports but does not accept incoming connections.
   * 
   * @throws IOException
   */
  public TCPWorkingTransport() throws IOException {
    this(-1);
  }

  /**
   * Creates a new <code>TCPTransport</code> capable of accepting new connections. The
   * <code>port</code> parameter specifies the port this transport accepts connections from.
   * 
   * @param port the port to use for accepting new connections.
   * @throws IOException
   */
  public TCPWorkingTransport(int port) {
    this.running = false;
    this.acceptingPort = port;
    this.acceptingThread = null;
    this.sSock = null;
    // determine my own address
    try {
    	for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
		    NetworkInterface intf = en.nextElement();
		    if (intf.isLoopback()) {
		    	continue;
		    }
		    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
		    	InetAddress addr = enumIpAddr.nextElement();
		    	if(addr instanceof Inet6Address) continue;
	            //if(addr.isLinkLocalAddress()||addr.isSiteLocalAddress()) continue;
	            addUrl("reds-tcp:"+addr.getHostAddress()+":"+acceptingPort);
		    }
		  }    	
    } catch(IOException ex) {
      logger.warning(ex+" determining the local address");
      addUrl("reds-tcp:localhost:"+acceptingPort);
    } finally {
      if(getUrls().length==0) addUrl("reds-tcp:localhost:"+acceptingPort);
    }
  }

  public synchronized void start() throws IOException {
    if(running) return;
    logger.fine("Starting "+this);
    running = true;
    if(acceptingPort!=-1) {
      sSock = new ServerSocket(acceptingPort);
      // Used to guarantee that the accepting thread does not remain blocked forever even when the
      // transport is stopped
      sSock.setSoTimeout(500);
      acceptingThread = new Thread(this);
      acceptingThread.setDaemon(true);
      acceptingThread.start();
    }
  }

  public void stop() throws IOException {
    synchronized(this) {
      if(!running) return;
      logger.fine("Stopping "+this);
      running = false;
      if(acceptingThread!=null) {
        // wait for the accepting thread to complete
        try {
          if(acceptingThread!=Thread.currentThread()) acceptingThread.join();
        } catch(InterruptedException ex) {
          logger.warning(ex+" waiting for accepting thread to stop");
        }
        acceptingThread = null;
        sSock.close();
        sSock = null;
      }
    }
    // close all open links
    for(Link l : getOpenLinks()) {
      l.close();
    }
  }

  public synchronized boolean isRunning() {
    return running;
  }

  @Override
  public void setBeaconing(boolean beaconing) {
    super.setBeaconing(beaconing);
    for(Link l : getOpenLinks()) {
      ((TCPLink) l).setBeaconing(beaconing);
    }
  }

  public boolean knowsProtocol(String protocol) {
    return protocol.equals(PROTOCOL_ID);
  }

  public Link openLink(String url) throws MalformedURLException, ConnectException,
      NotRunningException {
    TCPLink l = null;
    synchronized(this) {
      if(!running) throw new NotRunningException();
      String urlElements[] = url.split(":");
      if(urlElements.length!=3||!knowsProtocol(urlElements[0])) throw new MalformedURLException();
      logger.fine("Opening link toward "+url);
      String host = urlElements[1];
      int port = Integer.parseInt(urlElements[2]);
      Socket sock = null;
      try {
        sock = new Socket(host, port);
        l = new TCPLink(sock);
        l.setBeaconing(this.isBeaconing());
        addOpenLink(l);
      } catch(Exception ex) {
        throw new ConnectException();
      }
    }
    notifyLinkOpened(l);
    l.startReadingThread();
    return l;
  }

  public void run() {
    Socket sock;
    try {
      while(running) {
        try {
          sock = sSock.accept();
        } catch(SocketTimeoutException ex) {
          continue;
        }
        TCPLink l = new TCPLink(sock);
        l.setBeaconing(this.isBeaconing());
        addOpenLink(l);
        notifyLinkOpened(l);
        l.startReadingThread();
      }
    } catch(IOException ex) {
      logger.severe("I/O error accepting new connection");
      ex.printStackTrace(System.err);
    }
  }

  private class TCPLink implements Link, Runnable {
    private Socket sock;
    private REDSMarshaller marshaller;
    private REDSUnmarshaller unmarshaller;
    private Thread readingThread;
    private volatile boolean connected;
    private long lastReceivingTime;
    private long lastSendingTime;
    private Envelope sendingEnvelope;

    private TCPLink(Socket sock) throws IOException {
      this.sock = sock;
      marshaller = new REDSMarshaller(sock.getOutputStream());
      unmarshaller = new REDSUnmarshaller(sock.getInputStream());
      readingThread = null;
      sendingEnvelope = new Envelope();
      connected = true;
    }

    public void close() {
      synchronized(this) { // to synch with calls to send and with the run method
        if(!connected) return;
        logger.fine("Closing "+this);
        connected = false;
        try {
          sock.shutdownOutput();
        } catch(IOException ex1) {
          logger.warning("I/O error shutting down the output link for "+this);
        }
        removeOpenLink(this);
      }
      // Wait for the reading thread to terminate (it will notify listeners)
      try {
        if(readingThread!=null&&readingThread!=Thread.currentThread()) readingThread.join();
      } catch(InterruptedException ex) {
        logger.warning(ex+" waiting for the reading thread to end");
      }
    }

    public synchronized void send(String subject, Serializable data) throws IOException,
        NotConnectedException, NotRunningException {
      if(!connected) throw new NotConnectedException();
      if(!running) throw new NotRunningException();
      logger.fine("Sending msg \""+data+"\" to sbj \""+subject+"\" through "+this);
      sendingEnvelope.setSubject(subject);
      sendingEnvelope.setPayload(data);
      marshaller.reset();
      marshaller.writeObject(sendingEnvelope);
      marshaller.flush();
      lastSendingTime = System.currentTimeMillis();
    }

    public Transport getTransport() {
      return TCPWorkingTransport.this;
    }

    public boolean isConnected() {
      return connected;
    }

    public void run() {
      Envelope readEnvelope = null;
      while(true) {
        try {
          readEnvelope = (Envelope) unmarshaller.readObject();
          lastReceivingTime = System.currentTimeMillis();
        } catch(SocketTimeoutException ex) {
          if(!isBeaconing()) {
            logger.warning(ex+" thrown in non beaconing link."
                +" It could happen if just changed from beaconing to non-beaconing.");
          } else {
            // send beacon if necessary
            sendBeaconIfRequired();
            // check if the link crashed (too long time elapsed since last data was received)
            if((System.currentTimeMillis()-lastReceivingTime)>=BEACONING_INTERVAL*4) {
              killLink(null, "Too many beacons lost, considering the link as crashed");
              break;
            }
            continue;
          }
        } catch(ClassNotFoundException ex) {
          logger.warning(ex+" unmarshalling data");
          continue;
        } catch(EOFException ex) {
          synchronized(this) { // to synch with calls to close && send
            logger.fine(ex+" reading data, considering the link as closed");
            connected = false;
            try {
              sock.close();
            } catch(IOException ex1) {
              logger.warning("I/O error closing the socket for "+this);
            }
            removeOpenLink(this);
          }
          // notify listeners out of the synchronized block
          notifyLinkClosed(this);
          break;
        } catch(IOException ex) {
          killLink(ex, ex+" reading data, considering the link as crashed");
          break;
        }
        if(isBeaconing()) sendBeaconIfRequired();
        // process the received data
        if(readEnvelope.getSubject().equals(BEACON_SBJ)) {
          // do nothing
        } else notifyDataArrived(readEnvelope.getSubject(), this, readEnvelope.getPayload());
      }
    }

    private void killLink(Exception ex, String warningMsg) {
      synchronized(this) { // to synch with calls to close && send
        if(!connected) return;
        logger.warning(warningMsg);
        if(ex!=null) ex.printStackTrace();
        connected = false;
        try {
          sock.close();
        } catch(IOException ex1) {
          logger.warning("I/O error closing the socket for "+this);
        }
        removeOpenLink(this);
      }
      // notify listeners out of the synchronized block
      notifyLinkCrashed(this);
    }

    private void setBeaconing(boolean beaconing) {
      try {
        if(beaconing) {
          sock.setSoTimeout(BEACONING_INTERVAL);
          lastSendingTime = lastReceivingTime = System.currentTimeMillis();
        } else sock.setSoTimeout(0);
      } catch(SocketException ex) {
        logger.warning(ex+" set beaconing state to "+this);
      }
    }

    private void sendBeaconIfRequired() {
      long now = System.currentTimeMillis();
      synchronized(this) {
        if((now-lastSendingTime)>=BEACONING_INTERVAL) {
          logger.fine("Sending beacon through "+this);
          try {
            send(BEACON_SBJ, null);
          } catch(Exception ex) {
            logger.warning(ex+" sending beacon");
          }
        }
      }
    }

    private void startReadingThread() {
      readingThread = new Thread(this,"TCPLink.Reader");
      readingThread.setDaemon(true);
      readingThread.start();
    }

    public String toString() {
      return "TCPLink:"+getUrls()[0]+"<-->"+sock.getInetAddress().getHostAddress()+":"
          +sock.getPort();
    }
  }
}