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

Tool uses a number of open source projects to work properly:

* [Spark Java] - Spark Core
* [Apache Commons] - Commandline arguments parsing

### Installation

Install the dependencies and devDependencies and start the server.

```sh
$ mvn clean install
$ echo "alias jsls='java -jar $(pwd)/target/serverless-local-java.jar'" >> ~/.bashrc 
```


### Examples

Lets try with the `examples/` folder where you can test it locally. 

```sh
cd examples/simple-api
mvn clean install
./run.sh
```

You should have a server running in `4567` port . Now lets send a simple curl request.
```sh
curl -d '{"name":"Tom"}' http://localhost:4567/api/simple-api
```
It should print like this:   
```
{"message":"Hello Tom"}
```

If you are using any IDE like Netbeans, IntelliJ which auto generates the classes for you and this tool will automatically pickup the new classes and no need to re-run. You can use your own lambda layers with custom docker image to run your lambda.  



### Development

Want to contribute? Great!

