### dev-objecttives-2015 ###

## Authentication, Authorization, and Fine-grained Access Control ##

Securing an application by user and role is easy, but what if we need
fine-grained control?  I'm talking about the kind'a thing github does
where you own a repo, and invite others to contribute with varying
levels of permissions.

We'll discuss...

- The distinction between authentication, authorization, and
  fine-grained security.
- Ways to make it easier to discuss this kind of security with our
  clients.
- The risks of ignoring this security problem
- Approaches to solving this problem in a way that minimizes the
  complexity of the code.

Though not required to understand this topic, it would help if the
attendees have at least attempted to build web applications with
non-trivial security requirements.

Along the way we'll walk through a simple grails/mongoDB application as
we add authentication, authorization, and fine-grained access control.
Knowledge of grails, groovy, mongo is not required, though there will be
some code examples.
