package cx.lehmann.vertx.mail;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailService;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import java.util.Arrays;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/*
 * smtp client test using vertx unit
 */
/**
 * @author <a href="http://oss.lehmann.cx/">Alexander Lehmann</a>
 *
 */
@RunWith(VertxUnitRunner.class)
public class MailUnitTest {

  private static final Logger log = LoggerFactory.getLogger(MailUnitTest.class);

  Vertx vertx = Vertx.vertx();

  @Test
  public void testMail(TestContext context) throws AddressException, EmailException {
    log.info("starting");

    Async async = context.async();

    MailConfig mailConfig = new MailConfig("localhost", 1587);

    MailService mailService = MailService.create(vertx, mailConfig);

    Email email = new MySimpleEmail();

    email.setFrom("user@example.com")
      .setBounceAddress("sender@example.com")
      .setTo(Arrays.asList(new InternetAddress("user@example.net")))
      .setSubject("Test email")
      .setMsg("this is a message");

    EmailWrapper.sendMail(mailService, email, result -> {
      log.info("mail finished");
      if (result.succeeded()) {
        log.info(result.result().toString());
        async.complete();
      } else {
        log.warn("got exception", result.cause());
        context.fail(result.cause().toString());
      }
    });
  }

  @Before
  public void before(TestContext context) {
    log.info("starting smtp server");
    Async async = context.async();
    smtpServer = new TestSmtpServer(vertx, v -> async.complete());
  }

  @After
  public void after(TestContext context) {
    log.info("stopping smtp server");
    smtpServer.stop();
  }

  private TestSmtpServer smtpServer;
}
