function a() {
    a = true
    a+= a ? 6 : 8
}

NF == 6 {
    for (i = 0; i < 10; i++) {
        a(1)
    }
}