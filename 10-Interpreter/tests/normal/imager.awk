

NR == 1 {
    if (NF == 1) {
        size = +$1   
    }
    else {
        print "ERROR: First line must contain only one number the square side size"
        exit 1
    }
    if (size < 1) {
        print "ERROR: Size must be greater than 0"
        exit 1
    }
    total = size * size
    FS = ""
    x = 0
    y = 0
}   

NR != 1 {
    for (i = 1; i <= NF; i++) {
        if ($i == ".") {
            # mark x, y as .
            pic["x" x "y" y] = "."
        }
        else if ($i == "#") {
            # mark x, y as erased
            pic["x" x "y" y] = ""
        }
        else if ($i == "u") {
            # move up
            y--
        }
        else if ($i == "d") {
            # move down
            y++
        }
        else if ($i == "l") {
            # move left
            x--
        }
        else if ($i == "r") {
            # move right
            x++
        }
        else {
            print "ERROR: Invalid character", $i
            exit 1
        }
        check_pos()
    }
}

END {
    print "picture:"
    for (i = 0; i < size; i++) {
        for (j = 0; j < size; j++) {
            if (pic["x" j "y" i] == "") {
                printf " "
            }
            else {
                printf "%s", pic["x" j "y" i]
            }
        }
            print ""
    }
        print "end"
}

function check_pos() {
    if (x < 0 || x >= size || y < 0 || y >= size) {
        print "ERROR: Out of bounds"
        exit 1
    }
}