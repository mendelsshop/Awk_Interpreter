BEGIN {FS=","} # set field separator to `,` so it works for csv
    {
        sum  = 0 # reset sum for each line
        for (i = 1; i <= NF; i++) sum = sum + $i # sum up current line
        total = sum + total # add sum of current line to total
        print "Line", NR ":", sum
    }
END { print "Grand total:", total }