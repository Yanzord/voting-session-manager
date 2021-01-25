## Voting Session Manager

This project was created for a technical challenge and its README is divided in the following sections:

- [The Challenge](https://github.com/Yanzord/voting-session-manager/blob/master/README.md#the-challenge)
- [About It](https://github.com/Yanzord/voting-session-manager/blob/master/README.md#about-it)
- [Getting Started](https://github.com/Yanzord/voting-session-manager/blob/master/README.md#getting-started)
- [Documentation](https://github.com/Yanzord/voting-session-manager/blob/master/README.md#documentation)
- [Future Improvements](https://github.com/Yanzord/voting-session-manager/blob/master/README.md#future-improvements)

## The Challenge

In cooperatives, each member has one vote and decisions are taken in assemblies, by vote. From there, you need to create a back-end solution to manage these voting sessions. This solution must be executed in the cloud and promote the following functionalities through a REST API:
- Register a new agenda;
- Open a voting session on an agenda (the voting session must be open for a specified time in the opening call or 1 minute by default);
- Receive votes from members on agendas (votes are only 'Yes' / 'No'. Each member is identified by a unique id and can vote only once per agenda);
- Count the votes and give the result of the vote on the agenda.


For exercise purposes, the security of the interfaces can be abstracted and any call to the interfaces can be considered as authorized. The choice of language, frameworks and libraries is free (as long as it does not infringe use rights).

It is important that the agendas and votes are persisted and that they are not lost with the application restart.


### Bonus tasks
Bonus tasks are not mandatory, but we can evaluate other knowledge that you may have.

We always suggest that the candidate consider and delivers as far as he can do, considering his
level of knowledge and quality of delivery.
#### Bonus Task 1 - Integration with external systems
Integrate with a system that verifies, from the member's CPF, he can vote
- GET https://user-info.herokuapp.com/users/{cpf}
- If the CPF is invalid, an API will return HTTP Status 404 (Not found). You can use CPFs to generate valid CPFs;
- If the CPF is valid, an API returned by the user can (ABLE_TO_VOTE) or cannot (UNABLE_TO_VOTE) perform an operation
  Service return examples

#### Bonus Task 2 - Messaging and Queues
Information classification: Internal Use
The voting result needs to be informed for the remaining platform, this should preferably be done through messaging. When a voting session closes, post a message with the result of the vote.

#### Bonus Task 3 - Performance
Imagine that your application can be used in scenarios where there are hundreds of thousands of votes. It must behave in a performative manner in these situations;
- Performance tests are a good way to guarantee and observe how your application behaves.

#### Bonus Task 4 - API Version
How would you version an API for your application? What strategy to use?

## About It

The API code is pretty straightforward, it uses SpringBoot as its core framework, MongoDB for data persistence, Netflix Feign for 
external requests, Swagger for documentation and Docker for containerization.
I choose a relational approach for the relationship between the documents in the database, mainly to avoid data duplication and desync. 
As for the API versioning, it is done by URI path.
Currently the test coverage is about 86% of the lines.

## Getting Started

To run this project you'll need [Docker](https://docs.docker.com/desktop/) and [Docker Compose](https://docs.docker.com/compose/install/).

Clone this repo to your PC:

    git clone https://github.com/Yanzord/voting-session-manager.git

If you're using an unix OS with bash as a shell interpreter you should navigate to the project root folder and execute the following scripts to deploy and destroy the application environment:

    bash deploy.sh
    bash destroy.sh

If you can't execute the scripts you'll need a mongodb service running locally, then from the project root folder execute:

    ./gradlew build
    java -jar -Dspring.profiles.active=local build/libs/voting-session-manager-1.0-SNAPSHOT.war

## Documentation

Documentation is provided by Swagger. Get the application up and running and access the link bellow:

http://localhost:8080/swagger-ui.html

## Future Improvements

- Convert to a reactive approach;
- Performance tests;
- Improve test coverage by testing the app web layer;
- Implement fallbacks;
- Implement messaging.