package cx.lehmann.vertx.mail;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;

/*
 * really dumb mock test server that just replays a number of lines
 * as response. this doesn't check any conditions at all.  
 */
public class TestSmtpServer {

  private NetServer netServer;
  private String answers;

  /*
   * set up server with a default reply
   * that works for EHLO and no login
   * with one recipient
   */
  public TestSmtpServer(Vertx vertx, Handler<Void> handler) {
    setAnswers("220 example.com ESMTP",
        "250-example.com",
        "250-SIZE 1000000",
        "250 PIPELINING",
        "250 2.1.0 Ok",
        "250 2.1.5 Ok",
        "354 End data with <CR><LF>.<CR><LF>",
        "250 2.0.0 Ok: queued",
        "221 2.0.0 Bye");
    startServer(vertx, handler);
  }

  private void startServer(Vertx vertx, Handler<Void> finished) {
    NetServerOptions nsOptions = new NetServerOptions();
    nsOptions.setPort(1587);
    netServer = vertx.createNetServer(nsOptions);

    netServer.connectHandler(socket -> {
      socket.write(answers);
      // wait 10 seconds for the protocol to finish
      vertx.setTimer(10000, v -> socket.close());
    });
    netServer.listen(v -> finished.handle(null));
  }

  public void setAnswers(String answers) {
    this.answers = answers;
  }

  public void setAnswers(String... answers) {
    this.answers = String.join("\r\n", answers) + "\r\n";
  }

  public void stop() {
    if (netServer != null) {
      netServer.close();
      netServer = null;
    }
  }

}
