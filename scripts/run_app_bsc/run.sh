rm -rf ./lib
rm -rf ./logs
rm ethparser.jar
mvn install -Dmaven.test.skip=true -f ./../../pom.xml
cp -R ./../../dist/lib/. ./lib
cp ./../../dist/ethparser.jar ./ethparser.jar
java -Xmx1g -Dspring.config.location=./../application-bsc.yml,run_config.yml -cp ethparser.jar pro.belbix.ethparser.Application
