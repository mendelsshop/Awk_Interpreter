{
    print "Classes: " $0
    for (i = 1; i < NF; i++) {
        if ($i !~ `[0-9][0-9][0-9][a-z]?`) {
            print "Error: " $i " is not a valid class"
        } else if ($i ~ `[0-9][0-9][0-9][a-z]`) {
            print "Special class: " $i
        } else {
            print "Class: " $i
        }
    }

}