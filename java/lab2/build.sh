#!/usr/bin/bash
javac -d build -sourcepath src -cp libs/Pokemon.jar src/Main.java
echo -e "Main-Class: Main\nClass-Path: libs/Pokemon.jar" > MANIFEST.mf
jar cfm lab2.jar MANIFEST.mf -C build .
rm -rf build MANIFEST.mf
