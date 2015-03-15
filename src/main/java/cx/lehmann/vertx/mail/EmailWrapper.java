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

  private static Logger log=LoggerFactory.getLogger(EmailWrapper.class);
  
  private EmailWrapper() {
  }

  public static void sendMail(MailService service, Email commonsEmail, Handler<AsyncResult<JsonObject>> resultHandler) {
    MailMessage email = new MailMessage();

    // prefer bounce address over from address
    // currently (1.3.3) commons mail is missing the getter for bounceAddress
    // I have requested that https://issues.apache.org/jira/browse/EMAIL-146
    String fromAddr = commonsEmail.getFromAddress().toString();
    if (commonsEmail instanceof BounceGetter) {
      String bounceAddr = ((BounceGetter) commonsEmail).getBounceAddress();
      if (bounceAddr != null && !bounceAddr.isEmpty()) {
        fromAddr = bounceAddr;
      }
    }

    email.setBounceAddress(fromAddr);
    email.setFrom(commonsEmail.getFromAddress().toString());
    email.setTo(convertAddresses(commonsEmail.getToAddresses()));
    email.setCc(convertAddresses(commonsEmail.getCcAddresses()));
    email.setBcc(convertAddresses(commonsEmail.getBccAddresses()));

    service.sendMailString(email, createMailMessage(commonsEmail), resultHandler);
  }

  private static List<String> convertAddresses(List<InternetAddress> toAddresses) {
    List<String> addresses = new ArrayList<String>();
    for(InternetAddress a : toAddresses) {
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
