#!/bin/sh
javac -d classes creatures/*.java enums/*.java interfaces/*.java room/*.java Main.java
java -cp classes Main