package cx.lehmann.vertx.mail;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mail.MailResult;
import io.vertx.ext.mail.MailClient;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.mail.Email;

public class EmailWrapper {

  private static Logger log = LoggerFactory.getLogger(EmailWrapper.class);

  private EmailWrapper() {
  }

  public static void sendMail(MailClient client, Email email,
      Handler<AsyncResult<MailResult>> resultHandler) {
    MailMessage message = new MailMessage();

    // prefer bounce address over from address
    String fromAddr = email.getFromAddress().toString();
    String bounceAddr = email.getBounceAddress();
    if (bounceAddr != null && !bounceAddr.isEmpty()) {
      fromAddr = bounceAddr;
    }

    message.setBounceAddress(fromAddr)
        .setFrom(email.getFromAddress().toString())
        .setTo(convertAddresses(email.getToAddresses()))
        .setCc(convertAddresses(email.getCcAddresses()))
        .setBcc(convertAddresses(email.getBccAddresses()));

    // TODO: createMailMessage is not async, this should be done in
    // in a executeBlocking block
    String mailText = createMailMessage(email).replaceAll("\r", "");
    MultiMap headers = getHeaders(email);
    // we only need the body, currently MailMessage considers \t
    // as char that has to be encoded, we change that to space
    // which is a bit hacky
    String mailBody = mailText.substring(mailText.indexOf("\n\n")+2)
        .replace('\t', ' ');
    // set headers without adding any in MailEncoder
    message.setHeaders(headers);
    // set encoded message as text, since we override the headers
    // it will end up as mail body
    message.setText(mailBody);
    client.sendMail(message, resultHandler);
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

  @SuppressWarnings("unchecked")
  private static MultiMap getHeaders(Email email) {
    CaseInsensitiveHeaders headers = new CaseInsensitiveHeaders();
    Enumeration<Header> headerEnum;
    try {
      headerEnum = email.getMimeMessage().getAllHeaders();
    } catch (MessagingException ex) {
      log.error("cannot read headers", ex);
      return null;
    }
    while(headerEnum.hasMoreElements()) {
      Header header = headerEnum.nextElement();
      headers.add(header.getName(), header.getValue());
    }
    return headers;
  }
  
}
