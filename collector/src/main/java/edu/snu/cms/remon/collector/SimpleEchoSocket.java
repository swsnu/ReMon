package edu.snu.cms.remon.collector;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Basic Echo Client Socket to connect to the Monitor server.
 * When Collector has received a message from one evaluator,
 * it connects to the Monitor and emit the data in JSON format.
 */
@WebSocket(maxTextMessageSize = 256 * 1024)
public class SimpleEchoSocket {
  // TODO : Persist the connection and hold the socket object. Send the data via existing socket object.
  private static final Logger LOG = Logger.getLogger(SimpleEchoSocket.class.getName());
  private ByteBuffer msg;
  private final CountDownLatch closeLatch;

  @SuppressWarnings("unused")
  private Session session;

  public SimpleEchoSocket(final ByteBuffer msg) {
    this.closeLatch = new CountDownLatch(1);
    this.msg = msg;
  }

  public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
    return this.closeLatch.await(duration, unit);
  }

  @OnWebSocketClose
  public void onClose(int statusCode, String reason) {
    LOG.log(Level.SEVERE, "Connection closed: {0} - {1}", new Object[]{statusCode, reason});
    this.session = null;
    this.closeLatch.countDown();
  }

  @OnWebSocketConnect
  public void onConnect(Session session) {
    LOG.log(Level.INFO, "Got connect: {0}", session);
    this.session = session;
    try {
      Future<Void> fut;
      fut = session.getRemote().sendBytesByFuture(msg);
      fut.get(1, TimeUnit.SECONDS);
//      session.getRemote().sendBytes(msg);
      session.close(StatusCode.NORMAL, "I'm done");
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  @OnWebSocketMessage
  public void onMessage(String msg) {
    System.out.printf("Got msg: %s%n", msg);
  }
}