#!/bin/sh
javac -d classes creatures/*.java enums/*.java interfaces/*.java room/*/*.java utils/Weather.java Main.java
java -cp classes Main
