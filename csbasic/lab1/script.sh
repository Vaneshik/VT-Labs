#!/usr/bin/bash
# variant 1415

chmod -R 777 lab0  
rm -rf lab0

# ================ PART 1 =================
# Create tree and write content
mkdir lab0 && cd lab0
    mkdir bulbasaur4 && cd bulbasaur4 
        echo -e "Тип покемона  ROCK WATER" > omastar
	    echo -e "Тип покемона  WATER\nNONE" > seel
	    echo -e "Возможности  Overland=2 Surface=1 Sky=7 Jump=1 Power1=0\nIntelligence=4 Tracker=0" > woobat
	    echo -e"satk=7 sdef=3 spd=6" > horsea
	    echo -e "Живет\nForest Urban"> persian
        cd ..

    echo -e "satk=13 sdef=12 spd=8" > gardevoir0
    echo -e "Ходы  Bullet\nSeed Covet Fury Cutter Giga Drain Heal Bell Helping Hand Hyper\nVoice Irot Tail Knock Off Last Resort Mud-Slap Seed Bomb Sleep Talk Snore Swift Sunthesis Worry Seed" > leafeon9
    
    mkdir nosepass8 && cd nosepass8
        mkdir grumpig kingler
        echo -e "Ходы  Bullet Seed Giga Drain\nHelping Hand Natural Gift Rollout Seed Bomb Sleep Talk Snore Synthesis\nWorry Seed" > cherrim
        echo -e "weight=92.6 height=55.0 atk=12\ndef=8" > luxray
        cd ..
    
    echo -e "Возможности  Overland=1 Surface=5 Underwater=5 Sky=5\nJump=2 Power=2 Intelligence=4 Fountain=0 Gilled=0" > staryu5

    mkdir ursaring8 && cd ursaring8
        mkdir munna seedot
        echo -e "Возможности\nOverland=7 Surface=6 Jump=3 Power=5\nIntelligence=4" > exploud
        echo -e "Способности  Sand-Attack Thundershock Quick\nAttack Double Kick Thunder Fang Pin Missile Agility Thunder Wave\nDischarge Last Resort Thunder" > jolteon
        echo -e "Развитые способности  Super\nLuck" > togekiss
	cd ..
# =========================================


# ================ PART 2 =================
# Set permissions
chmod 771 bulbasaur4
chmod a=r bulbasaur4/omastar
chmod 006 bulbasaur4/seel
chmod 444 bulbasaur4/woobat
chmod 004 bulbasaur4/horsea
chmod 660 bulbasaur4/persian

chmod 046 gardevoir0
chmod 062 leafeon9

chmod 733 nosepass8
chmod 711 nosepass8/grumpig
chmod 577 nosepass8/kingler
chmod 440 nosepass8/cherrim
chmod 622 nosepass8/luxray

chmod 624 staryu5

chmod 311 ursaring8
chmod 357 ursaring8/munna
chmod 524 ursaring8/seedot
chmod 620 ursaring8/exploud
chmod 604 ursaring8/jolteon
chmod 600 ursaring8/togekiss
# =========================================


# ================ PART 3 =================
# Copy and make links
ln -s gardevoir0 ./bulbasaur4/seelgardevoir

chmod u+r leafeon9
chmod u+w nosepass8/kingler
cp leafeon9 ./nosepass8/kingler
cp leafeon9 ./bulbasaur4/seelleafeon
chmod u-w nosepass8/kingler
chmod u-r leafeon9

ln leafeon9 ./bulbasaur4/omastarleafeon
cat ./bulbasaur4/omastar ./nosepass8/cherrim > ./staryu5_66
ln -s nosepass8 ./Copy_64

chmod u+w ursaring8/seedot
cp -R nosepass8 ./ursaring8/seedot
chmod u-w ursaring8/seedot 
# =========================================


# ================ PART 4 =================
# Search and filter
echo "======== 4.1 ========"
wc -m <staryu5 >>staryu5 && echo "Done!"
echo "======== 4.2 ========"
ls -Rl 2>/tmp/s409858_errors.txt | grep "on" | sort -n -k 2
echo "======== 4.3 ========"
cat $(ls -dp1 $PWD/** | grep -E "(/[^/]+)+/l[^/:]+\$") | sort -r
echo "======== 4.4 ========"
cat bulbasaur4/* | grep -v "Sle"
echo "======== 4.5 ========"
ls -tdp1l $PWD/** | grep -E "(/[^/]+)+/g.+\$"
echo "======== 4.6 ========"
cat ursaring8/exploud ursaring8/jolteon | grep ce
# =========================================

# echo -e "\nTree:"
# tree

# ================ PART 5 =================
# delete files
rm -f leafeon9
rm -rf ./ursaring8/exploud
rm -f ./bulbasaur4/seelgardevo*
rm -f ./bulbasaur4/omastarleafe*
chmod -R 777 ursaring8 && rm -rf ursaring8
# rm -rf ursaring8/seedot
# =========================================