package edu.snu.cms.remon.collector;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;

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
@org.eclipse.jetty.websocket.api.annotations.WebSocket(maxTextMessageSize = 64 * 1024)
public class RemonWebSocket implements AutoCloseable {
  private static final Logger LOG = Logger.getLogger(RemonWebSocket.class.getName());
  private ByteBuffer msg;
  private final CountDownLatch closeLatch;
  boolean isDone = false;

  @SuppressWarnings("unused")
  private Session session;

  public RemonWebSocket() {
    this.closeLatch = new CountDownLatch(1);
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
      while (!isDone) {
        Future<Void> fut;
        fut = session.getRemote().sendBytesByFuture(msg);
        fut.get(2, TimeUnit.SECONDS);
        this.wait();
      }
      // When user calls {@code close()} then close the session.
      session.close();
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public synchronized void sendMessage(final ByteBuffer msg) {
    if (session != null) {
      session.getRemote().sendBytesByFuture(msg);
    }
  }

  @Override
  public synchronized void close() {
    isDone = true;
    this.notify();
  }
}