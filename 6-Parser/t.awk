BEGIN {for (i = 0; i < 10; i++) if (rand(i)) break; else prints(i);} function t(i) {return --i} NF == 6 {a[0] = 5;a[$0]=$1;delete a[$0]} END {j = 6; while (j > 0) {if (j ==2) continue; prints(j) }}