rm -rf ./lib
rm -rf ./logs
rm ethparser.jar
mvn install -Dmaven.test.skip=true -f ./../../pom.xml
cp -R ./../../dist/lib/. ./lib
cp ./../../dist/ethparser.jar ./ethparser.jar
java -Xmx1g -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 -Dspring.config.location=./../application.yml,run_config.yml -cp ethparser.jar pro.belbix.ethparser.utils.recalculation.AppUtils
