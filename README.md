# vertx-example-commonsemail

Small example using apache-commons-email to construct the email message and using the vert.x mail service to send it.

This is mostly a wrapper for the mail-service using code I had in the mail-service project before we decided to implement the mail formatter directly without the dependencies of commons-email and javamail.

The unit test shows the bits that are implemented until now, the html and attachment handling is not yet done.

If you have questions about the project, please send me a mail via github or drop by on the vert.x irc channel.
