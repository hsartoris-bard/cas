---
layout: default
title: CAS - Passwordless Authentication
category: Authentication
---

# Passwordless Authentication

Passwordless Authentication is a form of authentication in CAS where passwords take the form of tokens that expire after a configurable period of time. 
Using this strategy, users are simply asked for an identifier (i.e. username) which is used to locate the user record that contains forms of contact such as email and phone
number. Once located, the CAS-generated token is sent to the user via the configured notification strategies (i.e. email, sms, etc) where the user is then expected to 
provide the token back to CAS in order to proceed. 

<div class="alert alert-info"><strong>No Magic Link</strong><p>
Presently, there is no support for magic links that would remove the task of providing the token back to CAS allowing the user to proceed automagically.
This variant may be worked out in future releases.</p></div>

In order to successfully implement this feature, configuration needs to be in place to contact account stores that hold user records who qualify for passwordless authentication. 
Similarly, CAS must be configured to manage issued tokens in order to execute find, validate, expire or save operations in appropriate data stores.

## Overview

Support is enabled by including the following module in the overlay:

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-passwordless-webflow</artifactId>
    <version>${cas.version}</version>
</dependency>
```

To see the relevant list of CAS properties, please [review this guide](../configuration/Configuration-Properties.html#passwordless-authentication).

## Account Stores

User records that qualify for passwordless authentication must be found by CAS using one of the following strategies. All strategies may be configured
using CAS settings and are activated depending on the presence of configuration values.

### Simple

This strategy provides a static map of usernames that are linked to their method of contact, such as email or phone number. It is best used
for testing and demo purposes. The key in the map is taken to be the username eligible for authentication while the value can either be an email
address or phone number that would be used to contact the user with issued tokens.

### LDAP

This strategy simply allows one to locate a user record in an LDAP directory. The record is expected to carry the user's phone number
or email address via configurable attributes. To see the relevant list of CAS 
properties, please [review this guide](../configuration/Configuration-Properties.html#passwordless-authentication).

Support is enabled by including the following module in the overlay:

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-passwordless-ldap</artifactId>
    <version>${cas.version}</version>
</dependency>
```

### Groovy

This strategy allows one to locate user records via a Groovy script. The body of the script may be defined as such:

```groovy
import org.apereo.cas.api.*

def run(Object[] args) {
    def username = args[0]
    def logger = args[1]
    
    logger.info("Locating user record for user $username")
    
    /*
     ...
     Locate the record for the given username, and return the result back to CAS.
     ...
    */
    
    def account = new PasswordlessUserAccount()
    account.setUsername(username)
    account.setEmail("username@example.org")
    account.setName("TestUser")
    account.setPhone("123-456-7890") 
    account.setAttributes(Map.of("...", List.of("...", "...")) 
    account.setMultifactorAuthenticationEligible(false)
    return account
}
```

### REST

This strategy allows one design REST endpoints in charge of locating passwordless user records. A successful execution of the endpoint  
would produce a response body similar to the following:

```json
{
  "@class" : "org.apereo.cas.api.PasswordlessUserAccount",
  "username" : "casuser",
  "email" : "cas@example.org",
  "phone" : "123-456-7890",
  "name" : "CASUser",        
  "multifactorAuthenticationEligible": false,
  "attributes":{ "lastName" : ["...", "..."] }
}
```

## Token Management

The following strategies define how issued tokens may be managed by CAS. 

### Memory

This is the default option where tokens are kept in memory using a cache with a configurable expiration period. Needless to say, this option 
is not appropriate in clustered CAS deployments inside there is not a way to synchronize and replicate tokens across CAS nodes.

### JPA

This strategy allows one to store tokens and manage their expiration policy using a relational database.

Support is enabled by including the following module in the overlay:

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-passwordless-jpa</artifactId>
    <version>${cas.version}</version>
</dependency>
```     

To see the relevant list of CAS 
properties, please [review this guide](../configuration/Configuration-Properties.html#passwordless-authentication).

### REST

This strategy allows one design REST endpoints in charge of managing tokens and their expiration policy entirely. 
CAS continues to generate tokens and the endpoint is only acting as a facade to the real token store, receiving tokens from CAS
in an encrypted fashion. 

The following operations need to be supported by the endpoint:

| HTTP Method | Description                               | Parameter(s)          | Response
|-------------|-------------------------------------------|-----------------------|--------------------------------
| `GET`       | Locate tokens for the user.               | `username`            | Token in the response body.
| `DELETE`    | Delete all tokens for the user.           | `username`            | N/A
| `DELETE`    | Delete a single token for the user.       | `username`, `token`   | N/A
| `POST`      | Save a token for the user.                | `username`, `token`   | N/A

### Messaging & Notifications

Users may be notified of tokens via text messages, mail, etc.
To learn more about available options, please [see this guide](../notifications/SMS-Messaging-Configuration.html)
or [this guide](../notifications/Sending-Email-Configuration.html).

## Multifactor Authentication Integration

Passwordless authentication can be integrated with [CAS multifactor authentication providers](../mfa/Configuring-Multifactor-Authentication.html). In this scenario,
once CAS configuration is enabled to support this behavior or the located passwordless user account is considered *eligible* for multifactor authentication,
CAS will allow passwordless authentication to skip its own *intended normal* flow (i.e. as described above with token generation, etc) in favor of 
multifactor authentication providers that may be available and defined in CAS.

This means that if [multifactor authentication providers](../mfa/Configuring-Multifactor-Authentication.html) are defined and activated, and defined 
[multifactor triggers](../mfa/Configuring-Multifactor-Authentication-Triggers.html) in CAS signal availability and eligibility of an multifactor flow for the given passwordless user, CAS will skip 
its normal passwordless authentication flow in favor of the requested multifactor authentication provider and its flow. If no multifactor providers 
are available, or if no triggers require the use of multifactor authentication for the verified passwordless user, passwordless 
authentication flow will commence as usual.

