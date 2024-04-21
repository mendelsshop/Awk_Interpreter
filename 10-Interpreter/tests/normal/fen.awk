
BEGIN {
    FS = "/"
    PIECES["K"] = "♔"
    PIECES["Q"] = "♕"
    PIECES["R"] = "♖"
    PIECES["B"] = "♗"
    PIECES["N"] = "♘"
    PIECES["P"] = "♙"
    PIECES["k"] = "♚"
    PIECES["q"] = "♛"
    PIECES["r"] = "♜"
    PIECES["b"] = "♝"
    PIECES["n"] = "♞"
    PIECES["p"] = "♟"
}

{
    parse_fen($0)
}


function parse_fen(fen) {

 if (NF != 8) {
     print "Invalid FEN: " fen
     exit 1
 }
 for (ii = 1; ii < 8; ii++) {
        parse_row($ii)
 }
 FS = " "
 $0 = $8
 if (NF != 6) {
     print "Invalid FEN: " fen
     exit 1

}
    parse_row($1)
    color($2)
    castling($3)
    en_pessant($4)
    half_clock($5)
    full_clock($6)
}
function color(c) {
    if (c == "w") {
        print "White to move"
    }
    else if (c == "b") {
        print "Black to move"
    }
    else {
        print "Invalid FEN: not recondingzed color", c
        exit 1
    }
}

function castling(c) {
    if (c == "-") {
        print "No castling"
    }
    else {
        castle[1]
        split_string(c, castle)
        for (i in castle) {
            if (castle[i] == "K") {
                printf "white king side castling "
            }
            else if (castle[i] == "Q") {
                printf "white queen side castling "
            }
            else if (castle[i] == "k") {
                printf "black king side castling "
            }
            else if (castle[i] == "q") {
                printf "black queen side castling "
            }
            else {
                print "Invalid FEN: not recondingzed castling", castle[i]
                exit 1
            }
        }
        print ""
    }
}

function half_clock(c) {
    if (match(c, "^[0-9]+$")) {
        print "half clock: ", c
    }
    else {
        print "Invalid FEN: not recondingzed half clock", c
        exit 1
    }
}

function full_clock(c) {
    if (match(c, /^[0-9]+$/)) {
        print "full clock: ", c
    }
    else {
        print "Invalid FEN: not recondingzed full clock", c
        exit 1
    }
}
function en_pessant(c) {
    if (c == "-") {
        print "No en pessant"
    }
    else if (c ~ /^[a-h][1-8]$/) {
        printf "En pessant %s\n", c
    }
    else {
        print "Invalid FEN: not recondingzed en pessant", c
        exit 1
    }
}

function split_string(str, row) {
    delete row
    split(str, row, "")
}

function parse_row(row_str) {
    row[1]
    split_string(row_str, row)
    len = 0
    for (i in row) {
        if (len > 8) {
            print "Invalid FEN row: ", row_str
            exit 1
        }
        else
        if (row[i] > 0 && row[i] < 9) {
            for (j = 0; j < row[i]; j++) {
                printf "."
            }
            len += row[i]
        }
        else if (row[i] in PIECES) {
            printf "%s", PIECES[row[i]]
            len += 1
        }
        else {
            print "Invalid FEN: not recondingzed piece", row[i]
            exit 1
        }
    }    
    if (len > 8) {
            print "Invalid FEN row: ", row_str
            exit 1
    }
    print ""
}
