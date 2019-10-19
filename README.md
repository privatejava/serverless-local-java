# Serverless Local Java

[![Build Status](https://travis-ci.com/privatejava/serverless-local-java.svg?branch=master)](https://travis-ci.com/privatejava/serverless-local-java)


Serverless Local Java is a cli tool for running serverless java application using docker.

  - Lambda execution
  - Fully containerized

### Prerequisite 
  - Java 8 
  - Docker 
  - Maven 

### Tech

Dillinger uses a number of open source projects to work properly:

* [Spark Java] - Spark Core
* [Apache Commons] - Commandline arguments parsing

### Installation

Dillinger requires [Node.js](https://nodejs.org/) v4+ to run.

Install the dependencies and devDependencies and start the server.

```sh
$ mvn clean install
$ echo "alias jsls='java -jar $(pwd)/target/serverless-local-java.jar'" >> ~/.bashrc 
```

### Development

Want to contribute? Great!

