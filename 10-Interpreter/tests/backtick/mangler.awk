{
    for (i = 1 ; i <= NF; i++) {
        len = length($i)
        if (len > max) {
            max = len
            word = $i
        }
        gsub(" ", word  len ^ index($i, "a") (len  > 4 ? 3 : 0), $0)
        FS = word
        sub(".o", "why o", word)
    }
}