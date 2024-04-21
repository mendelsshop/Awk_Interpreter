{
    l = 0;
    while (getline) {
        i = 0
        do {
            printf " "
            i++
        } while (i <= len);
        print  toupper( $0)
        len += length($0);
        word[l] = word[l-1] $0      
        l++
    }

    for (i = 0; i < l; i++) {
        print word[i]
    }
}