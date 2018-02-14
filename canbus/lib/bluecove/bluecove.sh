# echo "*** install bluetooth"
# sudo apt-get install bluetooth bluez blueman
# echo "*** install bluetooth-dev"
# sudo apt-get install libbluetooth-dev
# echo "*** install ant"
# sudo apt-get install ant

echo "*** create workdir"
mkdir bluecove_work
cd bluecove_work

echo "*** build bluecove"
wget http://snapshot.bluecove.org/distribution/download/2.1.1-SNAPSHOT/2.1.1-SNAPSHOT.63/bluecove-2.1.1-SNAPSHOT-sources.tar.gz
tar -zxf bluecove-2.1.1-SNAPSHOT-sources.tar.gz
mv bluecove-2.1.1-SNAPSHOT bluecove
cd bluecove
ant all
jar cf ./target/bluecove-2.1.1-SNAPSHOT-sources.jar -C ./src/main/java/ .
jar uf ./target/bluecove-2.1.1-SNAPSHOT-sources.jar -C ./src/main/resources/ .
jar uf ./target/bluecove-2.1.1-SNAPSHOT-sources.jar -C ./src/main/c/ .
cd ..
rm bluecove-2.1.1-SNAPSHOT-sources.tar.gz

echo "*** build bluecove-gpl"
wget http://snapshot.bluecove.org/distribution/download/2.1.1-SNAPSHOT/2.1.1-SNAPSHOT.63/bluecove-gpl-2.1.1-SNAPSHOT-sources.tar.gz
tar -zxf bluecove-gpl-2.1.1-SNAPSHOT-sources.tar.gz
mv bluecove-gpl-2.1.1-SNAPSHOT bluecove-gpl
cd bluecove-gpl
ant all
jar cf ./target/bluecove-gpl-2.1.1-SNAPSHOT-sources.jar -C ./src/main/java/ .
jar uf ./target/bluecove-gpl-2.1.1-SNAPSHOT-sources.jar -C ./src/main/resources/ .
jar uf ./target/bluecove-gpl-2.1.1-SNAPSHOT-sources.jar -C ./src/main/c/ .
cd ..
rm bluecove-gpl-2.1.1-SNAPSHOT-sources.tar.gz

echo "*** build bluecove-emu"
wget http://snapshot.bluecove.org/distribution/download/2.1.1-SNAPSHOT/2.1.1-SNAPSHOT.63/bluecove-emu-2.1.1-SNAPSHOT-sources.tar.gz
tar -zxf bluecove-emu-2.1.1-SNAPSHOT-sources.tar.gz
mv bluecove-emu-2.1.1-SNAPSHOT bluecove-emu
cd bluecove-emu
ant all
jar cf ./target/bluecove-emu-2.1.1-SNAPSHOT-sources.jar -C ./src/main/java/ .
jar uf ./target/bluecove-emu-2.1.1-SNAPSHOT-sources.jar -C ./src/main/resources/ .
jar uf ./target/bluecove-emu-2.1.1-SNAPSHOT-sources.jar -C ./src/main/c/ .
cd ..
rm bluecove-emu-2.1.1-SNAPSHOT-sources.tar.gz

echo "*** clean workdir"
cd ..
# rm -R bluecove_work

