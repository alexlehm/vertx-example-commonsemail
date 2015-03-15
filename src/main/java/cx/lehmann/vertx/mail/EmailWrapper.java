package cx.lehmann.vertx.mail;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mail.MailService;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.InternetAddress;

import org.apache.commons.mail.Email;

public class EmailWrapper {

  private static Logger log = LoggerFactory.getLogger(EmailWrapper.class);

  private EmailWrapper() {
  }

  public static void sendMail(MailService service, Email email,
      Handler<AsyncResult<JsonObject>> resultHandler) {
    MailMessage message = new MailMessage();

    // prefer bounce address over from address
    // currently (1.3.3) commons mail is missing the getter for bounceAddress
    // I have requested that https://issues.apache.org/jira/browse/EMAIL-146
    String fromAddr = email.getFromAddress().toString();
    if (email instanceof BounceGetter) {
      String bounceAddr = ((BounceGetter) email).getBounceAddress();
      if (bounceAddr != null && !bounceAddr.isEmpty()) {
        fromAddr = bounceAddr;
      }
    }

    message.setBounceAddress(fromAddr)
        .setFrom(email.getFromAddress().toString())
        .setTo(convertAddresses(email.getToAddresses()))
        .setCc(convertAddresses(email.getCcAddresses()))
        .setBcc(convertAddresses(email.getBccAddresses()));

    // TODO: createMailMessage is not async, this should be done in
    // in a executeBlocking block
    service.sendMailString(message, createMailMessage(email), resultHandler);
  }

  private static List<String> convertAddresses(List<InternetAddress> toAddresses) {
    List<String> addresses = new ArrayList<String>();
    for (InternetAddress a : toAddresses) {
      addresses.add(a.toString());
    }
    return addresses;
  }

  private static String createMailMessage(Email email) {
    email.setHostName("localhost");// TODO: why is this necessary?
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      email.buildMimeMessage();
      email.getMimeMessage().writeTo(bos);
      return bos.toString();
    } catch (Exception e) {
      log.error("cannot create mime message", e);
      return "";
    }
  }

}
