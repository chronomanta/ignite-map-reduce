# ignite-map-reduce
Examples how to use distributed computing over Apache Ignite thin client 

## how to use the code?
To run components of the sample smoothly, you will need to have
- docker and docker-compose installed
- Java at least in version 1.8
- Apache Maven to build the project
- Java IDE (optional)

### building
```shell
mvn clean install
```

### creating Apache Ignite stack
Sample files for starting Apache Ignite are in [server_config](server_config) directory. 
You can use it directly or copy to directory you choose for a stack.
You will need to create ./libs folder inside for custom compiled code.

Example:
```shell
mkdir -p ./server_config/libs
```

### code deployment
To add project classes to Apache Ignite cluster, you need to copy the built artifact from [target](target) to libs folder.

Example:
```shell
cp ./target/ignite-map-reduce-1.0-SNAPSHOT.jar ./server_config/libs
```

### running the cluster
Go to the folder with stack files and run the stack using docker-compose

Example:
```shell
cd ./server_config
docker-compose up -d
```

### playing with Apache Ignite
Please use samples: 
- [LengthOfLongestWordMapReduce](./src/main/java/pl/jlabs/example/ignite/LengthOfLongestWordMapReduce.java)
- [LoadIris](./src/main/java/pl/jlabs/example/ignite/LoadIris.java)
- [TestIrisRecognition](./src/main/java/pl/jlabs/example/ignite/TestIrisRecognition.java)

To test the ML model, you need to load the iris data to the cluster first.

### stopping the cluster
Go to the folder with stack files and stop the stack using docker-compose

Example:
```shell
cd ./server_config
docker-compose down
```