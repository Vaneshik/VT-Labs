#!/bin/sh
echo 4.1
chmod u+r lab0/karrablast3
chmod u+r lab0/charmeleon4
grep -cR --include="k*" "" lab0|sort -n 2>&1
chmod u-r lab0/charmeleon4
chmod u-r lab0/karrablast3

echo 4.2
chmod u+r lab0/cranidos0/zorua
chmod u+r lab0/karrablast3
chmod u+r lab0/pignite2
chmod u+r lab0/charmeleon4
ls -lR $(grep -rl "vo" lab0)|sort -k9 -r 2>&1
chmod u-r lab0/cranidos0/zorua
chmod u-r lab0/karrablast3
chmod u-r lab0/pignite2
chmod u-r lab0/charmeleon4

echo 4.3
chmod u+r lab0/cranidos0/zorua
sort -r lab0/charmeleon4/klang lab0/charmeleon4/crobat lab0/charmeleon4/lopunny lab0/cranidos0/carracosta lab0/cranidos0/zorua lab0/karrablast3/sandshrew 2>&1
chmod u-r lab0/cranidos0/zorua

echo 4.4
chmod u+r lab0/karrablast3
grep -Rh "e$" lab0/karrablast3/|cat -n 2> /tmp/lab0_errors.log
chmod u-r lab0/karrablast3

echo 4.5
chmod u+r lab0/cranidos0/zorua
chmod u+r lab0/karrablast3
chmod u+r lab0/charmeleon4
grep -cR --include="*a" "" lab0|sort -n 2>&1
chmod u-r lab0/cranidos0/zorua
chmod u-r lab0/karrablast3
chmod u-r lab0/charmeleon4

echo 4.6
chmod u+r lab0/karrablast3
grep -Rh "d$" lab0/karrablast3/
chmod u-r lab0/karrablast3

