# vertx-example-commonsemail

Small example using apache-commons-email to construct the email message and using vertx-mail-service to send it.

The conversion is a bit hacky but it should be a good startingpoint for implementing mail generation with commons-email.

It's mostly a wrapper for mail-client using code I had in the mail-service project before we decided to implement the mail formatter directly without dependencies of commons-email and javamail.

The unit test shows how to use it, since most work is done by commons-email, it should be ok for most use cases.

If you have questions about the project, please send me a mail at alexlehm@gmail.com or drop by on the vert.x irc channel.
