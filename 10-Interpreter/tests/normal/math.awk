{
    len = length($0)
    len %= 5 ^ (len % 2)
    print len++
    len += len && !len
    if (len) {
        print len
    } else {
        print len++ || "pizza"
    } 
}