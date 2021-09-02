# IBMMQSample
IBM MQ HA Sample

```console
mkdir lib
cd lib
curl -o com.ibm.mq.allclient-9.2.3.0.jar https://repo1.maven.org/maven2/com/ibm/mq/com.ibm.mq.allclient/9.2.3.0/com.ibm.mq.allclient-9.2.3.0.jar
curl -o javax.jms-api-2.0.1.jar https://repo1.maven.org/maven2/javax/jms/javax.jms-api/2.0.1/javax.jms-api-2.0.1.jar
cd ..

mkdir -p src/com/ibm/mq/samples/jms/
cd src/com/ibm/mq/samples/jms/
curl -o JmsPutGet.java https://raw.githubusercontent.com/ibm-messaging/mq-dev-samples/master/gettingStarted/jms/com/ibm/mq/samples/jms/JmsPutGet.java
cd ../../../../../../

javac -cp ./lib/com.ibm.mq.allclient-9.2.3.0.jar:./lib/javax.jms-api-2.0.1.jar src/com/ibm/mq/samples/jms/JmsPutGet.java
ls -l src/com/ibm/mq/samples/jms/
cd src
java -cp ../lib/com.ibm.mq.allclient-9.2.3.0.jar:../lib/javax.jms-api-2.0.1.jar:. com.ibm.mq.samples.jms.JmsPutGet
```
