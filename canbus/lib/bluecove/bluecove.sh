# echo "*** install bluetooth"
# sudo apt-get install bluetooth bluez blueman
# echo "*** install bluetooth-dev"
# sudo apt-get install libbluetooth-dev
# echo "*** install ant"
# sudo apt-get install ant

echo "*** environment variables"
JAVA_HOME=/opt/jdk1.8.0_151
PATH=$PATH:$JAVA_HOME/bin

# https://excellmedia.dl.sourceforge.net/project/bluecove/BlueCove/2.1.0/bluecove-gpl-2.1.0-sources.tar.gz
# http://snapshot.bluecove.org/distribution/download/2.1.1-SNAPSHOT/2.1.1-SNAPSHOT.63/
BLUECOVE_HOST=http://snapshot.bluecove.org/distribution/download
BLUECOVE_VERSION=2.1.1-SNAPSHOT
BLUECOVE_SNAPHOT_VERSION=$BLUECOVE_VERSION.63
echo "JAVA_HOME:                " $JAVA_HOME
echo "BLUECOVE_VERSION:         " $BLUECOVE_VERSION
echo "BLUECOVE_SNAPHOT_VERSION: " $BLUECOVE_SNAPHOT_VERSION

echo "*** create workdir"
mkdir bluecove_work
cd bluecove_work

echo "*** build bluecove"
wget $BLUECOVE_HOST/$BLUECOVE_VERSION/$BLUECOVE_SNAPHOT_VERSION/bluecove-$BLUECOVE_VERSION-sources.tar.gz
tar -zxf bluecove-$BLUECOVE_VERSION-sources.tar.gz
mv bluecove-$BLUECOVE_VERSION bluecove
cd bluecove
ant all
jar cf ./target/bluecove-$BLUECOVE_VERSION-sources.jar -C ./src/main/java/ .
jar uf ./target/bluecove-$BLUECOVE_VERSION-sources.jar -C ./src/main/resources/ .
jar uf ./target/bluecove-$BLUECOVE_VERSION-sources.jar -C ./src/main/c/ .
cd ..
rm bluecove-$BLUECOVE_VERSION-sources.tar.gz

echo "*** build bluecove-gpl"
wget $BLUECOVE_HOST/$BLUECOVE_VERSION/$BLUECOVE_SNAPHOT_VERSION/bluecove-gpl-$BLUECOVE_VERSION-sources.tar.gz
tar -zxf bluecove-gpl-$BLUECOVE_VERSION-sources.tar.gz
mv bluecove-gpl-$BLUECOVE_VERSION bluecove-gpl
cd bluecove-gpl
ant all
jar cf ./target/bluecove-gpl-$BLUECOVE_VERSION-sources.jar -C ./src/main/java/ .
jar uf ./target/bluecove-gpl-$BLUECOVE_VERSION-sources.jar -C ./src/main/resources/ .
jar uf ./target/bluecove-gpl-$BLUECOVE_VERSION-sources.jar -C ./src/main/c/ .
cd ..
rm bluecove-gpl-$BLUECOVE_VERSION-sources.tar.gz

echo "*** build bluecove-emu"
wget $BLUECOVE_HOST/$BLUECOVE_VERSION/$BLUECOVE_SNAPHOT_VERSION/bluecove-emu-$BLUECOVE_VERSION-sources.tar.gz
tar -zxf bluecove-emu-$BLUECOVE_VERSION-sources.tar.gz
mv bluecove-emu-$BLUECOVE_VERSION bluecove-emu
cd bluecove-emu
ant all
jar cf ./target/bluecove-emu-$BLUECOVE_VERSION-sources.jar -C ./src/main/java/ .
jar uf ./target/bluecove-emu-$BLUECOVE_VERSION-sources.jar -C ./src/main/resources/ .
jar uf ./target/bluecove-emu-$BLUECOVE_VERSION-sources.jar -C ./src/main/c/ .
cd ..
rm bluecove-emu-$BLUECOVE_VERSION-sources.tar.gz

echo "*** clean workdir"
cd ..
# rm -R bluecove_work

