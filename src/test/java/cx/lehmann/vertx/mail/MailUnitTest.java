package cx.lehmann.vertx.mail;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import java.util.Arrays;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;
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
  public void testMail(TestContext context) throws AddressException,
      EmailException {
    Async async = context.async();

    MailConfig mailConfig = new MailConfig("localhost", 1587);

    MailClient mailService = MailClient.createShared(vertx, mailConfig);

    Email email = new SimpleEmail();

    email.setFrom("user@example.com").setBounceAddress("sender@example.com")
        .setTo(Arrays.asList(new InternetAddress("user@example.net")))
        .setSubject("Test email").setMsg("this is a message");

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

  @Test
  public void mailHtml(TestContext context) throws AddressException,
      EmailException {
    Async async = context.async();

    MailConfig mailConfig = new MailConfig("localhost", 1587);

    MailClient mailService = MailClient.createShared(vertx, mailConfig);

    HtmlEmail email = new HtmlEmail();

    email.setFrom("user@example.com")
        .setTo(Arrays.asList(new InternetAddress("user@example.net")))
        .setSubject("Test email");
    email.setHtmlMsg("this is a message").setTextMsg("this is a text part");

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

  @Test
  public void mailAttachment(TestContext context) {
    try {
      log.info("starting");

      Async async = context.async();

      MailConfig mailConfig = new MailConfig("localhost", 1587);

      MailClient mailService = MailClient.createShared(vertx, mailConfig);

      EmailAttachment attachment = new EmailAttachment();
      attachment.setPath("logo-white-big.png");
      attachment.setDisposition(EmailAttachment.ATTACHMENT);
      attachment.setDescription("vert.x logo");
      attachment.setName("logo-white-big.png");

      HtmlEmail email = new HtmlEmail();
      email.setFrom("user@example.com")
          .setTo(Arrays.asList(new InternetAddress("user@example.net")))
          .setSubject("attachment mail")
          .setMsg("This message contains an attachment");

      // attach() may not be safe to use in an async program since it uses
      // File.exists()
      email.attach(attachment);
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
    } catch (Exception e) {
      log.warn("Exception", e);
      context.fail("got exception");
    }
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
