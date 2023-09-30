#!/usr/bin/bash
# variant 1415

chmod -R 777 ~/VT-Labs/csbasic/lab1/lab0  
rm -rf ~/VT-Labs/csbasic/lab1/lab0/

# enable double asterisk
shopt -s globstar

# ================ PART 1 =================
# Create tree and write content
mkdir ~/VT-Labs/csbasic/lab1/lab0/
    mkdir ~/VT-Labs/csbasic/lab1/lab0/bulbasaur4/ 
        echo -e "Тип покемона  ROCK WATER" > ~/VT-Labs/csbasic/lab1/lab0/bulbasaur4/omastar
	    echo -e "Тип покемона  WATER\nNONE" > ~/VT-Labs/csbasic/lab1/lab0/bulbasaur4/seel
	    echo -e "Возможности  Overland=2 Surface=1 Sky=7 Jump=1 Power1=0\nIntelligence=4 Tracker=0" > ~/VT-Labs/csbasic/lab1/lab0/bulbasaur4/woobat
	    echo -e"satk=7 sdef=3 spd=6" > ~/VT-Labs/csbasic/lab1/lab0/bulbasaur4/horsea
	    echo -e "Живет\nForest Urban"> ~/VT-Labs/csbasic/lab1/lab0/bulbasaur4/persian
    echo -e "satk=13 sdef=12 spd=8" > ~/VT-Labs/csbasic/lab1/lab0/gardevoir0
    echo -e "Ходы  Bullet\nSeed Covet Fury Cutter Giga Drain Heal Bell Helping Hand Hyper\nVoice Irot Tail Knock Off Last Resort Mud-Slap Seed Bomb Sleep Talk Snore Swift Sunthesis Worry Seed" > ~/VT-Labs/csbasic/lab1/lab0/leafeon9
    mkdir ~/VT-Labs/csbasic/lab1/lab0/nosepass8/
        mkdir ~/VT-Labs/csbasic/lab1/lab0/nosepass8/grumpig ~/VT-Labs/csbasic/lab1/lab0/nosepass8/kingler
        echo -e "Ходы  Bullet Seed Giga Drain\nHelping Hand Natural Gift Rollout Seed Bomb Sleep Talk Snore Synthesis\nWorry Seed" > ~/VT-Labs/csbasic/lab1/lab0/nosepass8/cherrim
        echo -e "weight=92.6 height=55.0 atk=12\ndef=8" > ~/VT-Labs/csbasic/lab1/lab0/nosepass8/luxray
    echo -e "Возможности  Overland=1 Surface=5 Underwater=5 Sky=5\nJump=2 Power=2 Intelligence=4 Fountain=0 Gilled=0" > ~/VT-Labs/csbasic/lab1/lab0/staryu5
    mkdir ~/VT-Labs/csbasic/lab1/lab0/ursaring8/
        mkdir ~/VT-Labs/csbasic/lab1/lab0/ursaring8/munna ~/VT-Labs/csbasic/lab1/lab0/ursaring8/seedot
        echo -e "Возможности\nOverland=7 Surface=6 Jump=3 Power=5\nIntelligence=4" > ~/VT-Labs/csbasic/lab1/lab0/ursaring8/exploud
        echo -e "Способности  Sand-Attack Thundershock Quick\nAttack Double Kick Thunder Fang Pin Missile Agility Thunder Wave\nDischarge Last Resort Thunder" > ~/VT-Labs/csbasic/lab1/lab0/ursaring8/jolteon
        echo -e "Развитые способности  Super\nLuck" > ~/VT-Labs/csbasic/lab1/lab0/ursaring8/togekiss
# =========================================


# ================ PART 2 =================
# Set permissions
chmod 771 ~/VT-Labs/csbasic/lab1/lab0/bulbasaur4
chmod a=r ~/VT-Labs/csbasic/lab1/lab0/bulbasaur4/omastar
chmod 006 ~/VT-Labs/csbasic/lab1/lab0/bulbasaur4/seel
chmod 444 ~/VT-Labs/csbasic/lab1/lab0/bulbasaur4/woobat
chmod 004 ~/VT-Labs/csbasic/lab1/lab0/bulbasaur4/horsea
chmod 660 ~/VT-Labs/csbasic/lab1/lab0/bulbasaur4/persian

chmod 046 ~/VT-Labs/csbasic/lab1/lab0/gardevoir0
chmod 062 ~/VT-Labs/csbasic/lab1/lab0/leafeon9

chmod 733 ~/VT-Labs/csbasic/lab1/lab0/nosepass8
chmod 711 ~/VT-Labs/csbasic/lab1/lab0/nosepass8/grumpig
chmod 577 ~/VT-Labs/csbasic/lab1/lab0/nosepass8/kingler
chmod 440 ~/VT-Labs/csbasic/lab1/lab0/nosepass8/cherrim
chmod 622 ~/VT-Labs/csbasic/lab1/lab0/nosepass8/luxray

chmod 624 ~/VT-Labs/csbasic/lab1/lab0/staryu5

chmod 311 ~/VT-Labs/csbasic/lab1/lab0/ursaring8
chmod 357 ~/VT-Labs/csbasic/lab1/lab0/ursaring8/munna
chmod 524 ~/VT-Labs/csbasic/lab1/lab0/ursaring8/seedot
chmod 620 ~/VT-Labs/csbasic/lab1/lab0/ursaring8/exploud
chmod 604 ~/VT-Labs/csbasic/lab1/lab0/ursaring8/jolteon
chmod 600 ~/VT-Labs/csbasic/lab1/lab0/ursaring8/togekiss
# =========================================


# ================ PART 3 =================
# Copy and make links
ln -s ~/VT-Labs/csbasic/lab1/lab0/gardevoir0 ~/VT-Labs/csbasic/lab1/lab0/bulbasaur4/seelgardevoir

chmod u+r ~/VT-Labs/csbasic/lab1/lab0/leafeon9
chmod u+w ~/VT-Labs/csbasic/lab1/lab0/nosepass8/kingler
cp ~/VT-Labs/csbasic/lab1/lab0/leafeon9 ~/VT-Labs/csbasic/lab1/lab0/nosepass8/kingler
cp ~/VT-Labs/csbasic/lab1/lab0/leafeon9 ~/VT-Labs/csbasic/lab1/lab0/bulbasaur4/seelleafeon
chmod u-w ~/VT-Labs/csbasic/lab1/lab0/nosepass8/kingler
chmod u-r ~/VT-Labs/csbasic/lab1/lab0/leafeon9

ln ~/VT-Labs/csbasic/lab1/lab0/leafeon9 ~/VT-Labs/csbasic/lab1/lab0/bulbasaur4/omastarleafeon
cat ~/VT-Labs/csbasic/lab1/lab0/bulbasaur4/omastar ~/VT-Labs/csbasic/lab1/lab0/nosepass8/cherrim > ~/VT-Labs/csbasic/lab1/lab0/staryu5_66
ln -s ~/VT-Labs/csbasic/lab1/lab0/nosepass8 ~/VT-Labs/csbasic/lab1/lab0/Copy_64

chmod u+w ~/VT-Labs/csbasic/lab1/lab0/ursaring8/seedot
cp -R ~/VT-Labs/csbasic/lab1/lab0/nosepass8 ~/VT-Labs/csbasic/lab1/lab0/ursaring8/seedot
chmod u-w ~/VT-Labs/csbasic/lab1/lab0/ursaring8/seedot
# =========================================


# ================ PART 4 =================
# Search and filter
echo "======== 4.1 ========"
wc -m <~/VT-Labs/csbasic/lab1/lab0/staryu5 >>~/VT-Labs/csbasic/lab1/lab0/staryu5 2>&1 && echo "Done!"
echo "======== 4.2 ========"
ls -Rl 2>/tmp/s409858_errors.txt | grep "on" | sort -n -k 2
echo "======== 4.3 ========"
cat $(ls -dp1 $(pwd)/**/* | grep -E "(/[^/]+)+/l[^/:]+\$") | sort -r
echo "======== 4.4 ========"
cat ~/VT-Labs/csbasic/lab1/lab0/bulbasaur4/* | grep -v "Sle"
echo "======== 4.5 ========"
ls -tdp1l $(pwd)/**/* 2>&1 | grep -E "(/[^/]+)+/g.+\$"
echo "======== 4.6 ========"
cat ~/VT-Labs/csbasic/lab1/lab0/ursaring8/exploud ~/VT-Labs/csbasic/lab1/lab0/ursaring8/jolteon | grep ce
# =========================================


# ================ PART 5 =================
# delete files
rm -f ~/VT-Labs/csbasic/lab1/lab0/leafeon9
rm -r ~/VT-Labs/csbasic/lab1/lab0/ursaring8/exploud
rm ~/VT-Labs/csbasic/lab1/lab0/bulbasaur4/seelgardevo*
rm -f ~/VT-Labs/csbasic/lab1/lab0/bulbasaur4/omastarleafe*
chmod -R 777 ~/VT-Labs/csbasic/lab1/lab0/ursaring8 && rm -r ~/VT-Labs/csbasic/lab1/lab0/ursaring8
# rm -r ~/VT-Labs/csbasic/lab1/lab0/ursaring8/seedot
# =========================================
