# vertx-example-commonsemail

Small example using apache-commons-email to construct the email message and using vertx-mail-service to send it.

This is mostly a wrapper for the mail-service using code I had in the mail-service project before we decided to implement the mail formatter directly without the dependencies of commons-email and javamail.

The unit test shows how to use it, since most work is done by commons-email, it should be ok for most use cases.

If you have questions about the project, please send me a mail at alexlehm@gmail.com or drop by on the vert.x irc channel.
